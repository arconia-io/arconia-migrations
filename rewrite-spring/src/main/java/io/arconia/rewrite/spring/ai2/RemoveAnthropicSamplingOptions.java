package io.arconia.rewrite.spring.ai2;

import java.util.List;
import java.util.Set;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Removes the {@code temperature}, {@code topP}, and {@code topK} calls from {@code AnthropicChatOptions}
 * builders. The calls are matched on the type of their receiver rather than on the type declaring the method:
 * up to Spring AI 2.0, the builder inherits these methods from a parent shared with the builders of other model
 * providers, which must keep their sampling options.
 */
public class RemoveAnthropicSamplingOptions extends Recipe {

    private static final String ANTHROPIC_CHAT_OPTIONS_FQN = "org.springframework.ai.anthropic.AnthropicChatOptions";

    private static final String BUILDER_FQN = ANTHROPIC_CHAT_OPTIONS_FQN + "$AbstractBuilder";

    private static final Set<String> SAMPLING_OPTIONS = Set.of("temperature", "topP", "topK");

    @Override
    public String getDisplayName() {
        return "Remove the sampling options from `AnthropicChatOptions` builders";
    }

    @Override
    public String getDescription() {
        return "Remove the `temperature`, `topP`, and `topK` calls from `AnthropicChatOptions` builders. Anthropic API " +
                "version `2026-10-21`, used by Spring AI 2.1, rejects these options, so Spring AI discards their values " +
                "and logs a warning. The builders of other model providers are left unchanged.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesType<>(ANTHROPIC_CHAT_OPTIONS_FQN + "*", false), new JavaVisitor<>() {
            @Override
            public J visitBlock(J.Block block, ExecutionContext ctx) {
                J.Block b = (J.Block) super.visitBlock(block, ctx);
                // What's left of a chain is a call on a builder variable, which is a statement of its own.
                List<Statement> statements = b.getStatements().stream()
                        .filter(statement -> !(statement instanceof J.MethodInvocation invocation && isSamplingOption(invocation)))
                        .toList();
                return statements.size() == b.getStatements().size() ? b : b.withStatements(statements);
            }

            @Override
            public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                J.MethodInvocation m = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);
                Expression select = m.getSelect();
                if (select == null || !isSamplingOption(m)) {
                    return m;
                }
                Object parent = getCursor().getParentTreeCursor().getValue();
                if (!(select instanceof J.MethodInvocation) && (parent instanceof J.Block || parent instanceof J.Lambda)) {
                    return m;
                }
                return select.withPrefix(m.getPrefix());
            }
        });
    }

    private static boolean isSamplingOption(J.MethodInvocation method) {
        Expression select = method.getSelect();
        return select != null && SAMPLING_OPTIONS.contains(method.getSimpleName()) && TypeUtils.isAssignableTo(BUILDER_FQN, select.getType());
    }

}
