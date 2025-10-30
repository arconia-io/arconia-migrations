package io.arconia.rewrite.frameworks;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

/**
 * Unit tests for "io.arconia.rewrite.UpgradeArconia_0_15".
 */
class UpgradeArconia_0_15_Tests implements RewriteTest {

    @Test
    void recipeConfigured() {
        assertRecipesConfigure("io.arconia.rewrite.UpgradeArconia_0_15");
    }

}
