package io.arconia.rewrite.spring.ai;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.java.Assertions.srcMainJava;
import static org.openrewrite.properties.Assertions.properties;

public class UpgradeSpringAi_1_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.ai.UpgradeSpringAi_1_0")
            .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "spring-ai-core-1.0.0-M6"));
    }

    // Package Name Changes

    @Test
    void doesNotChangeKeywordMetadataEnricher() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.ai.chat.transformer.KeywordMetadataEnricher;
                        """
                )
        );
    }

    @Test
    void changesKeywordMetadataEnricher() {
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

                        import org.springframework.ai.chat.transformer.KeywordMetadataEnricher;

                        class Demo {
                            KeywordMetadataEnricher enricher = null;
                        }
                        """
                )
        );
    }

    @Test
    void doesNotChangeSummaryMetadataEnricher() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.ai.chat.transformer.SummaryMetadataEnricher;
                        """
                )
        );
    }

    @Test
    void changesSummaryMetadataEnricher() {
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

                        import org.springframework.ai.chat.transformer.SummaryMetadataEnricher;

                        class Demo {
                            SummaryMetadataEnricher enricher = null;
                        }
                        """
                )
        );
    }

    @Test
    void doesNotChangeContent() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.ai.content.Content;
                        """
                )
        );
    }

    @Test
    void changesContent() {
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
    void doesNotChangeMedia() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.ai.content.Media;
                        """
                )
        );
    }

    @Test
    void changesMedia() {
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
    void doesNotChangeMediaContent() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.ai.content.MediaContent;
                        """
                )
        );
    }

    @Test
    void changesMediaContent() {
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
    void doesNotChangeRetrievalAugmentationAdvisor() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
                        """
                )
        );
    }

    @Test
    void changesRetrievalAugmentationAdvisor() {
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
    void doesNotAddVectorStoreDependency() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                mavenProject("project",
                        //language=groovy
                        buildGradle(
                                """
                                plugins {
                                    id "java-library"
                                }

                                repositories {
                                    mavenCentral()
                                }
                                """
                        )
                )
        );
    }

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
    void doesNotAddRagDependency() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                mavenProject("project",
                        //language=groovy
                        buildGradle(
                                """
                                plugins {
                                    id "java-library"
                                }

                                repositories {
                                    mavenCentral()
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
    void doesNotAddVectorStoreAdvisorsDependency() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                mavenProject("project",
                        //language=groovy
                        buildGradle(
                                """
                                plugins {
                                    id "java-library"
                                }

                                repositories {
                                    mavenCentral()
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
    void doesNotAddCassandraMemoryDependency() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                mavenProject("project",
                        //language=groovy
                        buildGradle(
                                """
                                plugins {
                                    id "java-library"
                                }

                                repositories {
                                    mavenCentral()
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

    // Property Changes

    @Test
    void doesNotChangeProperties() {
        rewriteRun(
                //language=properties
                properties("""
                        spring.ai.chat.memory.jdbc.initialize-schema
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void changeProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.ai.chat.memory.jdbc.initialize-schema=false
                        """,
                        """
                        spring.ai.chat.memory.repository.jdbc.initialize-schema=false
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

}
