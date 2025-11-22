package io.arconia.rewrite.framework;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;

class UpgradeArconia_0_13_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.UpgradeArconia_0_13");
    }

    @Test
    void migrateProperties() {
        rewriteRun(
            //language=properties
            properties(
                """
                arconia.config.profiles.enabled=true
                arconia.config.profiles.development=dev1, dev2, dev3
                arconia.config.profiles.test=test1, test2
                arconia.config.profiles.prod=prod1
                """,
                """
                arconia.bootstrap.profiles.enabled=true
                arconia.dev.profiles=dev1, dev2, dev3
                arconia.test.profiles=test1, test2
                # Removed. Use 'spring.profiles.active' to specify the profiles to activate in production mode.
                # arconia.config.profiles.prod=prod1
                """,
                s -> s.path("src/main/resources/application.properties")
            )
        );
    }

}
