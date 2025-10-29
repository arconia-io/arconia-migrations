package io.arconia.rewrite.spring.framework;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

/**
 * Unit tests for "io.arconia.rewrite.spring.framework.UpgradeSpringFramework_7_0".
 */
class UpgradeSpringFramework_7_0_Tests implements RewriteTest {

    @Test
    void recipeConfigured() {
        assertRecipesConfigure("io.arconia.rewrite.spring.framework.UpgradeSpringFramework_7_0");
    }

}
