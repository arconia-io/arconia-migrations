package io.arconia.rewrite.spring.ai1;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link UseEmbeddingOptionsBuilder}.
 */
class UseEmbeddingOptionsBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseEmbeddingOptionsBuilder())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-model-1.0"));
    }

    @Test
    @DocumentExample
    void useBuilder() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.ai.embedding.EmbeddingOptions;
                        import org.springframework.ai.embedding.EmbeddingOptionsBuilder;

                        class Demo {
                            EmbeddingOptions test() {
                                return EmbeddingOptionsBuilder.builder().build();
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.embedding.EmbeddingOptions;

                        class Demo {
                            EmbeddingOptions test() {
                                return EmbeddingOptions.builder().build();
                            }
                        }
                        """
                )
        );
    }

}
