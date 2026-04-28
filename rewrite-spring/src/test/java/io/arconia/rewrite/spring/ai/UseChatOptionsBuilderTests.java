package io.arconia.rewrite.spring.ai;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

class UseChatOptionsBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseChatOptionsBuilder())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-client-chat-2.0.0-M4", "spring-ai-model-2.0.0-M4"));
    }

    @Test
    void removeBuildFromOptionsCall() {
        rewriteRun(
                spec -> spec.typeValidationOptions(TypeValidation.builder().methodInvocations(false).build()),
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
                spec -> spec.typeValidationOptions(TypeValidation.builder().methodInvocations(false).build()),
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
                spec -> spec.typeValidationOptions(TypeValidation.builder().methodInvocations(false).build()),
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
                spec -> spec.typeValidationOptions(TypeValidation.builder().methodInvocations(false).build()),
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
