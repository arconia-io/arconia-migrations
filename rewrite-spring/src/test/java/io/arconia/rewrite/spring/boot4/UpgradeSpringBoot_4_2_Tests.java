package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

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

    @Test
    void deprecatedPropertiesFlaggedOnceThroughChainedUpgrades() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.jackson2.constructor-detector=default
                        spring.pulsar.client.service-url=pulsar://localhost:6650
                        """,
                        """
                        # Deprecated Spring Boot property. Deprecated in favor of Jackson 3
                        spring.jackson2.constructor-detector=default
                        # Deprecated Spring Boot property. module deprecated in favor of pulsar-spring
                        spring.pulsar.client.service-url=pulsar://localhost:6650
                        """,
                        s -> s.path("src/main/resources/application-deprecated.properties")),
                //language=yaml
                yaml(
                        """
                        spring:
                          jackson2:
                            constructor-detector: default
                        """,
                        """
                        spring:
                          jackson2:
                            # Deprecated Spring Boot property. Deprecated in favor of Jackson 3
                            constructor-detector: default
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

}
