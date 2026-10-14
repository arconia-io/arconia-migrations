package io.arconia.rewrite.docling;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link UseDoclingServeApiConfigToBuilder}.
 */
class UseDoclingServeApiConfigToBuilderTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UseDoclingServeApiConfigToBuilder())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "docling-serve-api-0.6", "docling-serve-client-0.6"));
    }

    @Test
    @DocumentExample
    void migratesToBuilderOnDoclingServeApi() {
        rewriteRun(
                //language=java
                java(
                        """
                        import ai.docling.serve.api.DoclingServeApi;

                        class Demo {
                            DoclingServeApi copy(DoclingServeApi api) {
                                return api.toBuilder().logRequests().build();
                            }
                        }
                        """,
                        """
                        import ai.docling.serve.api.DoclingServeApi;

                        class Demo {
                            DoclingServeApi copy(DoclingServeApi api) {
                                return api.config().toBuilder().logRequests().build();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void leavesConcreteClientToBuilderUntouched() {
        rewriteRun(
                //language=java
                java(
                        """
                        import ai.docling.serve.client.DoclingServeJackson3Client;

                        class Demo {
                            DoclingServeJackson3Client copy(DoclingServeJackson3Client client) {
                                return client.toBuilder().logRequests().build();
                            }
                        }
                        """
                )
        );
    }

}
