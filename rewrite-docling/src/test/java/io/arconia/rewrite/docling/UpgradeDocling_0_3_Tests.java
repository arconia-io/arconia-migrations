package io.arconia.rewrite.docling;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeDocling_0_3_Tests implements RewriteTest {

    @Test
    void recipeConfigured() {
        assertRecipesConfigure("io.arconia.rewrite.docling.UpgradeDocling_0_3");
    }

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.docling.UpgradeDocling_0_3")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "docling-core-0.2"));
    }

    @Test
    void packageChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import ai.docling.api.core.DoclingDocument;

                        class Demo {
                            DoclingDocument document = null;
                        }
                        """,
                        """
                        import ai.docling.core.DoclingDocument;

                        class Demo {
                            DoclingDocument document = null;
                        }
                        """
                )
        );
    }

}
