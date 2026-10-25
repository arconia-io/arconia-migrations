package io.arconia.rewrite.spring.ai2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.gradle.marker.GradleDependencyConfiguration;
import org.openrewrite.gradle.marker.GradleProject;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.marker.JavaProject;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaSourceFile;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.marker.SearchResult;
import org.openrewrite.maven.tree.Dependency;
import org.openrewrite.maven.tree.MavenResolutionResult;

/**
 * Finds the source files of a project whose Spring AI chat memory can be migrated to session memory
 * without changing what the application does. Meant as the precondition of
 * {@code MigrateSpringAiChatMemoryToSessionMemory}, which migrates a project only when every chat memory usage in
 * it can be migrated.
 * Each project of a multi-project build, identified by its {@link JavaProject} marker, qualifies on its own.
 *
 * <p>A project qualifies when it depends on the JDBC chat memory starter, which auto-configures the
 * only chat memory store with a session memory counterpart, and its code uses chat memory only in
 * ways that map one to one onto session memory: the auto-configured {@code ChatMemory} bean, read with
 * {@code get} and cleared with {@code clear}, and a {@code MessageChatMemoryAdvisor} built inline as
 * an argument of a {@code ChatClient} advisors method, optionally with an order. Any other chat memory
 * dependency, a chat memory or repository built or implemented in code, a call to {@code add}, another
 * Spring AI API that takes a {@code ChatMemory}, or a {@code MessageChatMemoryAdvisor} held anywhere
 * else rules the project out.
 */
public class FindMigratableSpringAiChatMemory extends ScanningRecipe<FindMigratableSpringAiChatMemory.Accumulator> {

    private static final String SPRING_AI_GROUP_ID = "org.springframework.ai";

    private static final String JDBC_STARTER = "spring-ai-starter-model-chat-memory-repository-jdbc";

    private static final String CHAT_MEMORY_FQN = "org.springframework.ai.chat.memory.ChatMemory";

    private static final String CHAT_MEMORY_REPOSITORY_FQN = "org.springframework.ai.chat.memory.ChatMemoryRepository";

    private static final List<String> UNSUPPORTED_TYPES = List.of(
            "org.springframework.ai.chat.memory.MessageWindowChatMemory",
            "org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor",
            MessageChatMemoryAdvisorChain.ADVISOR_FQN,
            CHAT_MEMORY_REPOSITORY_FQN);

    private static final MethodMatcher CHAT_MEMORY_ADD = new MethodMatcher(CHAT_MEMORY_FQN + " add(..)", true);

    @Override
    public String getDisplayName() {
        return "Find projects whose Spring AI chat memory can be migrated to session memory";
    }

    @Override
    public String getDescription() {
        return "Find the source files of a project that uses the auto-configured JDBC chat memory only in ways that map " +
                "one to one onto Spring AI session memory. A project that uses another chat memory store, builds or " +
                "implements a chat memory or repository in code, appends messages to the chat memory directly, or holds " +
                "a `MessageChatMemoryAdvisor` outside a `ChatClient` advisors call does not qualify.";
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
                if (!(tree instanceof SourceFile sourceFile)) {
                    return tree;
                }
                JavaProject project = project(sourceFile);
                sourceFile.getMarkers().findFirst(MavenResolutionResult.class).ifPresent(result ->
                        result.getPom().getRequestedDependencies().forEach(dependency -> recordDependency(acc, project, dependency)));
                sourceFile.getMarkers().findFirst(GradleProject.class).ifPresent(gradleProject -> {
                    for (GradleDependencyConfiguration configuration : gradleProject.getConfigurations()) {
                        configuration.getRequested().forEach(dependency -> recordDependency(acc, project, dependency));
                    }
                });
                if (sourceFile instanceof JavaSourceFile && !acc.unsupportedProjects.contains(project)) {
                    UsageScanner scanner = new UsageScanner();
                    scanner.visit(sourceFile, ctx);
                    if (scanner.unsupported) {
                        acc.unsupportedProjects.add(project);
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
                if (tree instanceof SourceFile sourceFile && acc.isMigratable(project(sourceFile))) {
                    return sourceFile.withMarkers(sourceFile.getMarkers().addIfAbsent(
                            new SearchResult(Tree.randomId(), "Spring AI chat memory can be migrated to session memory")));
                }
                return tree;
            }
        };
    }

