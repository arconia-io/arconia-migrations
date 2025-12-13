package io.arconia.rewrite.framework;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;

class UpgradeArconia_0_20_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.UpgradeArconia_0_20");
    }

    @Test
    void propertyChanges() {
        rewriteRun(
            //language=properties
            properties(
                """
                arconia.docling.url=http://localhost:8080
                """,
                """
                arconia.docling.base-url=http://localhost:8080
                """,
                s -> s.path("src/main/resources/application.properties"))
        );
    }

}
