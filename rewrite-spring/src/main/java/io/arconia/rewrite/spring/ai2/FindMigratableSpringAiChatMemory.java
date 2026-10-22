package io.arconia.rewrite.spring.ai2;

import java.util.List;

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
                sourceFile.getMarkers().findFirst(MavenResolutionResult.class).ifPresent(result ->
                        result.getPom().getRequestedDependencies().forEach(dependency -> recordDependency(acc, dependency)));
                sourceFile.getMarkers().findFirst(GradleProject.class).ifPresent(project -> {
                    for (GradleDependencyConfiguration configuration : project.getConfigurations()) {
                        configuration.getRequested().forEach(dependency -> recordDependency(acc, dependency));
                    }
                });
                if (sourceFile instanceof JavaSourceFile && !acc.unsupported) {
                    new UsageScanner().visit(sourceFile, acc);
                }
                return tree;
            }
        };
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor(Accumulator acc) {
        if (!acc.jdbcStarter || acc.unsupported) {
            return TreeVisitor.noop();
        }
        return new TreeVisitor<>() {
            @Override
            public @Nullable Tree visit(@Nullable Tree tree, ExecutionContext ctx) {
                if (tree instanceof SourceFile sourceFile) {
                    return sourceFile.withMarkers(sourceFile.getMarkers().addIfAbsent(
                            new SearchResult(Tree.randomId(), "Spring AI chat memory can be migrated to session memory")));
                }
                return tree;
            }
        };
    }

    private static void recordDependency(Accumulator acc, Dependency dependency) {
        if (!SPRING_AI_GROUP_ID.equals(dependency.getGroupId())) {
            return;
        }
        String artifactId = dependency.getArtifactId();
        if (JDBC_STARTER.equals(artifactId)) {
            acc.jdbcStarter = true;
        }
        else if (artifactId.contains("chat-memory")) {
            acc.unsupported = true;
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

    private static class UsageScanner extends JavaIsoVisitor<Accumulator> {

        private int advisorBuilders;

        private int migratableChains;

        @Override
        public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, Accumulator acc) {
            advisorBuilders = 0;
            migratableChains = 0;
            J.CompilationUnit c = super.visitCompilationUnit(cu, acc);
            if (advisorBuilders != migratableChains) {
                acc.unsupported = true;
            }
            return c;
        }

        @Override
        public J.Import visitImport(J.Import anImport, Accumulator acc) {
            return anImport;
        }

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, Accumulator acc) {
            JavaType.FullyQualified type = classDecl.getType();
            if (type != null && (TypeUtils.isAssignableTo(CHAT_MEMORY_FQN, type) || TypeUtils.isAssignableTo(CHAT_MEMORY_REPOSITORY_FQN, type))) {
                acc.unsupported = true;
            }
            return super.visitClassDeclaration(classDecl, acc);
        }

        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, Accumulator acc) {
            if (MessageChatMemoryAdvisorChain.BUILDER.matches(method)) {
                advisorBuilders++;
            }
            else if (CHAT_MEMORY_ADD.matches(method) || takesChatMemory(method.getMethodType())) {
                acc.unsupported = true;
            }
            if (MessageChatMemoryAdvisorChain.find(method) != null) {
                migratableChains++;
            }
            return super.visitMethodInvocation(method, acc);
        }

        @Override
        public J.NewClass visitNewClass(J.NewClass newClass, Accumulator acc) {
            if (takesChatMemory(newClass.getConstructorType())) {
                acc.unsupported = true;
            }
            return super.visitNewClass(newClass, acc);
        }

        @Override
        public J.Identifier visitIdentifier(J.Identifier identifier, Accumulator acc) {
            Object parent = getCursor().getParentTreeCursor().getValue();
            boolean methodName = parent instanceof J.MethodInvocation invocation && invocation.getName() == identifier;
            boolean builderReceiver = parent instanceof J.MethodInvocation invocation && invocation.getSelect() == identifier
                    && MessageChatMemoryAdvisorChain.BUILDER.matches(invocation);
            if (!methodName && !builderReceiver && isUnsupportedType(identifier.getType())) {
                acc.unsupported = true;
            }
            return super.visitIdentifier(identifier, acc);
        }

        @Override
        public J.FieldAccess visitFieldAccess(J.FieldAccess fieldAccess, Accumulator acc) {
            if (isUnsupportedType(fieldAccess.getType())) {
                acc.unsupported = true;
            }
            return super.visitFieldAccess(fieldAccess, acc);
        }

    }

    public static class Accumulator {

        // Whether a build file declares the JDBC chat memory starter.
        boolean jdbcStarter;

        // Whether anything in the project rules out the migration.
        boolean unsupported;

    }

}
