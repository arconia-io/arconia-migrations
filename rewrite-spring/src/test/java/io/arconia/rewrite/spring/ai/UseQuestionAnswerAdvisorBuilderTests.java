package io.arconia.rewrite.spring.ai;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

class UseQuestionAnswerAdvisorBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseQuestionAnswerAdvisorBuilder())
            .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                    "spring-ai-advisors-vector-store-1.0", "spring-ai-client-chat-1.0", "spring-ai-vector-store-1.0"));
    }

    @Test
    void vector() {
        rewriteRun(
                spec -> spec.typeValidationOptions(TypeValidation.builder().methodInvocations(false).build()),
                //language=java
                java(
                        """
                        import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
                        import org.springframework.ai.vectorstore.VectorStore;

                        class Demo {
                            void test(VectorStore vectorStore) {
                                var advisor = new QuestionAnswerAdvisor(vectorStore);
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
                        import org.springframework.ai.vectorstore.VectorStore;

                        class Demo {
                            void test(VectorStore vectorStore) {
                                var advisor = QuestionAnswerAdvisor.builder(vectorStore).build();
                            }
                        }
                        """
                )
        );
    }

}
