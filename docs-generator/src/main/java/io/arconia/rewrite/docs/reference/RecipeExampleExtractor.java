package io.arconia.rewrite.docs.reference;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

/**
 * Extracts the canonical before/after example for each recipe from its {@code @DocumentExample}
 * tests, by statically parsing the recipe modules' test sources with the OpenRewrite Java parser.
 * <p>
 * This is based on our test conventions based on the {@code RewriteTest} interface. The recipe
 * under test comes from {@code defaults(...)} ({@code recipeFromResources("id")} or
 * {@code recipe(new X(...))}), and the example comes from the {@code rewriteRun(...)} of the
 * first {@code @DocumentExample} method, whose source-spec helpers ({@code java}, {@code properties},
 * {@code buildGradle}, …) carry the before/after as their first two string arguments.
 */
final class RecipeExampleExtractor {

    // One source file's before/after in an example.
    record Source(String language, String before, String after) {
    }

    // A recipe's canonical example: the source files it transforms.
    record Example(List<Source> sources) {
    }

    // Source-spec helper name -> highlight language.
    private static final Map<String, String> SOURCE_LANG = Map.ofEntries(
            Map.entry("java", "java"),
            Map.entry("groovy", "groovy"),
            Map.entry("buildGradle", "groovy"),
            Map.entry("buildGradleKts", "kotlin"),
            Map.entry("settingsGradle", "groovy"),
            Map.entry("properties", "properties"),
            Map.entry("yaml", "yaml"),
            Map.entry("pomXml", "xml"),
            Map.entry("xml", "xml"),
            Map.entry("json", "json"),
            Map.entry("text", "text"),
            Map.entry("hcl", "hcl"),
            Map.entry("dockerfile", "docker"),
            Map.entry("protobuf", "protobuf"));

    private RecipeExampleExtractor() {
    }

    // Recipe id -> canonical example, for every @DocumentExample test under the given roots.
    static Map<String, Example> extract(Path repoRoot, List<Path> testRoots) {
        List<Path> files = new ArrayList<>();
        for (Path root : testRoots) {
            if (!Files.isDirectory(root)) {
                continue;
            }
            try (Stream<Path> walk = Files.walk(root)) {
                walk.filter(p -> p.toString().endsWith(".java"))
                        .filter(RecipeExampleExtractor::mentionsDocumentExample)
                        .forEach(files::add);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        files.sort(Comparator.naturalOrder());

        Map<String, Example> result = new HashMap<>();
        if (files.isEmpty()) {
            return result;
        }
        JavaParser parser = JavaParser.fromJavaVersion().build();
        ExecutionContext ctx = new InMemoryExecutionContext();
        parser.parse(files, repoRoot, ctx).forEach(sf -> {
            if (sf instanceof J.CompilationUnit cu) {
                process(cu, result);
            }
        });
        System.out.println("Extracted " + result.size() + " examples from "
                + files.size() + " @DocumentExample test files.");
        return result;
    }

    private static boolean mentionsDocumentExample(Path p) {
        try {
            return Files.readString(p).contains("DocumentExample");
        } catch (IOException ex) {
            return false;
        }
    }

    private static void process(J.CompilationUnit cu, Map<String, Example> out) {
        Map<String, String> imports = new HashMap<>();
        for (J.Import imp : cu.getImports()) {
            if (!imp.isStatic()) {
                String fqn = imp.getTypeName();
                imports.put(simpleName(fqn), fqn);
            }
        }
        String pkg = packageOf(cu);
        for (J.ClassDeclaration cd : cu.getClasses()) {
            String recipeId = recipeIdFromDefaults(cd, imports, pkg);
            if (recipeId == null) {
                continue;
            }
            Example example = firstExample(cd);
            if (example != null && !example.sources().isEmpty()) {
                out.putIfAbsent(recipeId, example);
            }
        }
    }

    private static @Nullable String recipeIdFromDefaults(J.ClassDeclaration cd,
                                                         Map<String, String> imports, String pkg) {
        J.MethodDeclaration defaults = method(cd, "defaults");
        if (defaults == null) {
            return null;
        }
        // Accumulate into a list (first match wins) to avoid a @Nullable type argument.
        List<String> ids = new ArrayList<>();
        new JavaIsoVisitor<List<String>>() {
            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation m, List<String> acc) {
                if (acc.isEmpty()) {
                    if (m.getSimpleName().equals("recipeFromResources")) {
                        String literal = firstStringArg(m);
                        if (literal != null) {
                            acc.add(literal);
                        }
                    } else if (m.getSimpleName().equals("recipe")) {
                        String fqn = newClassFqn(m, imports, pkg);
                        if (fqn != null) {
                            acc.add(fqn);
                        }
                    }
                }
                return super.visitMethodInvocation(m, acc);
            }
        }.visit(defaults, ids);
        return ids.isEmpty() ? null : ids.getFirst();
    }

    private static @Nullable String newClassFqn(J.MethodInvocation recipeCall,
                                                Map<String, String> imports, String pkg) {
        if (recipeCall.getArguments().isEmpty()) {
            return null;
        }
        Expression first = recipeCall.getArguments().getFirst();
        if (!(first instanceof J.NewClass newClass)) {
            return null;
        }
        String simple = typeName(newClass.getClazz());
        if (simple == null) {
            return null;
        }
        String imported = imports.get(simple);
        if (imported != null) {
            return imported;
        }
        return pkg.isEmpty() ? simple : pkg + "." + simple;
    }

    private static @Nullable Example firstExample(J.ClassDeclaration cd) {
        for (Statement st : cd.getBody().getStatements()) {
            if (st instanceof J.MethodDeclaration md && hasDocumentExample(md)) {
                return new Example(extractSources(md));
            }
        }
        return null;
    }

    private static boolean hasDocumentExample(J.MethodDeclaration md) {
        for (J.Annotation a : md.getLeadingAnnotations()) {
            if (a.getSimpleName().equals("DocumentExample")) {
                return true;
            }
        }
        return false;
    }

    private static List<Source> extractSources(J.MethodDeclaration md) {
        List<Source> sources = new ArrayList<>();
        new JavaIsoVisitor<List<Source>>() {
            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation m, List<Source> acc) {
                String lang = SOURCE_LANG.get(m.getSimpleName());
                if (lang != null) {
                    List<String> strings = stringArgs(m);
                    if (strings.size() >= 2) {
                        acc.add(new Source(lang, strings.get(0), strings.get(1)));
                    }
                }
                return super.visitMethodInvocation(m, acc);
            }
        }.visit(md, sources);
        return sources;
    }

