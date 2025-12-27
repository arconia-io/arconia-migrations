package io.arconia.rewrite.docling;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeDocling_0_2_Tests implements RewriteTest {

    @Test
    void recipeConfigured() {
        assertRecipesConfigure("io.arconia.rewrite.docling.UpgradeDocling_0_2");
    }

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.docling.UpgradeDocling_0_2")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "docling-serve-api-0.1", "docling-serve-client-0.1"));
    }

    @Test
    void packageChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import ai.docling.api.serve.DoclingServeApi;
                        import ai.docling.client.serve.DoclingServeClient;

                        class Demo {
                            DoclingServeApi doclingServeApi = null;
                            DoclingServeClient doclingServeClient = null;
                        }
                        """,
                        """
                        import ai.docling.serve.api.DoclingServeApi;
                        import ai.docling.serve.client.DoclingServeClient;

                        class Demo {
                            DoclingServeApi doclingServeApi = null;
                            DoclingServeClient doclingServeClient = null;
                        }
                        """
                )
        );
    }

}
