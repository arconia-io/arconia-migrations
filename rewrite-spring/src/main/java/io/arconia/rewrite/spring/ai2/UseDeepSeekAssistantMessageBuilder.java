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
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

/**
 * Replaces the {@code DeepSeekAssistantMessage.prefixAssistantMessage} factory methods, removed in Spring AI 2.1,
 * with the equivalent {@code DeepSeekAssistantMessage.builder()} chain, which returns the same type wherever the
 * factory method was used.
 */
public class UseDeepSeekAssistantMessageBuilder extends Recipe {

    private static final String MESSAGE_FQN = "org.springframework.ai.deepseek.DeepSeekAssistantMessage";

    private static final MethodMatcher PREFIX_ASSISTANT_MESSAGE = new MethodMatcher(MESSAGE_FQN + " prefixAssistantMessage(..)");

    @Override
    public String getDisplayName() {
        return "Use the `DeepSeekAssistantMessage` builder instead of `prefixAssistantMessage`";
    }

    @Override
    public String getDescription() {
        return "Replace the `DeepSeekAssistantMessage.prefixAssistantMessage` factory methods, removed in Spring AI 2.1, " +
                "with the equivalent `DeepSeekAssistantMessage.builder()` chain.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesMethod<>(PREFIX_ASSISTANT_MESSAGE), new JavaVisitor<>() {
            @Override
            public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                J.MethodInvocation m = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);
                if (!PREFIX_ASSISTANT_MESSAGE.matches(m)) {
                    return m;
                }
                List<Expression> arguments = m.getArguments();
                String builder = "DeepSeekAssistantMessage.builder().content(#{any(java.lang.String)}).prefix(true)" +
                        (arguments.size() == 2 ? ".reasoningContent(#{any(java.lang.String)})" : "") +
                        ".build()";
                maybeAddImport(MESSAGE_FQN);
                return JavaTemplate.builder(builder)
                        .imports(MESSAGE_FQN)
                        .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx,
                                "spring-ai-deepseek-2.1", "spring-ai-model-2.1", "spring-ai-commons-2.1"))
                        .build()
                        .apply(getCursor(), m.getCoordinates().replace(), arguments.toArray());
            }
        });
    }

}
