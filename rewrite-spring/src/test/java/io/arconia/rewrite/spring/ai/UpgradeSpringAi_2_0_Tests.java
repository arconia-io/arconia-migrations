package io.arconia.rewrite.spring.ai;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

class UpgradeSpringAi_2_0_Tests implements RewriteTest {

    @Test
    void recipeConfigured() {
        assertRecipesConfigure("io.arconia.rewrite.spring.ai.UpgradeSpringAi_2_0");
    }

}
