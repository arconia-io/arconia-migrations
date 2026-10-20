package io.arconia.rewrite.spring.cloud2026;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;

class UpgradeSpringCloud_2026_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.cloud2026.UpgradeSpringCloud_2026_0");
    }

    @Test
    @DocumentExample
    void propertyRenames() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.cloud.consul.config.prefix=config
                        spring.cloud.gateway.server.webflux.metrics.tags.path.enabled=true
                        """,
                        """
                        spring.cloud.consul.config.prefixes=config
                        spring.cloud.gateway.server.webflux.metrics.path-tags.enabled=true
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void removedPropertiesAreFlagged() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.cloud.vault.kv.backend-version=2
                        """,
                        """
                        # Deprecated property removed in Spring Cloud 2026.0. Backend version no longer required. The kv version is determined during secret retrieval
                        # spring.cloud.vault.kv.backend-version=2
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

}
