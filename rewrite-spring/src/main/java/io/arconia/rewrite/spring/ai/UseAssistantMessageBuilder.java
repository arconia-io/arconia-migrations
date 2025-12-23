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
 * Replaces `new AssistantMessage(...)` with builder pattern `AssistantMessage.builder()....build()`.
 */
public class UseAssistantMessageBuilder extends Recipe {

    private static final String FQN_ASSISTANT_MESSAGE = "org.springframework.ai.chat.messages.AssistantMessage";

    private static final MethodMatcher ASSISTANT_MESSAGE_CONSTRUCTOR_MATCHER_ONE_ARG
            = new MethodMatcher("org.springframework.ai.chat.messages.AssistantMessage <constructor>(java.lang.String)");
    private static final MethodMatcher ASSISTANT_MESSAGE_CONSTRUCTOR_MATCHER_TWO_ARGS
            = new MethodMatcher("org.springframework.ai.chat.messages.AssistantMessage <constructor>(java.lang.String, java.util.Map)");
    private static final MethodMatcher ASSISTANT_MESSAGE_CONSTRUCTOR_MATCHER_THREE_ARGS
            = new MethodMatcher("org.springframework.ai.chat.messages.AssistantMessage <constructor>(java.lang.String, java.util.Map, java.util.List)");
    private static final MethodMatcher ASSISTANT_MESSAGE_CONSTRUCTOR_MATCHER_FOUR_ARGS
            = new MethodMatcher("org.springframework.ai.chat.messages.AssistantMessage <constructor>(java.lang.String, java.util.Map, java.util.List, java.util.List)");

    @Override
    public String getDisplayName() {
        return "Use `AssistantMessage.Builder` instead of constructor";
    }

    @Override
    public String getDescription() {
        return "Replace `new AssistantMessage(...)` with `AssistantMessage.builder()....build()`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                Preconditions.or(
                        new UsesMethod<>(ASSISTANT_MESSAGE_CONSTRUCTOR_MATCHER_ONE_ARG),
                        new UsesMethod<>(ASSISTANT_MESSAGE_CONSTRUCTOR_MATCHER_TWO_ARGS),
                        new UsesMethod<>(ASSISTANT_MESSAGE_CONSTRUCTOR_MATCHER_THREE_ARGS),
                        new UsesMethod<>(ASSISTANT_MESSAGE_CONSTRUCTOR_MATCHER_FOUR_ARGS)),
                new JavaVisitor<>() {
                    @Override
                    public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                        J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

                        if (ASSISTANT_MESSAGE_CONSTRUCTOR_MATCHER_ONE_ARG.matches(nc)) {
                            List<Expression> args = nc.getArguments();
                            maybeAddImport(FQN_ASSISTANT_MESSAGE);
                            return JavaTemplate.builder("AssistantMessage.builder().content(#{any(java.lang.String)}).build()")
                                    .imports(FQN_ASSISTANT_MESSAGE)
                                    .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "spring-ai-model-1.0"))
                                    .build()
                                    .apply(getCursor(), nc.getCoordinates().replace(), args.get(0));
                        }
                        if (ASSISTANT_MESSAGE_CONSTRUCTOR_MATCHER_TWO_ARGS.matches(nc)) {
                            List<Expression> args = nc.getArguments();
                            maybeAddImport(FQN_ASSISTANT_MESSAGE);
                            return JavaTemplate.builder("AssistantMessage.builder().content(#{any(java.lang.String)}).properties(#{any()}).build()")
                                    .imports(FQN_ASSISTANT_MESSAGE)
                                    .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "spring-ai-model-1.0"))
                                    .build()
                                    .apply(getCursor(), nc.getCoordinates().replace(), args.get(0), args.get(1));
                        }
                        if (ASSISTANT_MESSAGE_CONSTRUCTOR_MATCHER_THREE_ARGS.matches(nc)) {
                            List<Expression> args = nc.getArguments();
                            maybeAddImport(FQN_ASSISTANT_MESSAGE);
                            return JavaTemplate.builder("AssistantMessage.builder().content(#{any(java.lang.String)}).properties(#{any()}).toolCalls(#{any()}).build()")
                                    .imports(FQN_ASSISTANT_MESSAGE)
                                    .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "spring-ai-model-1.0"))
                                    .build()
                                    .apply(getCursor(), nc.getCoordinates().replace(), args.get(0), args.get(1), args.get(2));
                        }
                        if (ASSISTANT_MESSAGE_CONSTRUCTOR_MATCHER_FOUR_ARGS.matches(nc)) {
                            List<Expression> args = nc.getArguments();
                            maybeAddImport(FQN_ASSISTANT_MESSAGE);
                            return JavaTemplate.builder("AssistantMessage.builder().content(#{any(java.lang.String)}).properties(#{any()}).toolCalls(#{any()}).media(#{any()}).build()")
                                    .imports(FQN_ASSISTANT_MESSAGE)
                                    .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "spring-ai-model-1.0"))
                                    .build()
                                    .apply(getCursor(), nc.getCoordinates().replace(), args.get(0), args.get(1), args.get(2), args.get(3));
                        }
                        return nc;
                    }
                }
        );
    }

}
