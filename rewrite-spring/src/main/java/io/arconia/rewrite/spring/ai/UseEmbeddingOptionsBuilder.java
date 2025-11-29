package io.arconia.rewrite.spring.ai;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.J;

/**
 * Replaces `EmbeddingOptionsBuilder.builder().build()` with `EmbeddingOptions.builder().build()`.
 */
public class UseEmbeddingOptionsBuilder extends Recipe {

    private static final String FQN_EMBEDDING_OPTIONS = "org.springframework.ai.embedding.EmbeddingOptions";
    private static final String FQN_EMBEDDING_OPTIONS_BUILDER = "org.springframework.ai.embedding.EmbeddingOptionsBuilder";

    private static final MethodMatcher EMBEDDING_OPTIONS_BUILDER_MATCHER
            = new MethodMatcher("org.springframework.ai.embedding.EmbeddingOptionsBuilder builder()");

    @Override
    public String getDisplayName() {
        return "Use `EmbeddingOptions.builder()` instead of `EmbeddingOptionsBuilder.builder()`";
    }

    @Override
    public String getDescription() {
        return "Replace `EmbeddingOptionsBuilder.builder()` with `EmbeddingOptions.builder()`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new UsesMethod<>(EMBEDDING_OPTIONS_BUILDER_MATCHER),
                new EmbeddingOptionsBuilderVisitor()
        );
    }

    private static class EmbeddingOptionsBuilderVisitor extends JavaVisitor<ExecutionContext> {

        @Override
        public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
            J.MethodInvocation mi = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);

            if (!EMBEDDING_OPTIONS_BUILDER_MATCHER.matches(mi)) {
                return mi;
            }

            maybeRemoveImport(FQN_EMBEDDING_OPTIONS_BUILDER);
            maybeAddImport(FQN_EMBEDDING_OPTIONS);

            return JavaTemplate.builder("EmbeddingOptions.builder()")
                    .imports(FQN_EMBEDDING_OPTIONS)
                    .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "spring-ai-model-1.0.*"))
                    .build()
                    .apply(getCursor(), mi.getCoordinates().replace());
        }

    }

}
