package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;

class UpgradeSpringBoot_4_2_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.UpgradeSpringBoot_4_2");
    }

    @Test
    @DocumentExample
    void modulithPropertiesThroughChainedUpgrades() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.modulith.republish-outstanding-events-on-restart=true
                        spring.modulith.events.staleness.check-intervall=30s
                        spring.modulith.events.staleness.resubmission=5m
                        """,
                        """
                        spring.modulith.events.republish-outstanding-events-on-restart=true
                        spring.modulith.events.staleness.check-interval=30s
                        spring.modulith.events.staleness.resubmitted=5m
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

}
