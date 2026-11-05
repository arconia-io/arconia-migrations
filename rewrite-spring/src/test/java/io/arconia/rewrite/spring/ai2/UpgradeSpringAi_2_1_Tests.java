package io.arconia.rewrite.spring.ai2;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.properties.Assertions.properties;

/**
 * Unit tests for "io.arconia.rewrite.spring.ai2.UpgradeSpringAi_2_1".
 */
class UpgradeSpringAi_2_1_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.ai2.UpgradeSpringAi_2_1")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-anthropic-2\\.0\\.[1-9]", "spring-ai-commons-2\\.0\\.[1-9]", "spring-ai-deepseek-2.0", "spring-ai-mcp-annotations-2.0", "spring-ai-model-2\\.0\\.[1-9]"));
    }

    @Test
    @DocumentExample
    void mcpToolMethodCallbacksWithoutToolCallExceptionClass() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.lang.reflect.Method;

                        import org.springframework.ai.mcp.annotation.method.tool.AsyncMcpToolMethodCallback;
                        import org.springframework.ai.mcp.annotation.method.tool.ReturnMode;
                        import org.springframework.ai.mcp.annotation.method.tool.SyncMcpToolMethodCallback;

                        class Demo {
                            SyncMcpToolMethodCallback sync(Method method, Object provider) {
                                return new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider, Exception.class);
                            }

                            AsyncMcpToolMethodCallback async(Method method, Object provider) {
                                return new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider, RuntimeException.class);
                            }
                        }
                        """,
                        """
                        import java.lang.reflect.Method;

                        import org.springframework.ai.mcp.annotation.method.tool.AsyncMcpToolMethodCallback;
                        import org.springframework.ai.mcp.annotation.method.tool.ReturnMode;
                        import org.springframework.ai.mcp.annotation.method.tool.SyncMcpToolMethodCallback;

                        class Demo {
                            SyncMcpToolMethodCallback sync(Method method, Object provider) {
                                return new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);
                            }

                            AsyncMcpToolMethodCallback async(Method method, Object provider) {
                                return new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);
                            }
                        }
                        """
                )
        );
    }

    @Test
    void anthropicSamplingOptions() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.ai.anthropic.AnthropicChatOptions;

                        class Demo {
                            AnthropicChatOptions options() {
                                return AnthropicChatOptions.builder()
                                        .temperature(0.7)
                                        .topP(0.9)
                                        .topK(40)
                                        .maxTokens(1000)
                                        .build();
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.anthropic.AnthropicChatOptions;

                        class Demo {
                            AnthropicChatOptions options() {
                                return AnthropicChatOptions.builder()
                                        .maxTokens(1000)
                                        .build();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void anthropicSamplingOptionsOnBuilderVariable() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.ai.anthropic.AnthropicChatOptions;

                        class Demo {
                            AnthropicChatOptions options(AnthropicChatOptions.Builder builder) {
                                builder.temperature(0.7);
                                builder.maxTokens(1000).topP(0.9);
                                return builder.build();
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.anthropic.AnthropicChatOptions;

                        class Demo {
                            AnthropicChatOptions options(AnthropicChatOptions.Builder builder) {
                                builder.maxTokens(1000);
                                return builder.build();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void samplingOptionsOfOtherProvidersAreKept() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.ai.anthropic.AnthropicChatOptions;
                        import org.springframework.ai.chat.prompt.ChatOptions;

                        class Demo {
                            ChatOptions generic() {
                                return ChatOptions.builder().temperature(0.7).topP(0.9).topK(40).build();
                            }

                            AnthropicChatOptions anthropic() {
                                return AnthropicChatOptions.builder().maxTokens(1000).build();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void deepSeekPrefixAssistantMessage() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.ai.chat.messages.Message;
                        import org.springframework.ai.deepseek.DeepSeekAssistantMessage;

                        class Demo {
                            Message prefix(String content) {
                                return DeepSeekAssistantMessage.prefixAssistantMessage(content);
                            }

                            DeepSeekAssistantMessage prefixWithReasoning(String reasoning) {
                                return DeepSeekAssistantMessage.prefixAssistantMessage("```python", reasoning);
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.chat.messages.Message;
                        import org.springframework.ai.deepseek.DeepSeekAssistantMessage;

                        class Demo {
                            Message prefix(String content) {
                                return DeepSeekAssistantMessage.builder().content(content).prefix(true).build();
                            }

                            DeepSeekAssistantMessage prefixWithReasoning(String reasoning) {
                                return DeepSeekAssistantMessage.builder().content("```python").prefix(true).reasoningContent(reasoning).build();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void removedMcpServerProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.ai.mcp.server.sse-endpoint=/sse
                        """,
                        """
                        # Deprecated property removed in Spring AI 2.1.
                        # spring.ai.mcp.server.sse-endpoint=/sse
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

}
