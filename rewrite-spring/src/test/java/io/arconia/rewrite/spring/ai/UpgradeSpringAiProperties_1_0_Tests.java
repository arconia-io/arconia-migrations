package io.arconia.rewrite.spring.ai;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;

/**
 * Tests for "io.arconia.rewrite.spring.ai.UpgradeSpringAi_1_0".
 */
public class UpgradeSpringAiProperties_1_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.ai.UpgradeSpringAiProperties_1_0");
    }

    @Test
    void updateProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.ai.chat.memory.jdbc.initialize-schema=false
                        spring.ai.chat.client.observations.include-input=true
                        spring.ai.mistralai.chat.enabled=true
                        """,
                        """
                        spring.ai.chat.memory.repository.jdbc.initialize-schema=false
                        spring.ai.chat.client.observations.log-prompt=true
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

}
