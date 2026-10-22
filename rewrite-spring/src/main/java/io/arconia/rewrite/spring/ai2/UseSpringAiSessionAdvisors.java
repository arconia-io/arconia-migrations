package io.arconia.rewrite.spring.ai2;

import java.util.ArrayList;
import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JRightPadded;
import org.openrewrite.java.tree.JavaType;

/**
 * Replaces a {@code MessageChatMemoryAdvisor} passed to a {@code ChatClient} advisors method with the
 * session advisors built by {@code SessionAdvisors}. The session memory advisor reads the last 20
 * events, the window of the auto-configured {@code MessageWindowChatMemory}, and keeps the order set
 * on the chat memory advisor. Since the session advisors come as a list, the other advisors passed
 * in the same call stay there and the session advisors move to a call of their own; advisors run
 * by their order, not by the order they were added in.
 *
 * <p>The chat memory passed to the builder is expected to become a {@code SessionService}, which
 * {@code MigrateSpringAiChatMemoryToSessionMemory} does by changing the {@code ChatMemory} type.
 */
public class UseSpringAiSessionAdvisors extends Recipe {

    private static final String SESSION_ADVISORS_FQN = "org.springframework.ai.session.advisor.SessionAdvisors";

    private static final String EVENT_FILTER_FQN = "org.springframework.ai.session.EventFilter";

    private static final int DEFAULT_MAX_MESSAGES = 20;

    @Override
    public String getDisplayName() {
        return "Use the Spring AI session advisors instead of `MessageChatMemoryAdvisor`";
    }

    @Override
    public String getDescription() {
        return "Replace a `MessageChatMemoryAdvisor` passed to a `ChatClient` advisors method with the advisors built by " +
                "`SessionAdvisors`, reading the same 20-message window as the auto-configured chat memory and keeping the " +
                "advisor order. Other advisors passed in the same call stay in place, and the session advisors move to a " +
                "call of their own.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesMethod<>(MessageChatMemoryAdvisorChain.BUILDER), new JavaVisitor<>() {
            @Override
            public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                J.MethodInvocation m = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);
                MessageChatMemoryAdvisorChain chain = MessageChatMemoryAdvisorChain.find(m);
                JavaType.Method methodType = m.getMethodType();
                if (chain == null || methodType == null) {
                    return m;
                }

                String sessionAdvisors = "SessionAdvisors.builder(#{any(org.springframework.ai.session.SessionService)})" +
                        ".memory(memory -> memory.eventFilter(EventFilter.lastN(" + DEFAULT_MAX_MESSAGES + "))" +
                        (chain.order() != null ? ".order(#{any(int)})" : "") +
                        ").build()";
                List<Object> parameters = new ArrayList<>();
                parameters.add(chain.chatMemory());
                if (chain.order() != null) {
                    parameters.add(chain.order());
                }

                maybeAddImport(SESSION_ADVISORS_FQN);
                maybeAddImport(EVENT_FILTER_FQN);
                maybeRemoveImport(MessageChatMemoryAdvisorChain.ADVISOR_FQN);

                if (m.getArguments().size() == 1) {
                    return template(sessionAdvisors, ctx)
                            .apply(getCursor(), m.getCoordinates().replaceArguments(), parameters.toArray());
                }

                // Keep the other advisors in the original call, and add the session advisors with a call of their own.
                List<Expression> otherAdvisors = new ArrayList<>(m.getArguments());
                Expression removed = otherAdvisors.remove(chain.argumentIndex());
                if (chain.argumentIndex() == 0) {
                    otherAdvisors.set(0, otherAdvisors.get(0).withPrefix(removed.getPrefix()));
                }
                J.MethodInvocation withOtherAdvisors = m.withArguments(otherAdvisors);
                parameters.add(0, withOtherAdvisors);
                String receiverType = methodType.getDeclaringType().getFullyQualifiedName().replace('$', '.');
                J.MethodInvocation updated = template("#{any(" + receiverType + ")}." + m.getSimpleName() + "(" + sessionAdvisors + ")", ctx)
                        .apply(getCursor(), m.getCoordinates().replace(), parameters.toArray());
                // Break the line before the new call the same way the original call was broken.
                JRightPadded<Expression> select = updated.getPadding().getSelect();
                JRightPadded<Expression> originalSelect = m.getPadding().getSelect();
                if (select != null && originalSelect != null) {
                    updated = updated.getPadding().withSelect(select.withAfter(originalSelect.getAfter()));
                }
                return updated;
            }
        });
    }

    private static JavaTemplate template(String code, ExecutionContext ctx) {
        return JavaTemplate.builder(code)
                .imports(SESSION_ADVISORS_FQN, EVENT_FILTER_FQN)
                .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx,
                        "spring-ai-session-advisor-2.1", "spring-ai-session-2.1", "spring-ai-client-chat-2.1", "spring-ai-model-2.1", "spring-core-7.0"))
                .build();
    }

}
