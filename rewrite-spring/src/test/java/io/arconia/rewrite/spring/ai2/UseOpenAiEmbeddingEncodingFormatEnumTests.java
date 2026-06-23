package io.arconia.rewrite.spring.ai2;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link UseOpenAiEmbeddingEncodingFormatEnum}.
 */
class UseOpenAiEmbeddingEncodingFormatEnumTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseOpenAiEmbeddingEncodingFormatEnum())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-commons-1.1", "spring-ai-model-1.1", "spring-ai-openai-1.1"));
    }

    @Test
    @DocumentExample
    void useEnumConstant() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.ai.openai.OpenAiEmbeddingOptions;

                        class Demo {
                            OpenAiEmbeddingOptions test() {
                                return OpenAiEmbeddingOptions.builder()
                                        .encodingFormat("float")
                                        .build();
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.openai.OpenAiEmbeddingOptions;

                        class Demo {
                            OpenAiEmbeddingOptions test() {
                                return OpenAiEmbeddingOptions.builder()
                                        .encodingFormat(OpenAiEmbeddingOptions.EncodingFormat.FLOAT)
                                        .build();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void base64LiteralIsMigrated() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.ai.openai.OpenAiEmbeddingOptions;

                        class Demo {
                            OpenAiEmbeddingOptions test() {
                                return OpenAiEmbeddingOptions.builder()
                                        .encodingFormat("base64")
                                        .build();
                            }
                        }
                        """,
                        """
                        import org.springframework.ai.openai.OpenAiEmbeddingOptions;

                        class Demo {
                            OpenAiEmbeddingOptions test() {
                                return OpenAiEmbeddingOptions.builder()
                                        .encodingFormat(OpenAiEmbeddingOptions.EncodingFormat.BASE64)
                                        .build();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void unrelatedFloatLiteralIsLeftAlone() {
        rewriteRun(
                //language=java
                java(
                        """
                        class Demo {
                            String format = "float";
                        }
                        """
                )
        );
    }

}
