package io.arconia.rewrite.spring.ai2;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link UseChatOptionsBuilder}.
 */
class UseChatOptionsBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseChatOptionsBuilder())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-client-chat-2.0.0-M4", "spring-ai-model-2.0.0-M4"));
    }

    @Test
    @DocumentExample
    void removeBuildFromOptionsCall() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.ai.chat.client.ChatClient;
                        import org.springframework.ai.chat.prompt.ChatOptions;

                        class Demo {
                            void test(ChatClient chatClient) {
                                chatClient.prompt()
                                        .options(ChatOptions.builder()
                                                .temperature(0.2)
                                                .build())
                                        .call()
                                        .content();
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.chat.client.ChatClient;
                        import org.springframework.ai.chat.prompt.ChatOptions;

                        class Demo {
                            void test(ChatClient chatClient) {
                                chatClient.prompt()
                                        .options(ChatOptions.builder()
                                                .temperature(0.2))
                                        .call()
                                        .content();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void removeBuildFromDefaultOptionsCall() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.ai.chat.client.ChatClient;
                        import org.springframework.ai.chat.prompt.ChatOptions;

                        class Demo {
                            ChatClient build(ChatClient.Builder builder) {
                                return builder
                                        .defaultOptions(ChatOptions.builder()
                                                .temperature(0.0)
                                                .build())
                                        .build();
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.chat.client.ChatClient;
                        import org.springframework.ai.chat.prompt.ChatOptions;

                        class Demo {
                            ChatClient build(ChatClient.Builder builder) {
                                return builder
                                        .defaultOptions(ChatOptions.builder()
                                                .temperature(0.0))
                                        .build();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void removeBuildFromOptionsCallWithToolCallingChatOptions() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.ai.chat.client.ChatClient;
                        import org.springframework.ai.model.tool.ToolCallingChatOptions;

                        class Demo {
                            void test(ChatClient chatClient) {
                                chatClient.prompt()
                                        .options(ToolCallingChatOptions.builder()
                                                .temperature(0.2)
                                                .build())
                                        .call()
                                        .content();
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.chat.client.ChatClient;
                        import org.springframework.ai.model.tool.ToolCallingChatOptions;

                        class Demo {
                            void test(ChatClient chatClient) {
                                chatClient.prompt()
                                        .options(ToolCallingChatOptions.builder()
                                                .temperature(0.2))
                                        .call()
                                        .content();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void removeBuildFromDefaultOptionsCallWithToolCallingChatOptions() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.ai.chat.client.ChatClient;
                        import org.springframework.ai.model.tool.ToolCallingChatOptions;

                        class Demo {
                            ChatClient build(ChatClient.Builder builder) {
                                return builder
                                        .defaultOptions(ToolCallingChatOptions.builder()
                                                .temperature(0.0)
                                                .build())
                                        .build();
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.chat.client.ChatClient;
                        import org.springframework.ai.model.tool.ToolCallingChatOptions;

                        class Demo {
                            ChatClient build(ChatClient.Builder builder) {
                                return builder
                                        .defaultOptions(ToolCallingChatOptions.builder()
                                                .temperature(0.0))
                                        .build();
                            }
                        }
                        """
                )
        );
    }

}
