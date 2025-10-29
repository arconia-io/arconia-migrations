package io.arconia.rewrite.spring.framework;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

/**
 * Unit tests for "io.arconia.rewrite.spring.framework.UpgradeSpringFramework_6_2".
 */
class UpgradeSpringFramework_6_2_Tests implements RewriteTest {

    @Test
    void recipeConfigured() {
        assertRecipesConfigure("io.arconia.rewrite.spring.framework.UpgradeSpringFramework_6_2");
    }

}
