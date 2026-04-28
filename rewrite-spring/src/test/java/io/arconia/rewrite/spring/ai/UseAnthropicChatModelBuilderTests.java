package io.arconia.rewrite.spring.ai;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

class UseAnthropicChatModelBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseAnthropicChatModelBuilder())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-anthropic-2.0.0-M2", "spring-ai-model-2.0.0-M2",
                        "spring-core-7.0", "micrometer-observation-1.16"));
    }

    @Test
    void useBuilderForFiveArgConstructor() {
        rewriteRun(
                spec -> spec.typeValidationOptions(TypeValidation.builder().methodInvocations(false).build()),
                //language=java
                java(
                        """
                        import io.micrometer.observation.ObservationRegistry;
                        import org.springframework.ai.anthropic.AnthropicChatModel;
                        import org.springframework.ai.anthropic.AnthropicChatOptions;
                        import org.springframework.ai.anthropic.api.AnthropicApi;
                        import org.springframework.ai.model.tool.ToolCallingManager;
                        import org.springframework.core.retry.RetryTemplate;

                        class Demo {
                            AnthropicChatModel build(AnthropicApi anthropicApi, AnthropicChatOptions options,
                                    ToolCallingManager toolCallingManager, RetryTemplate retryTemplate,
                                    ObservationRegistry observationRegistry) {
                                return new AnthropicChatModel(anthropicApi, options, toolCallingManager,
                                        retryTemplate, observationRegistry);
                            }
                        }
                        """,
                        """
                        import io.micrometer.observation.ObservationRegistry;
                        import org.springframework.ai.anthropic.AnthropicChatModel;
                        import org.springframework.ai.anthropic.AnthropicChatOptions;
                        import org.springframework.ai.anthropic.api.AnthropicApi;
                        import org.springframework.ai.model.tool.ToolCallingManager;
                        import org.springframework.core.retry.RetryTemplate;

                        class Demo {
                            AnthropicChatModel build(AnthropicApi anthropicApi, AnthropicChatOptions options,
                                    ToolCallingManager toolCallingManager, RetryTemplate retryTemplate,
                                    ObservationRegistry observationRegistry) {
                                return AnthropicChatModel.builder()
                                        .anthropicClient(anthropicApi)
                                        .options(options)
                                        .toolCallingManager(toolCallingManager)
                                        .observationRegistry(observationRegistry)
                                        .build();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void useBuilderForSixArgConstructor() {
        rewriteRun(
                spec -> spec.typeValidationOptions(TypeValidation.builder().methodInvocations(false).build()),
                //language=java
                java(
                        """
                        import io.micrometer.observation.ObservationRegistry;
                        import org.springframework.ai.anthropic.AnthropicChatModel;
                        import org.springframework.ai.anthropic.AnthropicChatOptions;
                        import org.springframework.ai.anthropic.api.AnthropicApi;
                        import org.springframework.ai.model.tool.ToolCallingManager;
                        import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
                        import org.springframework.core.retry.RetryTemplate;

                        class Demo {
                            AnthropicChatModel build(AnthropicApi anthropicApi, AnthropicChatOptions options,
                                    ToolCallingManager toolCallingManager, RetryTemplate retryTemplate,
                                    ObservationRegistry observationRegistry,
                                    ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate) {
                                return new AnthropicChatModel(anthropicApi, options, toolCallingManager,
                                        retryTemplate, observationRegistry, toolExecutionEligibilityPredicate);
                            }
                        }
                        """,
                        """
                        import io.micrometer.observation.ObservationRegistry;
                        import org.springframework.ai.anthropic.AnthropicChatModel;
                        import org.springframework.ai.anthropic.AnthropicChatOptions;
                        import org.springframework.ai.anthropic.api.AnthropicApi;
                        import org.springframework.ai.model.tool.ToolCallingManager;
                        import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
                        import org.springframework.core.retry.RetryTemplate;

                        class Demo {
                            AnthropicChatModel build(AnthropicApi anthropicApi, AnthropicChatOptions options,
                                    ToolCallingManager toolCallingManager, RetryTemplate retryTemplate,
                                    ObservationRegistry observationRegistry,
                                    ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate) {
                                return AnthropicChatModel.builder()
                                        .anthropicClient(anthropicApi)
                                        .options(options)
                                        .toolCallingManager(toolCallingManager)
                                        .observationRegistry(observationRegistry)
                                        .toolExecutionEligibilityPredicate(toolExecutionEligibilityPredicate)
                                        .build();
                            }
                        }
                        """
                )
        );
    }

}
