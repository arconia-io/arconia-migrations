package io.arconia.rewrite.spring.ai;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UseQuestionAnswerAdvisorBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseQuestionAnswerAdvisorBuilder())
            .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                    "spring-ai-advisors-vector-store-1.0", "spring-ai-vector-store-1.0"));
    }

    @Test
    @DocumentExample
    void useBuilderWithVectorStoreInterface() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
                        import org.springframework.ai.vectorstore.VectorStore;

                        class Demo {
                            QuestionAnswerAdvisor test(VectorStore vectorStore) {
                                return new QuestionAnswerAdvisor(vectorStore);
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
                        import org.springframework.ai.vectorstore.VectorStore;

                        class Demo {
                            QuestionAnswerAdvisor test(VectorStore vectorStore) {
                                return QuestionAnswerAdvisor.builder(vectorStore).build();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void useBuilderWithConcreteVectorStoreSubclass() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
                        import org.springframework.ai.vectorstore.VectorStore;

                        abstract class CustomVectorStore implements VectorStore {}

                        class Demo {
                            QuestionAnswerAdvisor test(CustomVectorStore vectorStore) {
                                return new QuestionAnswerAdvisor(vectorStore);
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
                        import org.springframework.ai.vectorstore.VectorStore;

                        abstract class CustomVectorStore implements VectorStore {}

                        class Demo {
                            QuestionAnswerAdvisor test(CustomVectorStore vectorStore) {
                                return QuestionAnswerAdvisor.builder(vectorStore).build();
                            }
                        }
                        """
                )
        );
    }

}
