package io.arconia.rewrite.openapi.swagger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.marker.JavaProject;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.JavaSourceFile;
import org.openrewrite.marker.SearchResult;

/**
 * Finds the source files of a project whose code uses none of the {@code javax} Jakarta EE APIs that Swagger Core
 * reads. Meant as the precondition of {@code UpgradeSwaggerCore_3}, which migrates a project only when it is
 * already on the {@code jakarta} namespace, since Swagger Core 3 supports no other.
 * Each project of a multi-project build, identified by its {@link JavaProject} marker, qualifies on its own.
 *
 * <p>Swagger Core scans REST resources, runs as a Servlet, and turns Bean Validation constraints and JAXB annotations
 * into the OpenAPI schema. A project that uses any of those APIs from the {@code javax} namespace would either fail to
 * run on Swagger Core 3 or silently lose part of its OpenAPI document, so it does not qualify.
 */
public class FindProjectsWithoutJavaxEnterpriseApis extends ScanningRecipe<FindProjectsWithoutJavaxEnterpriseApis.Accumulator> {

    private static final List<String> JAVAX_TYPE_PATTERNS = List.of(
            "javax.ws.rs..*",
            "javax.servlet..*",
            "javax.validation..*",
            "javax.xml.bind..*");

    @Override
    public String getDisplayName() {
        return "Find projects that use no `javax` Jakarta EE APIs read by Swagger Core";
    }

    @Override
    public String getDescription() {
        return "Find the source files of a project whose code uses none of the `javax` REST, Servlet, Bean Validation, " +
                "or JAXB APIs. Swagger Core 3 supports only the `jakarta` namespace, so a project that still uses " +
                "them does not qualify.";
    }

    @Override
    public Accumulator getInitialValue(ExecutionContext ctx) {
        return new Accumulator();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(Accumulator acc) {
        return new TreeVisitor<>() {
            @Override
            public @Nullable Tree visit(@Nullable Tree tree, ExecutionContext ctx) {
                if (tree instanceof JavaSourceFile sourceFile) {
                    JavaProject project = project(sourceFile);
                    if (!acc.javaxProjects.contains(project) && usesJavaxApi(sourceFile, ctx)) {
                        acc.javaxProjects.add(project);
                    }
                }
                return tree;
            }
        };
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor(Accumulator acc) {
        return new TreeVisitor<>() {
            @Override
            public @Nullable Tree visit(@Nullable Tree tree, ExecutionContext ctx) {
                if (tree instanceof SourceFile sourceFile && !acc.javaxProjects.contains(project(sourceFile))) {
                    return sourceFile.withMarkers(sourceFile.getMarkers().addIfAbsent(
                            new SearchResult(Tree.randomId(), "Uses no javax Jakarta EE APIs read by Swagger Core")));
                }
                return tree;
            }
        };
    }

    private static boolean usesJavaxApi(JavaSourceFile sourceFile, ExecutionContext ctx) {
        for (String pattern : JAVAX_TYPE_PATTERNS) {
            UsesType<ExecutionContext> usesType = new UsesType<>(pattern, true);
            if (usesType.isAcceptable(sourceFile, ctx) && usesType.visit(sourceFile, ctx) != sourceFile) {
                return true;
            }
        }
        return false;
    }

    // Files without a JavaProject marker, such as in tests, count as a single project.
    private static @Nullable JavaProject project(SourceFile sourceFile) {
        return sourceFile.getMarkers().findFirst(JavaProject.class).orElse(null);
    }

    public static class Accumulator {

        // The projects whose code uses a javax Jakarta EE API.
        private final Set<@Nullable JavaProject> javaxProjects = new HashSet<>();

    }

}
