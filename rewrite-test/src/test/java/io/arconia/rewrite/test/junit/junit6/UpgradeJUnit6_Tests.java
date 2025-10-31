package io.arconia.rewrite.test.junit.junit6;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

/**
 * Unit tests for "io.arconia.rewrite.test.junit.UpgradeJUnit_6".
 */
class UpgradeJUnit6_Tests implements RewriteTest {

    @Test
    void recipeConfigured() {
        assertRecipesConfigure("io.arconia.rewrite.test.junit.UpgradeJUnit_6");
    }

}
