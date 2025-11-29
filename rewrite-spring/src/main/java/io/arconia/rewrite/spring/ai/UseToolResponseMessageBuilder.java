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
 * Replaces `new ToolResponseMessage(...)` with builder pattern `ToolResponseMessage.builder()....build()`.
 */
public class UseToolResponseMessageBuilder extends Recipe {

    private static final String FQN_TOOL_RESPONSE_MESSAGE = "org.springframework.ai.chat.messages.ToolResponseMessage";

    private static final MethodMatcher TOOL_RESPONSE_MESSAGE_CONSTRUCTOR_MATCHER_ONE_ARG
            = new MethodMatcher("org.springframework.ai.chat.messages.ToolResponseMessage <constructor>(java.util.List)");
    private static final MethodMatcher TOOL_RESPONSE_MESSAGE_CONSTRUCTOR_MATCHER_TWO_ARGS
            = new MethodMatcher("org.springframework.ai.chat.messages.ToolResponseMessage <constructor>(java.util.List, java.util.Map)");

    @Override
    public String getDisplayName() {
        return "Use `ToolResponseMessage.Builder` instead of constructor";
    }

    @Override
    public String getDescription() {
        return "Replace `new ToolResponseMessage(...)` with `ToolResponseMessage.builder()....build()`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                Preconditions.or(
                        new UsesMethod<>(TOOL_RESPONSE_MESSAGE_CONSTRUCTOR_MATCHER_ONE_ARG),
                        new UsesMethod<>(TOOL_RESPONSE_MESSAGE_CONSTRUCTOR_MATCHER_TWO_ARGS)),
                new ToolResponseMessageVisitor()
        );
    }

    private static class ToolResponseMessageVisitor extends JavaVisitor<ExecutionContext> {

        @Override
        public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
            J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

            if (TOOL_RESPONSE_MESSAGE_CONSTRUCTOR_MATCHER_ONE_ARG.matches(nc)) {
                List<Expression> args = nc.getArguments();
                maybeAddImport(FQN_TOOL_RESPONSE_MESSAGE);
                return JavaTemplate.builder("ToolResponseMessage.builder().responses(#{any()}).build()")
                        .imports(FQN_TOOL_RESPONSE_MESSAGE)
                        .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "spring-ai-model-1.0.*"))
                        .build()
                        .apply(getCursor(), nc.getCoordinates().replace(), args.get(0));
            } else if (TOOL_RESPONSE_MESSAGE_CONSTRUCTOR_MATCHER_TWO_ARGS.matches(nc)) {
                List<Expression> args = nc.getArguments();
                maybeAddImport(FQN_TOOL_RESPONSE_MESSAGE);
                return JavaTemplate.builder("ToolResponseMessage.builder().responses(#{any()}).metadata(#{any()}).build()")
                        .imports(FQN_TOOL_RESPONSE_MESSAGE)
                        .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "spring-ai-model-1.0.*"))
                        .build()
                        .apply(getCursor(), nc.getCoordinates().replace(), args.get(0), args.get(1));
            }

            return nc;
        }

    }

}
