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
 * Replaces {@code new AnthropicChatModel(...)} constructors with
 * {@code AnthropicChatModel.builder()...build()}.
 *
 * <p>The {@code RetryTemplate} parameter is dropped as it is no longer part of the API.
 * The {@code AnthropicApi} parameter is passed to {@code .anthropicClient()} and will
 * require a separate migration from {@code AnthropicApi} to the official Anthropic SDK's
 * {@code com.anthropic.client.AnthropicClient}.
 */
public class UseAnthropicChatModelBuilder extends Recipe {

    private static final String FQN_ANTHROPIC_CHAT_MODEL = "org.springframework.ai.anthropic.AnthropicChatModel";

    private static final MethodMatcher FIVE_ARG_MATCHER = new MethodMatcher(
            "org.springframework.ai.anthropic.AnthropicChatModel <constructor>("
            + "org.springframework.ai.anthropic.api.AnthropicApi, "
            + "org.springframework.ai.anthropic.AnthropicChatOptions, "
            + "org.springframework.ai.model.tool.ToolCallingManager, "
            + "org.springframework.core.retry.RetryTemplate, "
            + "io.micrometer.observation.ObservationRegistry)");

    private static final MethodMatcher SIX_ARG_MATCHER = new MethodMatcher(
            "org.springframework.ai.anthropic.AnthropicChatModel <constructor>("
            + "org.springframework.ai.anthropic.api.AnthropicApi, "
            + "org.springframework.ai.anthropic.AnthropicChatOptions, "
            + "org.springframework.ai.model.tool.ToolCallingManager, "
            + "org.springframework.core.retry.RetryTemplate, "
            + "io.micrometer.observation.ObservationRegistry, "
            + "org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate)");

    @Override
    public String getDisplayName() {
        return "Use `AnthropicChatModel.Builder` instead of constructor";
    }

    @Override
    public String getDescription() {
        return "Replace `new AnthropicChatModel(...)` with `AnthropicChatModel.builder()...build()`."
                + " The `RetryTemplate` parameter is dropped as it is no longer part of the API.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                Preconditions.or(
                        new UsesMethod<>(FIVE_ARG_MATCHER),
                        new UsesMethod<>(SIX_ARG_MATCHER)),
                new JavaVisitor<>() {
                    @Override
                    public J visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                        J.NewClass nc = (J.NewClass) super.visitNewClass(newClass, ctx);

                        if (!FIVE_ARG_MATCHER.matches(nc) && !SIX_ARG_MATCHER.matches(nc)) {
                            return nc;
                        }

                        List<Expression> args = nc.getArguments();
                        maybeAddImport(FQN_ANTHROPIC_CHAT_MODEL);

                        if (FIVE_ARG_MATCHER.matches(nc)) {
                            // args: [anthropicApi, defaultOptions, toolCallingManager, retryTemplate, observationRegistry]
                            // retryTemplate (index 3) is dropped
                            return JavaTemplate.builder("""
                                    AnthropicChatModel.builder()
                                            .anthropicClient(#{any()})
                                            .options(#{any()})
                                            .toolCallingManager(#{any()})
                                            .observationRegistry(#{any()})
                                            .build()""")
                                    .imports(FQN_ANTHROPIC_CHAT_MODEL)
                                    .javaParser(JavaParser.fromJavaVersion()
                                            .classpathFromResources(ctx, "spring-ai-anthropic-2.0.0-M4"))
                                    .build()
                                    .apply(getCursor(), nc.getCoordinates().replace(),
                                            args.get(0), args.get(1), args.get(2), args.get(4));
                        }

                        // Six-arg: [anthropicApi, defaultOptions, toolCallingManager, retryTemplate, observationRegistry, toolExecutionEligibilityPredicate]
                        // retryTemplate (index 3) is dropped
                        return JavaTemplate.builder("""
                                AnthropicChatModel.builder()
                                        .anthropicClient(#{any()})
                                        .options(#{any()})
                                        .toolCallingManager(#{any()})
                                        .observationRegistry(#{any()})
                                        .toolExecutionEligibilityPredicate(#{any()})
                                        .build()""")
                                .imports(FQN_ANTHROPIC_CHAT_MODEL)
                                .javaParser(JavaParser.fromJavaVersion()
                                        .classpathFromResources(ctx, "spring-ai-anthropic-2.0.0-M4"))
                                .build()
                                .apply(getCursor(), nc.getCoordinates().replace(),
                                        args.get(0), args.get(1), args.get(2), args.get(4), args.get(5));
                    }
                }
        );
    }

}
