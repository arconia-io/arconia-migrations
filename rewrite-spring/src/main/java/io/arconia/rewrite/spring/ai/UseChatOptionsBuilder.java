package io.arconia.rewrite.spring.ai;

import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Removes `.build()` from ChatOptions builder chains passed to ChatClient options methods,
 * since Spring AI 2.0 changed these methods to accept {@code ChatOptions.Builder} directly.
 */
public class UseChatOptionsBuilder extends Recipe {

    private static final String FQN_CHAT_OPTIONS = "org.springframework.ai.chat.prompt.ChatOptions";

    private static final MethodMatcher OPTIONS_MATCHER =
            new MethodMatcher("org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec options(..)", true);

    private static final MethodMatcher DEFAULT_OPTIONS_MATCHER =
            new MethodMatcher("org.springframework.ai.chat.client.ChatClient.Builder defaultOptions(..)", true);

    @Override
    public String getDisplayName() {
        return "Pass `ChatOptions.Builder` to `ChatClient` options methods";
    }

    @Override
    public String getDescription() {
        return "Remove `.build()` from `ChatOptions` builder chains passed to `ChatClient.options()` and `ChatClient.Builder.defaultOptions()`,"
                + " as these methods now accept `ChatOptions.Builder` directly in Spring AI 2.0.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                Preconditions.or(
                        new UsesMethod<>(OPTIONS_MATCHER),
                        new UsesMethod<>(DEFAULT_OPTIONS_MATCHER)),
                new JavaVisitor<>() {
                    @Override
                    public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                        J.MethodInvocation mi = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);

                        if (!OPTIONS_MATCHER.matches(mi) && !DEFAULT_OPTIONS_MATCHER.matches(mi)) {
                            return mi;
                        }

                        List<Expression> args = mi.getArguments();
                        if (args.size() != 1) {
                            return mi;
                        }

                        Expression arg = args.get(0);
                        if (!(arg instanceof J.MethodInvocation buildCall)) {
                            return mi;
                        }

                        if (!"build".equals(buildCall.getSimpleName()) || buildCall.getSelect() == null) {
                            return mi;
                        }

                        if (!TypeUtils.isAssignableTo(FQN_CHAT_OPTIONS, buildCall.getType())) {
                            return mi;
                        }

                        return mi.withArguments(List.of(buildCall.getSelect().withPrefix(arg.getPrefix())));
                    }
                }
        );
    }

}
