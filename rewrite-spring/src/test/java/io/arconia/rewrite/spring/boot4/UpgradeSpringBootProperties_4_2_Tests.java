package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

class UpgradeSpringBootProperties_4_2_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.UpgradeSpringBootProperties_4_2");
    }

    @Test
    @DocumentExample
    void forwardedHeaderStrategy() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        server.forward-headers-strategy=native
                        """,
                        """
                        server.request.header.forwarded.strategy=web-server
                        """,
                        s -> s.path("src/main/resources/application.properties")),
                //language=yaml
                yaml(
                        """
                        server:
                          forward-headers-strategy: NATIVE
                        """,
                        """
                        server:
                          request.header.forwarded.strategy: web-server
                        """,
                        s -> s.path("src/main/resources/application.yml")),
                //language=properties
                properties(
                        """
                        server.forward-headers-strategy=framework
                        """,
                        """
                        server.request.header.forwarded.strategy=framework
                        """,
                        s -> s.path("src/main/resources/application-prod.properties"))
        );
    }

    @Test
    void ignoredMeters() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        management.metrics.observations.ignored-meters=long-task-timer
                        management.metrics.use-global-registry=false
                        """,
                        """
                        management.metrics.use-global-registry=false
                        """,
                        s -> s.path("src/main/resources/application.properties")),
                //language=yaml
                yaml(
                        """
                        management:
                          metrics:
                            observations:
                              ignored-meters:
                                - long-task-timer
                            use-global-registry: false
                        """,
                        """
                        management:
                          metrics:
                            use-global-registry: false
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void deprecatedPulsarProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.pulsar.client.service-url=pulsar://localhost:6650
                        """,
                        """
                        # Deprecated Spring Boot property. module deprecated in favor of pulsar-spring
                        spring.pulsar.client.service-url=pulsar://localhost:6650
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

}
