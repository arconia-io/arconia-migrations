package io.arconia.rewrite.spring.ai2;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link UseTokenTextSplitterBuilder}.
 */
class UseTokenTextSplitterBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseTokenTextSplitterBuilder())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-commons-1.1"));
    }

    @Test
    @DocumentExample
    void noArgsConstructor() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.List;

                        import org.springframework.ai.document.Document;
                        import org.springframework.ai.transformer.splitter.TokenTextSplitter;

                        class Demo {
                            List<Document> split(List<Document> documents) {
                                return new TokenTextSplitter().split(documents);
                            }
                        }
                        """,
                        """
                        import java.util.List;

                        import org.springframework.ai.document.Document;
                        import org.springframework.ai.transformer.splitter.TokenTextSplitter;

                        class Demo {
                            List<Document> split(List<Document> documents) {
                                return TokenTextSplitter.builder().build().split(documents);
                            }
                        }
                        """
                )
        );
    }

    @Test
    void keepSeparatorConstructor() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.ai.transformer.splitter.TokenTextSplitter;

                        class Demo {
                            TokenTextSplitter splitter = new TokenTextSplitter(false);
                        }
                        """,
                        """
                        import org.springframework.ai.transformer.splitter.TokenTextSplitter;

                        class Demo {
                            TokenTextSplitter splitter = TokenTextSplitter.builder().withKeepSeparator(false).build();
                        }
                        """
                )
        );
    }

    @Test
    void allParametersConstructor() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.List;

                        import org.springframework.ai.transformer.splitter.TokenTextSplitter;

                        class Demo {
                            TokenTextSplitter splitter(int chunkSize) {
                                return new TokenTextSplitter(chunkSize, 350, 5, 10000, true, List.of('.', '?'));
                            }
                        }
                        """,
                        """
                        import java.util.List;

                        import org.springframework.ai.transformer.splitter.TokenTextSplitter;

                        class Demo {
                            TokenTextSplitter splitter(int chunkSize) {
                                return TokenTextSplitter.builder().withChunkSize(chunkSize).withMinChunkSizeChars(350).withMinChunkLengthToEmbed(5).withMaxNumChunks(10000).withKeepSeparator(true).withPunctuationMarks(List.of('.', '?')).build();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void encodingTypeConstructors() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-ai-commons-2.0", "jtokkit-1.1")),
                //language=java
                java(
                        """
                        import com.knuddels.jtokkit.api.EncodingType;
                        import org.springframework.ai.transformer.splitter.TokenTextSplitter;

                        class Demo {
                            TokenTextSplitter first = new TokenTextSplitter(EncodingType.O200K_BASE);
                            TokenTextSplitter second = new TokenTextSplitter(EncodingType.O200K_BASE, false);
                        }
                        """,
                        """
                        import com.knuddels.jtokkit.api.EncodingType;
                        import org.springframework.ai.transformer.splitter.TokenTextSplitter;

                        class Demo {
                            TokenTextSplitter first = TokenTextSplitter.builder().withEncodingType(EncodingType.O200K_BASE).build();
                            TokenTextSplitter second = TokenTextSplitter.builder().withEncodingType(EncodingType.O200K_BASE).withKeepSeparator(false).build();
                        }
                        """
                )
        );
    }

    @Test
    void builderUsageUnchanged() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.ai.transformer.splitter.TokenTextSplitter;

                        class Demo {
                            TokenTextSplitter splitter = TokenTextSplitter.builder().withChunkSize(100).build();
                        }
                        """
                )
        );
    }

    @Test
    void subclassesUnchanged() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.ai.transformer.splitter.TokenTextSplitter;

                        class CustomSplitter extends TokenTextSplitter {
                            CustomSplitter() {
                                super(false);
                            }
                        }

                        class Demo {
                            TokenTextSplitter custom = new CustomSplitter();
                            TokenTextSplitter anonymous = new TokenTextSplitter() {
                                @Override
                                protected int getLastPunctuationIndex(String chunkText) {
                                    return -1;
                                }
                            };
                        }
                        """
                )
        );
    }

}