    // ---- low-level ----------------------------------------------------------

    private static J.@Nullable MethodDeclaration method(J.ClassDeclaration cd, String name) {
        for (Statement st : cd.getBody().getStatements()) {
            if (st instanceof J.MethodDeclaration md && md.getSimpleName().equals(name)) {
                return md;
            }
        }
        return null;
    }

    private static @Nullable String firstStringArg(J.MethodInvocation m) {
        for (Expression a : m.getArguments()) {
            if (a instanceof J.Literal lit && lit.getValue() instanceof String s) {
                return s;
            }
        }
        return null;
    }

    private static List<String> stringArgs(J.MethodInvocation m) {
        List<String> strings = new ArrayList<>();
        for (Expression a : m.getArguments()) {
            if (a instanceof J.Literal lit && lit.getValue() instanceof String s) {
                strings.add(s);
            }
        }
        return strings;
    }

    private static @Nullable String typeName(@Nullable J tree) {
        if (tree instanceof J.Identifier id) {
            return id.getSimpleName();
        }
        if (tree instanceof J.ParameterizedType pt) {
            return typeName(pt.getClazz());
        }
        if (tree instanceof J.FieldAccess fa) {
            return fa.getSimpleName();
        }
        return null;
    }

    private static String simpleName(String fqn) {
        int dot = fqn.lastIndexOf('.');
        return dot < 0 ? fqn : fqn.substring(dot + 1);
    }

    private static String packageOf(J.CompilationUnit cu) {
        String path = cu.getSourcePath().toString().replace('\\', '/');
        String marker = "src/test/java/";
        int idx = path.indexOf(marker);
        if (idx < 0) {
            marker = "src/main/java/";
            idx = path.indexOf(marker);
        }
        if (idx < 0) {
            return "";
        }
        String rel = path.substring(idx + marker.length());
        int lastSlash = rel.lastIndexOf('/');
        return lastSlash < 0 ? "" : rel.substring(0, lastSlash).replace('/', '.');
    }

}
