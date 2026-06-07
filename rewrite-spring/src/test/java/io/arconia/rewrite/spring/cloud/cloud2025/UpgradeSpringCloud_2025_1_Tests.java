package io.arconia.rewrite.spring.cloud.cloud2025;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;

class UpgradeSpringCloud_2025_1_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.cloud2025.UpgradeSpringCloud_2025_1");
    }

    @Test
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

}
