package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;

class UpgradeSpringBootProperties_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.UpgradeSpringBootProperties_4_0");
    }

    @Test
    @DocumentExample
    void shouldRenameProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        management.endpoints.enabled-by-default=true
                        spring.flyway.oracle-sqlplus=true
                        """,
                        """
                        management.endpoints.access.default=true
                        spring.flyway.oracle.sqlplus=true
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

}
