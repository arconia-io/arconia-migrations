package io.arconia.rewrite.spring.ai;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.java.Assertions.srcMainJava;

/**
 * Tests for "io.arconia.rewrite.spring.ai.UpgradeSpringAi_1_0".
 */
public class UpgradeSpringAi_1_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.ai.UpgradeSpringAi_1_0")
            .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "spring-ai-core-1.0.0-M6"));
    }

    // Package Name Changes

    @Test
    void keywordMetadataEnricherM7() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.ai.transformer.KeywordMetadataEnricher;

                        class Demo {
                            KeywordMetadataEnricher enricher = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import org.springframework.ai.model.transformer.KeywordMetadataEnricher;

                        class Demo {
                            KeywordMetadataEnricher enricher = null;
                        }
                        """
                )
        );
    }

    @Test
    void keywordMetadataEnricherM8() {
        rewriteRun(r -> r.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "spring-ai-client-chat-1.0.0-M7")),
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.ai.chat.transformer.KeywordMetadataEnricher;

                        class Demo {
                            KeywordMetadataEnricher enricher = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import org.springframework.ai.model.transformer.KeywordMetadataEnricher;

                        class Demo {
                            KeywordMetadataEnricher enricher = null;
                        }
                        """
                )
        );
    }

    @Test
    void summaryMetadataEnricherM7() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.ai.transformer.SummaryMetadataEnricher;

                        class Demo {
                            SummaryMetadataEnricher enricher = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import org.springframework.ai.model.transformer.SummaryMetadataEnricher;

                        class Demo {
                            SummaryMetadataEnricher enricher = null;
                        }
                        """
                )
        );
    }

    @Test
    void summaryMetadataEnricherM8() {
        rewriteRun(r -> r.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "spring-ai-client-chat-1.0.0-M7")),
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.ai.chat.transformer.SummaryMetadataEnricher;

                        class Demo {
                            SummaryMetadataEnricher enricher = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import org.springframework.ai.model.transformer.SummaryMetadataEnricher;

                        class Demo {
                            SummaryMetadataEnricher enricher = null;
                        }
                        """
                )
        );
    }

    @Test
    void factCheckingEvaluator() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.ai.evaluation.FactCheckingEvaluator;

                        class Demo {
                            FactCheckingEvaluator factCheckingEvaluator = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;

                        class Demo {
                            FactCheckingEvaluator factCheckingEvaluator = null;
                        }
                        """
                )
        );
    }

    @Test
    void relevancyEvaluator() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.ai.evaluation.RelevancyEvaluator;

                        class Demo {
                            RelevancyEvaluator relevancyEvaluator = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import org.springframework.ai.chat.evaluation.RelevancyEvaluator;

                        class Demo {
                            RelevancyEvaluator relevancyEvaluator = null;
                        }
                        """
                )
        );
    }

    @Test
    void content() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.ai.model.Content;

                        class Demo {
                            Content content = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import org.springframework.ai.content.Content;

                        class Demo {
                            Content content = null;
                        }
                        """
                )
        );
    }

    @Test
    void media() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.ai.model.Media;

                        class Demo {
                            Media media = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import org.springframework.ai.content.Media;

                        class Demo {
                            Media media = null;
                        }
                        """
                )
        );
    }

    @Test
    void mediaContent() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.ai.model.MediaContent;

                        class Demo {
                            MediaContent mediaContent = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import org.springframework.ai.content.MediaContent;

                        class Demo {
                            MediaContent mediaContent = null;
                        }
                        """
                )
        );
    }

    @Test
    void promptAssert() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.ai.util.PromptAssert;

                        class Demo {
                            PromptAssert promptAssert = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import org.springframework.ai.rag.util.PromptAssert;

                        class Demo {
                            PromptAssert promptAssert = null;
                        }
                        """
                )
        );
    }

    @Test
    void retrievalAugmentationAdvisor() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;

                        class Demo {
                            RetrievalAugmentationAdvisor advisor = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;

                        class Demo {
                            RetrievalAugmentationAdvisor advisor = null;
                        }
                        """
                )
        );
    }

    @Test
    void questionAnswerAdvisor() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;

                        class Demo {
                            QuestionAnswerAdvisor advisor = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;

                        class Demo {
                            QuestionAnswerAdvisor advisor = null;
                        }
                        """
                )
        );
    }

    @Test
    void vectorStoreChatMemoryAdvisor() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.ai.chat.client.advisor.VectorStoreChatMemoryAdvisor;

                        class Demo {
                            VectorStoreChatMemoryAdvisor advisor = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;

                        class Demo {
                            VectorStoreChatMemoryAdvisor advisor = null;
                        }
                        """
                )
        );
    }

    // Additional Dependencies

    @Language("java")
    private final String usingSimpleVectorStore = """
        package com.yourorg;

        import org.springframework.ai.vectorstore.SimpleVectorStore;

        class Demo {
          SimpleVectorStore store = null;
        }
        """;

    @Test
    void addsVectorStoreDependency() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                mavenProject("project",
                        srcMainJava(
                                java(usingSimpleVectorStore)
                        ),
                        //language=groovy
                        buildGradle(
                                  """
                                  plugins {
                                      id "java-library"
                                  }

                                  repositories {
                                      mavenCentral()
                                  }
                                  """,
                                """
                                  plugins {
                                      id "java-library"
                                  }

                                  repositories {
                                      mavenCentral()
                                  }

                                  dependencies {
                                      implementation "org.springframework.ai:spring-ai-vector-store"
                                  }
                                  """
                        )
                )
        );
    }

    @Test
    void addsRagDependencyWhenAdvisor() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                mavenProject("project",
                        srcMainJava(
                                //language=java
                                java("""
                                    package com.yourorg;

                                    import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;

                                    class Demo {
                                      RetrievalAugmentationAdvisor advisor = null;
                                    }
                                    """,
                                    """
                                    package com.yourorg;

                                    import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;

                                    class Demo {
                                      RetrievalAugmentationAdvisor advisor = null;
                                    }
                                    """)
                        ),
                        //language=groovy
                        buildGradle(
                                """
                                plugins {
                                    id "java-library"
                                }

                                repositories {
                                    mavenCentral()
                                }
                                """,
                                """
                                  plugins {
                                      id "java-library"
                                  }

                                  repositories {
                                      mavenCentral()
                                  }

                                  dependencies {
                                      implementation "org.springframework.ai:spring-ai-rag"
                                  }
                                  """
                        )
                )
        );
    }

    @Test
    void addsRagDependencyWhenComponent() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                mavenProject("project",
                        srcMainJava(
                                //language=java
                                java("""
                                    package com.yourorg;

                                    import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;

                                    class Demo {
                                      VectorStoreDocumentRetriever retriever = null;
                                    }
                                    """)
                        ),
                        //language=groovy
                        buildGradle(
                                """
                                plugins {
                                    id "java-library"
                                }

                                repositories {
                                    mavenCentral()
                                }
                                """,
                                """
                                  plugins {
                                      id "java-library"
                                  }

                                  repositories {
                                      mavenCentral()
                                  }

                                  dependencies {
                                      implementation "org.springframework.ai:spring-ai-rag"
                                  }
                                  """
                        )
                )
        );
    }

    @Test
    void addsVectorStoreAdvisorsDependencyWhenQuestionAnswer() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                mavenProject("project",
                        srcMainJava(
                                //language=java
                                java("""
                                    package com.yourorg;

                                    import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;

                                    class Demo {
                                      QuestionAnswerAdvisor advisor = null;
                                    }
                                    """,
                                        """
                                    package com.yourorg;

                                    import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;

                                    class Demo {
                                      QuestionAnswerAdvisor advisor = null;
                                    }
                                    """)
                        ),
                        //language=groovy
                        buildGradle(
                                """
                                plugins {
                                    id "java-library"
                                }

                                repositories {
                                    mavenCentral()
                                }
                                """,
                                """
                                  plugins {
                                      id "java-library"
                                  }

                                  repositories {
                                      mavenCentral()
                                  }

                                  dependencies {
                                      implementation "org.springframework.ai:spring-ai-advisors-vector-store"
                                  }
                                  """
                        )
                )
        );
    }

    @Test
    void addsVectorStoreAdvisorsDependencyWhenChatMemory() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                mavenProject("project",
                        srcMainJava(
                                //language=java
                                java("""
                                    package com.yourorg;

                                    import org.springframework.ai.chat.client.advisor.VectorStoreChatMemoryAdvisor;

                                    class Demo {
                                      VectorStoreChatMemoryAdvisor advisor = null;
                                    }
                                    """,
                                        """
                                    package com.yourorg;

                                    import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;

                                    class Demo {
                                      VectorStoreChatMemoryAdvisor advisor = null;
                                    }
                                    """)
                        ),
                        //language=groovy
                        buildGradle(
                                """
                                plugins {
                                    id "java-library"
                                }

                                repositories {
                                    mavenCentral()
                                }
                                """,
                                """
                                  plugins {
                                      id "java-library"
                                  }

                                  repositories {
                                      mavenCentral()
                                  }

                                  dependencies {
                                      implementation "org.springframework.ai:spring-ai-advisors-vector-store"
                                  }
                                  """
                        )
                )
        );
    }

    @Test
    void addsCassandraMemoryDependency() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi())
                        .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "spring-ai-cassandra-store-1.0.0-M6")),
                mavenProject("project",
                        srcMainJava(
                                //language=java
                                java("""
                                    package com.yourorg;

                                    import org.springframework.ai.chat.memory.cassandra.CassandraChatMemory;

                                    class Demo {
                                      CassandraChatMemory advisor = null;
                                    }
                                    """)
                        ),
                        //language=groovy
                        buildGradle(
                                """
                                plugins {
                                    id "java-library"
                                }

                                repositories {
                                    mavenCentral()
                                }
                                """,
                                """
                                  plugins {
                                      id "java-library"
                                  }

                                  repositories {
                                      mavenCentral()
                                  }

                                  dependencies {
                                      implementation "org.springframework.ai:spring-ai-model-chat-memory-cassandra"
                                  }
                                  """
                        )
                )
        );
    }

    // Method Changes

    @Test
    void chatClientToolNames() {
        rewriteRun(r -> r
                        .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                                "spring-ai-model-1.0.0-M7", "spring-ai-client-chat-1.0.0-M7"))
                        .typeValidationOptions(TypeValidation.builder().methodInvocations(false).build()),
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.ai.chat.client.ChatClient;

                        class Demo {
                          private void example(ChatClient.ChatClientRequestSpec requestSpec) {
                            requestSpec.tools("lizard", "George");
                          }
                          private void example2(ChatClient.ChatClientRequestSpec requestSpec) {
                            requestSpec.tools(new MyTools());
                          }

                          public static class MyTools {
                            public String getLizard() {
                              return "lizard";
                            }
                          }
                        }
                        """,
                        """
                        package com.yourorg;

                        import org.springframework.ai.chat.client.ChatClient;

                        class Demo {
                          private void example(ChatClient.ChatClientRequestSpec requestSpec) {
                            requestSpec.toolNames("lizard", "George");
                          }
                          private void example2(ChatClient.ChatClientRequestSpec requestSpec) {
                            requestSpec.tools(new MyTools());
                          }

                          public static class MyTools {
                            public String getLizard() {
                              return "lizard";
                            }
                          }
                        }
                        """
                )
        );
    }

    @Test
    void chatClientToolCallbacks() {
        rewriteRun(r -> r
                        .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                                "spring-ai-model-1.0.0-M7", "spring-ai-client-chat-1.0.0-M7"))
                        .typeValidationOptions(TypeValidation.builder().methodInvocations(false).build()),
                //language=java
                java(
                        """
                        package com.yourorg;

                        import java.util.List;
                        import org.springframework.ai.chat.client.ChatClient;
                        import org.springframework.ai.tool.ToolCallback;
                        import org.springframework.ai.tool.function.FunctionToolCallback;
                        import org.springframework.ai.tool.method.MethodToolCallback;

                        class Demo {
                          private void example(ChatClient.ChatClientRequestSpec requestSpec, org.springframework.ai.model.function.FunctionCallback toolCallback1, org.springframework.ai.model.function.FunctionCallback toolCallback2) {
                            requestSpec.tools(toolCallback1, toolCallback2);
                          }
                          private void example(ChatClient.ChatClientRequestSpec requestSpec, ToolCallback toolCallback1, ToolCallback toolCallback2) {
                            requestSpec.tools(toolCallback1, toolCallback2);
                          }
                          private void example2(ChatClient.ChatClientRequestSpec requestSpec, MethodToolCallback toolCallback1, MethodToolCallback toolCallback2) {
                            requestSpec.tools(toolCallback1, toolCallback2);
                          }
                          private void example2(ChatClient.ChatClientRequestSpec requestSpec, FunctionToolCallback toolCallback1, FunctionToolCallback toolCallback2) {
                            requestSpec.tools(toolCallback1);
                          }
                          private void example(ChatClient.ChatClientRequestSpec requestSpec, ToolCallback toolCallback1, ToolCallback toolCallback2) {
                            requestSpec.tools(List.of(toolCallback1, toolCallback2));
                          }
                        }
                        """,
                        """
                        package com.yourorg;

                        import java.util.List;
                        import org.springframework.ai.chat.client.ChatClient;
                        import org.springframework.ai.tool.ToolCallback;
                        import org.springframework.ai.tool.function.FunctionToolCallback;
                        import org.springframework.ai.tool.method.MethodToolCallback;

                        class Demo {
                          private void example(ChatClient.ChatClientRequestSpec requestSpec, org.springframework.ai.model.function.FunctionCallback toolCallback1, org.springframework.ai.model.function.FunctionCallback toolCallback2) {
                            requestSpec.toolCallbacks(toolCallback1, toolCallback2);
                          }
                          private void example(ChatClient.ChatClientRequestSpec requestSpec, ToolCallback toolCallback1, ToolCallback toolCallback2) {
                            requestSpec.toolCallbacks(toolCallback1, toolCallback2);
                          }
                          private void example2(ChatClient.ChatClientRequestSpec requestSpec, MethodToolCallback toolCallback1, MethodToolCallback toolCallback2) {
                            requestSpec.toolCallbacks(toolCallback1, toolCallback2);
                          }
                          private void example2(ChatClient.ChatClientRequestSpec requestSpec, FunctionToolCallback toolCallback1, FunctionToolCallback toolCallback2) {
                            requestSpec.toolCallbacks(toolCallback1);
                          }
                          private void example(ChatClient.ChatClientRequestSpec requestSpec, ToolCallback toolCallback1, ToolCallback toolCallback2) {
                            requestSpec.toolCallbacks(List.of(toolCallback1, toolCallback2));
                          }
                        }
                        """
                )
        );
    }

    @Test
    void chatClientToolCallbackProviders() {
        rewriteRun(r -> r
                        .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                                "spring-ai-model-1.0.0-M7", "spring-ai-client-chat-1.0.0-M7"))
                        .typeValidationOptions(TypeValidation.builder().methodInvocations(false).build()),
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.ai.chat.client.ChatClient;
                        import org.springframework.ai.tool.ToolCallbackProvider;
                        import org.springframework.ai.tool.method.MethodToolCallbackProvider;

                        class Demo {
                          private void example(ChatClient.ChatClientRequestSpec requestSpec, ToolCallbackProvider toolCallback1, ToolCallbackProvider toolCallback2) {
                            requestSpec.tools(toolCallback1, toolCallback2);
                          }
                          private void example2(ChatClient.ChatClientRequestSpec requestSpec, MethodToolCallbackProvider toolCallback1, MethodToolCallbackProvider toolCallback2) {
                            requestSpec.tools(toolCallback1, toolCallback2);
                          }
                        }
                        """,
                        """
                        package com.yourorg;

                        import org.springframework.ai.chat.client.ChatClient;
                        import org.springframework.ai.tool.ToolCallbackProvider;
                        import org.springframework.ai.tool.method.MethodToolCallbackProvider;

                        class Demo {
                          private void example(ChatClient.ChatClientRequestSpec requestSpec, ToolCallbackProvider toolCallback1, ToolCallbackProvider toolCallback2) {
                            requestSpec.toolCallbacks(toolCallback1, toolCallback2);
                          }
                          private void example2(ChatClient.ChatClientRequestSpec requestSpec, MethodToolCallbackProvider toolCallback1, MethodToolCallbackProvider toolCallback2) {
                            requestSpec.toolCallbacks(toolCallback1, toolCallback2);
                          }
                        }
                        """
                )
        );
    }

}
