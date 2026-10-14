package io.arconia.rewrite.docling;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeDocling_0_7_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.docling.UpgradeDocling_0_7")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "docling-serve-api-0.6", "docling-serve-client-0.6"));
    }

    @Test
    @DocumentExample
    void migratesDeprecatedBuilderEntryPoints() {
        rewriteRun(
                //language=java
                java(
                        """
                        import ai.docling.serve.api.DoclingServeApi;
                        import ai.docling.serve.client.DoclingServeClientBuilderFactory;

                        class Demo {
                            DoclingServeApi create() {
                                return DoclingServeClientBuilderFactory.newBuilder()
                                        .baseUrl("http://localhost:5001")
                                        .build();
                            }

                            DoclingServeApi copy(DoclingServeApi api) {
                                return api.toBuilder().logRequests().build();
                            }
                        }
                        """,
                        """
                        import ai.docling.serve.api.DoclingServeApi;
                        import ai.docling.serve.client.DoclingServeClient;

                        class Demo {
                            DoclingServeApi create() {
                                return DoclingServeClient.builder()
                                        .baseUrl("http://localhost:5001")
                                        .build();
                            }

                            DoclingServeApi copy(DoclingServeApi api) {
                                return api.config().toBuilder().logRequests().build();
                            }
                        }
                        """
                )
        );
    }

}
