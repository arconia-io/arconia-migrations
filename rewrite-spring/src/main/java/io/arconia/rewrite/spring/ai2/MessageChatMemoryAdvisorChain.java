package io.arconia.rewrite.spring.ai2;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

/**
 * A {@code MessageChatMemoryAdvisor.builder(chatMemory)[.order(order)].build()} expression passed as
 * the only chat memory advisor to a {@code ChatClient} advisors method, the shape that
 * {@link UseSpringAiSessionAdvisors} migrates and {@link FindMigratableSpringAiChatMemory} accepts.
 *
 * @param advisorsCall the {@code defaultAdvisors(..)} or {@code advisors(..)} invocation
 * @param argumentIndex the position of the chain among the arguments of {@code advisorsCall}
 * @param builderCall the {@code MessageChatMemoryAdvisor.builder(..)} invocation that starts the chain
 * @param chatMemory the chat memory passed to the builder
 * @param order the advisor order, or {@code null} when the chain does not set one
 */
record MessageChatMemoryAdvisorChain(J.MethodInvocation advisorsCall, int argumentIndex,
        J.MethodInvocation builderCall, Expression chatMemory, @Nullable Expression order) {

    static final String ADVISOR_FQN = "org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor";

    static final MethodMatcher BUILDER =
            new MethodMatcher(ADVISOR_FQN + " builder(org.springframework.ai.chat.memory.ChatMemory)");

    private static final MethodMatcher ORDER = new MethodMatcher(ADVISOR_FQN + "$Builder order(int)");

    private static final MethodMatcher BUILD = new MethodMatcher(ADVISOR_FQN + "$Builder build()");

    private static final List<MethodMatcher> ADVISORS = List.of(
            new MethodMatcher("org.springframework.ai.chat.client.ChatClient$Builder defaultAdvisors(..)", true),
            new MethodMatcher("org.springframework.ai.chat.client.ChatClient$ChatClientRequestSpec advisors(..)", true),
            new MethodMatcher("org.springframework.ai.chat.client.ChatClient$AdvisorSpec advisors(..)", true));

    /**
     * Returns the chain passed to the given invocation, or {@code null} when the invocation is not a
     * {@code ChatClient} advisors method called on an explicit receiver with exactly one chain among
     * its arguments.
     */
    static @Nullable MessageChatMemoryAdvisorChain find(J.MethodInvocation advisorsCall) {
        if (advisorsCall.getSelect() == null || ADVISORS.stream().noneMatch(matcher -> matcher.matches(advisorsCall))) {
            return null;
        }
        MessageChatMemoryAdvisorChain found = null;
        List<Expression> arguments = advisorsCall.getArguments();
        for (int i = 0; i < arguments.size(); i++) {
            MessageChatMemoryAdvisorChain chain = parse(advisorsCall, i, arguments.get(i));
            if (chain != null) {
                if (found != null) {
                    return null;
                }
                found = chain;
            }
        }
        return found;
    }

    private static @Nullable MessageChatMemoryAdvisorChain parse(J.MethodInvocation advisorsCall, int index, Expression argument) {
        if (!(argument instanceof J.MethodInvocation build) || !BUILD.matches(build)) {
            return null;
        }
        Expression order = null;
        Expression select = build.getSelect();
        while (select instanceof J.MethodInvocation invocation && ORDER.matches(invocation)) {
            // The outermost order(..) call is the last one applied, so it wins.
            if (order == null) {
                order = invocation.getArguments().get(0);
            }
            select = invocation.getSelect();
        }
        if (select instanceof J.MethodInvocation builder && BUILDER.matches(builder)) {
            return new MessageChatMemoryAdvisorChain(advisorsCall, index, builder, builder.getArguments().get(0), order);
        }
        return null;
    }

}
