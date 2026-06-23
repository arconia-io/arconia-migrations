package io.arconia.rewrite.spring.ai2;

import java.util.Locale;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Replaces {@code OpenAiEmbeddingOptions.Builder.encodingFormat(String)} string-literal calls with the
 * matching {@code OpenAiEmbeddingOptions.EncodingFormat} enum constant introduced in Spring AI 2.0.
 */
public class UseOpenAiEmbeddingEncodingFormatEnum extends Recipe {

    private static final String FQN_OPENAI_EMBEDDING_OPTIONS = "org.springframework.ai.openai.OpenAiEmbeddingOptions";
    private static final String FQN_ENCODING_FORMAT = FQN_OPENAI_EMBEDDING_OPTIONS + ".EncodingFormat";

    private static final MethodMatcher ENCODING_FORMAT_MATCHER = new MethodMatcher(
            FQN_OPENAI_EMBEDDING_OPTIONS + "$Builder encodingFormat(java.lang.String)");

    @Override
    public String getDisplayName() {
        return "Use `OpenAiEmbeddingOptions.EncodingFormat` enum constants";
    }

    @Override
    public String getDescription() {
        return "Replace `OpenAiEmbeddingOptions.Builder.encodingFormat(String)` literal calls such as " +
                "`.encodingFormat(\"float\")` with the matching `OpenAiEmbeddingOptions.EncodingFormat` enum constant.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesMethod<>(ENCODING_FORMAT_MATCHER), new JavaVisitor<>() {
            @Override
            public J visitLiteral(J.Literal literal, ExecutionContext ctx) {
                if (!TypeUtils.isString(literal.getType()) || !(literal.getValue() instanceof String value)) {
                    return super.visitLiteral(literal, ctx);
                }
                String enumConstant = toEncodingFormatConstant(value);
                if (enumConstant == null) {
                    return super.visitLiteral(literal, ctx);
                }
                if (!(getCursor().getParentTreeCursor().getValue() instanceof J.MethodInvocation parent)
                        || !ENCODING_FORMAT_MATCHER.matches(parent)) {
                    return super.visitLiteral(literal, ctx);
                }
                maybeAddImport(FQN_OPENAI_EMBEDDING_OPTIONS);
                return JavaTemplate.builder("OpenAiEmbeddingOptions.EncodingFormat." + enumConstant)
                        .imports(FQN_OPENAI_EMBEDDING_OPTIONS)
                        .build()
                        .apply(getCursor(), literal.getCoordinates().replace())
                        .withPrefix(literal.getPrefix());
            }
        });
    }

    private static @Nullable String toEncodingFormatConstant(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "float" -> "FLOAT";
            case "base64" -> "BASE64";
            default -> null;
        };
    }

}
