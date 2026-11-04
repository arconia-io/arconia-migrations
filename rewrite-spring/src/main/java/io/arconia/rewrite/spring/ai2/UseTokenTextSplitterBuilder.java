package io.arconia.rewrite.spring.ai2;

import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.J;

/**
 * Replaces the deprecated {@code new TokenTextSplitter(...)} constructors with the equivalent
 * {@code TokenTextSplitter.builder()....build()} chain. Anonymous subclasses and {@code super(...)}
 * calls are left unchanged since the builder can't express them.
 */
public class UseTokenTextSplitterBuilder extends Recipe {

    private static final String FQN_TOKEN_TEXT_SPLITTER = "org.springframework.ai.transformer.splitter.TokenTextSplitter";

    private static final List<ConstructorMigration> MIGRATIONS = List.of(
            new ConstructorMigration("", ""),
            new ConstructorMigration("boolean", ".withKeepSeparator(#{any(boolean)})"),
            new ConstructorMigration("com.knuddels.jtokkit.api.EncodingType",
                    ".withEncodingType(#{any(com.knuddels.jtokkit.api.EncodingType)})"),
            new ConstructorMigration("com.knuddels.jtokkit.api.EncodingType, boolean",
                    ".withEncodingType(#{any(com.knuddels.jtokkit.api.EncodingType)})" +
                    ".withKeepSeparator(#{any(boolean)})"),
            new ConstructorMigration("int, int, int, int, boolean, java.util.List",
                    ".withChunkSize(#{any(int)})" +
                    ".withMinChunkSizeChars(#{any(int)})" +
                    ".withMinChunkLengthToEmbed(#{any(int)})" +
                    ".withMaxNumChunks(#{any(int)})" +
                    ".withKeepSeparator(#{any(boolean)})" +
                    ".withPunctuationMarks(#{any(java.util.List)})"));

    @Override
    public String getDisplayName() {
        return "Use `TokenTextSplitter.builder()` instead of constructor";
    }

    @Override
    public String getDescription() {
        return "Replace the deprecated `new TokenTextSplitter(...)` constructors with " +
                "`TokenTextSplitter.builder()....build()`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesType<>(FQN_TOKEN_TEXT_SPLITTER, false), new JavaVisitor<>() {
            @Override
            public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);
                if (nc.getBody() != null) {
                    return nc;
                }
                for (ConstructorMigration migration : MIGRATIONS) {
                    if (migration.matcher().matches(nc)) {
                        maybeAddImport(FQN_TOKEN_TEXT_SPLITTER);
                        return JavaTemplate.builder("TokenTextSplitter.builder()" + migration.builderCalls() + ".build()")
                                .imports(FQN_TOKEN_TEXT_SPLITTER)
                                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "spring-ai-commons-2.0", "jtokkit-1.1"))
                                .build()
                                .apply(getCursor(), nc.getCoordinates().replace(),
                                        nc.getArguments().stream().filter(arg -> !(arg instanceof J.Empty)).toArray());
                    }
                }
                return nc;
            }
        });
    }

    private record ConstructorMigration(MethodMatcher matcher, String builderCalls) {

        ConstructorMigration(String parameterTypes, String builderCalls) {
            this(new MethodMatcher(FQN_TOKEN_TEXT_SPLITTER + " <constructor>(" + parameterTypes + ")"), builderCalls);
        }

    }

}
