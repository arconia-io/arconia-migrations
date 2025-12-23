package io.arconia.rewrite.spring.ai;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

class UseEmbeddingOptionsBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseEmbeddingOptionsBuilder())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-model-1.0"));
    }

    @Test
    void useBuilder() {
        rewriteRun(
                spec -> spec.typeValidationOptions(TypeValidation.builder().methodInvocations(false).build()),
                //language=java
                java(
                        """
                        import org.springframework.ai.embedding.EmbeddingOptionsBuilder;

                        class Demo {
                            void test() {
                                var options = EmbeddingOptionsBuilder.builder().build();
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.embedding.EmbeddingOptions;

                        class Demo {
                            void test() {
                                var options = EmbeddingOptions.builder().build();
                            }
                        }
                        """
                )
        );
    }

}
