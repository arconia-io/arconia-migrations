package io.arconia.rewrite.frameworks;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

/**
 * Unit tests for "io.arconia.rewrite.UpgradeArconia_0_16".
 */
class UpgradeArconia_0_16_Tests implements RewriteTest {

    @Test
    void recipeConfigured() {
        assertRecipesConfigure("io.arconia.rewrite.UpgradeArconia_0_16");
    }

}
