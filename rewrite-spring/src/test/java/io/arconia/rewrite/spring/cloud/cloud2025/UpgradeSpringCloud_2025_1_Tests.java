package io.arconia.rewrite.spring.cloud.cloud2025;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

class UpgradeSpringCloud_2025_1_Tests implements RewriteTest {

    @Test
    void recipeConfigured() {
        assertRecipesConfigure("io.arconia.rewrite.spring.cloud2025");
    }

}
