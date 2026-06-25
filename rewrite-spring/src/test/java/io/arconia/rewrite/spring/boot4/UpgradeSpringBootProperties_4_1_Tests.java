package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;

class UpgradeSpringBootProperties_4_1_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.UpgradeSpringBootProperties_4_1");
    }

    @Test
    @DocumentExample
    void shouldRenameProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        management.endpoints.enabled-by-default=true
                        logging.file.max-history=10
                        """,
                        """
                        management.endpoints.access.default=true
                        logging.logback.rollingpolicy.max-history=10
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

}
