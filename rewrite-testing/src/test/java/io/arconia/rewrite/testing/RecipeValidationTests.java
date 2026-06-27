package io.arconia.rewrite.testing;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Validated;
import org.openrewrite.config.Environment;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.SourceSpecs;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeValidationTests implements RewriteTest {

    @Test
    void recipesConfigure() {
        assertRecipesConfigureTolerant("io.arconia.rewrite.testing");
    }

    /**
     * Like {@link RewriteTest#assertRecipesConfigure(String)}, but tolerant of parameterized
     * building-block recipes: recipes that fail validation only because a required option has no
     * value. Such recipes cannot be activated standalone; they are configured by the aggregate
     * recipes that supply their options, and are therefore exercised when those aggregates are
     * validated. Any other validation failure (for example, an unresolved sub-recipe) still fails.
     */
    private void assertRecipesConfigureTolerant(String packageName) {
        InMemoryExecutionContext ctx = new InMemoryExecutionContext();
        List<Recipe> recipes = Environment.builder().scanRuntimeClasspath(packageName).build().listRecipes();
        assertThat(recipes).as("No recipes found in %s", packageName).isNotEmpty();
        SoftAssertions softly = new SoftAssertions();
        for (Recipe recipe : recipes) {
            if (!recipe.getName().startsWith(packageName)) {
                continue;
            }
            Validated<Object> validated = recipe.validate(ctx);
            boolean onlyMissingRequiredOptions = validated.isInvalid()
                    && validated.failures().stream().allMatch(failure -> "is required".equals(failure.getMessage()));
            if (onlyMissingRequiredOptions) {
                continue;
            }
            softly.assertThatCode(() -> rewriteRun(spec -> spec.recipe(recipe), new SourceSpecs[0]))
                    .as("Recipe %s failed to configure", recipe.getName())
                    .doesNotThrowAnyException();
        }
        softly.assertAll();
    }

}
