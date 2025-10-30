package io.arconia.rewrite.frameworks;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

/**
 * Unit tests for "io.arconia.rewrite.UpgradeArconia_0_17".
 */
class UpgradeArconia_0_17_Tests implements RewriteTest {

    @Test
    void recipeConfigured() {
        assertRecipesConfigure("io.arconia.rewrite.UpgradeArconia_0_17");
    }

}
