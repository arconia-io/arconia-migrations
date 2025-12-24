package io.arconia.rewrite.framework;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

class UpgradeArconia_0_21_Tests implements RewriteTest {

    @Test
    void recipeConfigured() {
        assertRecipesConfigure("io.arconia.rewrite.UpgradeArconia_0_21");
    }

}