    private static void recordDependency(Accumulator acc, @Nullable JavaProject project, Dependency dependency) {
        if (!SPRING_AI_GROUP_ID.equals(dependency.getGroupId())) {
            return;
        }
        String artifactId = dependency.getArtifactId();
        if (JDBC_STARTER.equals(artifactId)) {
            acc.jdbcStarterProjects.add(project);
        }
        else if (artifactId.contains("chat-memory")) {
            acc.unsupportedProjects.add(project);
        }
    }

    private static boolean isUnsupportedType(@Nullable JavaType type) {
        JavaType.FullyQualified fullyQualified = TypeUtils.asFullyQualified(type);
        if (fullyQualified == null) {
            return false;
        }
        String name = fullyQualified.getFullyQualifiedName();
        return UNSUPPORTED_TYPES.stream().anyMatch(unsupported -> name.equals(unsupported) || name.startsWith(unsupported + "$"))
                || name.startsWith("org.springframework.ai.chat.memory.repository.");
    }

    private static boolean takesChatMemory(JavaType.@Nullable Method methodType) {
        if (methodType == null || !methodType.getDeclaringType().getFullyQualifiedName().startsWith(SPRING_AI_GROUP_ID)) {
            return false;
        }
        return methodType.getParameterTypes().stream().anyMatch(type -> TypeUtils.isOfClassType(type, CHAT_MEMORY_FQN));
    }

    private static class UsageScanner extends JavaIsoVisitor<ExecutionContext> {

        // Whether anything in the scanned source file rules out the migration.
        boolean unsupported;

        private int advisorBuilders;

        private int migratableChains;

        @Override
        public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext ctx) {
            advisorBuilders = 0;
            migratableChains = 0;
            J.CompilationUnit c = super.visitCompilationUnit(cu, ctx);
            if (advisorBuilders != migratableChains) {
                unsupported = true;
            }
            return c;
        }

        @Override
        public J.Import visitImport(J.Import anImport, ExecutionContext ctx) {
            return anImport;
        }

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
            JavaType.FullyQualified type = classDecl.getType();
            if (type != null && (TypeUtils.isAssignableTo(CHAT_MEMORY_FQN, type) || TypeUtils.isAssignableTo(CHAT_MEMORY_REPOSITORY_FQN, type))) {
                unsupported = true;
            }
            return super.visitClassDeclaration(classDecl, ctx);
        }

        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
            if (MessageChatMemoryAdvisorChain.BUILDER.matches(method)) {
                advisorBuilders++;
            }
            else if (CHAT_MEMORY_ADD.matches(method) || takesChatMemory(method.getMethodType())) {
                unsupported = true;
            }
            if (MessageChatMemoryAdvisorChain.find(method) != null) {
                migratableChains++;
            }
            return super.visitMethodInvocation(method, ctx);
        }

        @Override
        public J.NewClass visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
            if (takesChatMemory(newClass.getConstructorType())) {
                unsupported = true;
            }
            return super.visitNewClass(newClass, ctx);
        }

        @Override
        public J.Identifier visitIdentifier(J.Identifier identifier, ExecutionContext ctx) {
            Object parent = getCursor().getParentTreeCursor().getValue();
            boolean methodName = parent instanceof J.MethodInvocation invocation && invocation.getName() == identifier;
            boolean builderReceiver = parent instanceof J.MethodInvocation invocation && invocation.getSelect() == identifier
                    && MessageChatMemoryAdvisorChain.BUILDER.matches(invocation);
            if (!methodName && !builderReceiver && isUnsupportedType(identifier.getType())) {
                unsupported = true;
            }
            return super.visitIdentifier(identifier, ctx);
        }

        @Override
        public J.FieldAccess visitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext ctx) {
            if (isUnsupportedType(fieldAccess.getType())) {
                unsupported = true;
            }
            return super.visitFieldAccess(fieldAccess, ctx);
        }

    }

    // Files without a JavaProject marker, such as in tests, count as a single project.
    private static @Nullable JavaProject project(SourceFile sourceFile) {
        return sourceFile.getMarkers().findFirst(JavaProject.class).orElse(null);
    }

    public static class Accumulator {

        // The projects that declare the JDBC chat memory starter.
        private final Set<@Nullable JavaProject> jdbcStarterProjects = new HashSet<>();

        // The projects in which something rules out the migration.
        private final Set<@Nullable JavaProject> unsupportedProjects = new HashSet<>();

        boolean isMigratable(@Nullable JavaProject project) {
            return jdbcStarterProjects.contains(project) && !unsupportedProjects.contains(project);
        }

    }

}
