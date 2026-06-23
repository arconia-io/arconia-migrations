package io.arconia.rewrite.docling;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

class RecipeValidationTests implements RewriteTest {

    @Test
    void recipesConfigure() {
        assertRecipesConfigure("io.arconia.rewrite.docling");
    }

}
