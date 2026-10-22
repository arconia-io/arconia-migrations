package io.arconia.rewrite.spring.ai2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * {@code MigrateSpringAiChatMemoryToSessionMemory}, which must apply to the whole project or not at all.
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
                ProjectUsage usage = acc.usage(sourceFile);
                sourceFile.getMarkers().findFirst(MavenResolutionResult.class).ifPresent(result ->
                        result.getPom().getRequestedDependencies().forEach(dependency -> recordDependency(usage, dependency)));
                sourceFile.getMarkers().findFirst(GradleProject.class).ifPresent(project -> {
                    for (GradleDependencyConfiguration configuration : project.getConfigurations()) {
                        configuration.getRequested().forEach(dependency -> recordDependency(usage, dependency));
                    }
                });
                if (sourceFile instanceof JavaSourceFile && !usage.unsupported) {
                    new UsageScanner().visit(sourceFile, usage);
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
                if (tree instanceof SourceFile sourceFile && acc.usage(sourceFile).isMigratable()) {
                    return sourceFile.withMarkers(sourceFile.getMarkers().addIfAbsent(
                            new SearchResult(Tree.randomId(), "Spring AI chat memory can be migrated to session memory")));
                }
                return tree;
            }
        };
    }

    private static void recordDependency(ProjectUsage usage, Dependency dependency) {
        if (!SPRING_AI_GROUP_ID.equals(dependency.getGroupId())) {
            return;
        }
        String artifactId = dependency.getArtifactId();
        if (JDBC_STARTER.equals(artifactId)) {
            usage.jdbcStarter = true;
        }
        else if (artifactId.contains("chat-memory")) {
            usage.unsupported = true;
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

    private static class UsageScanner extends JavaIsoVisitor<ProjectUsage> {

        private int advisorBuilders;

        private int migratableChains;

        @Override
        public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ProjectUsage usage) {
            advisorBuilders = 0;
            migratableChains = 0;
            J.CompilationUnit c = super.visitCompilationUnit(cu, usage);
            if (advisorBuilders != migratableChains) {
                usage.unsupported = true;
            }
            return c;
        }

        @Override
        public J.Import visitImport(J.Import anImport, ProjectUsage usage) {
            return anImport;
        }

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ProjectUsage usage) {
            JavaType.FullyQualified type = classDecl.getType();
            if (type != null && (TypeUtils.isAssignableTo(CHAT_MEMORY_FQN, type) || TypeUtils.isAssignableTo(CHAT_MEMORY_REPOSITORY_FQN, type))) {
                usage.unsupported = true;
            }
            return super.visitClassDeclaration(classDecl, usage);
        }

        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ProjectUsage usage) {
            if (MessageChatMemoryAdvisorChain.BUILDER.matches(method)) {
                advisorBuilders++;
            }
            else if (CHAT_MEMORY_ADD.matches(method) || takesChatMemory(method.getMethodType())) {
                usage.unsupported = true;
            }
            if (MessageChatMemoryAdvisorChain.find(method) != null) {
                migratableChains++;
            }
            return super.visitMethodInvocation(method, usage);
        }

        @Override
        public J.NewClass visitNewClass(J.NewClass newClass, ProjectUsage usage) {
            if (takesChatMemory(newClass.getConstructorType())) {
                usage.unsupported = true;
            }
            return super.visitNewClass(newClass, usage);
        }

        @Override
        public J.Identifier visitIdentifier(J.Identifier identifier, ProjectUsage usage) {
            Object parent = getCursor().getParentTreeCursor().getValue();
            boolean methodName = parent instanceof J.MethodInvocation invocation && invocation.getName() == identifier;
            boolean builderReceiver = parent instanceof J.MethodInvocation invocation && invocation.getSelect() == identifier
                    && MessageChatMemoryAdvisorChain.BUILDER.matches(invocation);
            if (!methodName && !builderReceiver && isUnsupportedType(identifier.getType())) {
                usage.unsupported = true;
            }
            return super.visitIdentifier(identifier, usage);
        }

        @Override
        public J.FieldAccess visitFieldAccess(J.FieldAccess fieldAccess, ProjectUsage usage) {
            if (isUnsupportedType(fieldAccess.getType())) {
                usage.unsupported = true;
            }
            return super.visitFieldAccess(fieldAccess, usage);
        }

    }

    public static class Accumulator {

        // Chat memory usage of each project, keyed by the project the source files belong to.
        private final Map<@Nullable JavaProject, ProjectUsage> projects = new HashMap<>();

        ProjectUsage usage(SourceFile sourceFile) {
            return projects.computeIfAbsent(sourceFile.getMarkers().findFirst(JavaProject.class).orElse(null),
                    project -> new ProjectUsage());
        }

    }

    static class ProjectUsage {

        // Whether a build file declares the JDBC chat memory starter.
        boolean jdbcStarter;

        // Whether anything in the project rules out the migration.
        boolean unsupported;

        boolean isMigratable() {
            return jdbcStarter && !unsupported;
        }

    }

}
