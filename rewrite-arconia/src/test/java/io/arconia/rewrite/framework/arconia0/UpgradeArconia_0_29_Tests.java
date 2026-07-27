package io.arconia.rewrite.framework.arconia0;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;

class UpgradeArconia_0_29_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.framework.UpgradeArconia_0_29");
    }

    @Test
    @DocumentExample
    void migrateBootstrapProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.bootstrap.profiles.enabled=false
                        arconia.dev.profiles=local
                        spring.application.name=demo
                        """,
                        """
                        spring.application.name=demo
                        """,
                        s -> s.path("src/main/resources/application.properties")),
                //language=properties
                properties(
                        null,
                        """
                        arconia.bootstrap.profiles.enabled=false
                        arconia.dev.profiles=local
                        """,
                        s -> s.path("src/main/resources/META-INF/arconia-bootstrap.properties"))
        );
    }

}
