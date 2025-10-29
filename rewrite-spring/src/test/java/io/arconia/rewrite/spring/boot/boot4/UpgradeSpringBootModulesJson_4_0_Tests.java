package io.arconia.rewrite.spring.boot.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

/**
 * Unit tests for "io.arconia.rewrite.spring.boot4.UpgradeSpringBootModulesJson_4_0".
 */
class UpgradeSpringBootModulesJson_4_0_Tests implements RewriteTest {

    @Test
    void recipeConfigured() {
        assertRecipesConfigure("io.arconia.rewrite.spring.boot4.UpgradeSpringBootModulesJson_4_0");
    }

}
