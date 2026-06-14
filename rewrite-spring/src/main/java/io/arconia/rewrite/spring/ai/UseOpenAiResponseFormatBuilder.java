package io.arconia.rewrite.spring.ai;

import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

/**
 * Replaces {@code new ResponseFormat(type, jsonSchema)} with
 * {@code OpenAiChatModel.ResponseFormat.builder().type(type).jsonSchema(jsonSchema).build()}.
 * Targets the legacy {@code org.springframework.ai.openai.api.ResponseFormat} so it can run
 * before the {@code ChangeType} that relocates the class onto {@code OpenAiChatModel.ResponseFormat}.
 */
public class UseOpenAiResponseFormatBuilder extends Recipe {

    private static final String FQN_NEW_RESPONSE_FORMAT = "org.springframework.ai.openai.OpenAiChatModel.ResponseFormat";
    private static final String FQN_NEW_RESPONSE_FORMAT_INTERNAL = "org.springframework.ai.openai.OpenAiChatModel$ResponseFormat";

    private static final MethodMatcher CONSTRUCTOR_MATCHER = new MethodMatcher(
            FQN_NEW_RESPONSE_FORMAT_INTERNAL + " <constructor>(" + FQN_NEW_RESPONSE_FORMAT_INTERNAL + "$Type, java.lang.String)");

    @Override
    public String getDisplayName() {
        return "Use `OpenAiChatModel.ResponseFormat.builder(...)` instead of constructor";
    }

    @Override
    public String getDescription() {
        return "Replace `new ResponseFormat(type, jsonSchema)` with " +
                "`OpenAiChatModel.ResponseFormat.builder().type(type).jsonSchema(jsonSchema).build()`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesMethod<>(CONSTRUCTOR_MATCHER), new JavaVisitor<>() {
            @Override
            public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);
                if (!CONSTRUCTOR_MATCHER.matches(nc)) {
                    return nc;
                }
                List<Expression> args = nc.getArguments();
                maybeAddImport(FQN_NEW_RESPONSE_FORMAT);
                return JavaTemplate.builder(
                                "ResponseFormat.builder()" +
                                ".type(#{any()})" +
                                ".jsonSchema(#{any(java.lang.String)})" +
                                ".build()")
                        .imports(FQN_NEW_RESPONSE_FORMAT)
                        .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "spring-ai-openai-2.0"))
                        .build()
                        .apply(getCursor(), nc.getCoordinates().replace(), args.get(0), args.get(1));
            }
        });
    }

}
