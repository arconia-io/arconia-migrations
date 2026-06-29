package io.arconia.rewrite.spring.cloud2025;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

class UpgradeSpringCloud_2025_1_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.cloud2025.UpgradeSpringCloud_2025_1");
    }

    @Test
    @DocumentExample
    void gatewayMetricsPathTagsPropertyRename() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.cloud.gateway.server.webflux.metrics.tags.path.enabled=true
                        """,
                        """
                        spring.cloud.gateway.server.webflux.metrics.path-tags.enabled=true
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void springCloudContractStubRunnerPropertiesInProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        stubrunner.stubs-mode=LOCAL
                        stubrunner.cloud.consul.enabled=false
                        """,
                        """
                        spring.cloud.contract.stubrunner.stubs-mode=LOCAL
                        spring.cloud.contract.stubrunner.cloud.consul.enabled=false
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void springCloudContractStubRunnerPropertiesInYaml() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        stubrunner:
                          stubs-mode: LOCAL
                        """,
                        """
                        spring.cloud.contract.stubrunner.stubs-mode: LOCAL
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void removedWiremockServerPropertiesAreFlagged() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        wiremock.server.port-dynamic=true
                        """,
                        """
                        # Removed in Spring Cloud Contract 5.0. A dynamic port is the default with @EnableWireMock; remove this property.
                        # wiremock.server.port-dynamic=true
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

}
