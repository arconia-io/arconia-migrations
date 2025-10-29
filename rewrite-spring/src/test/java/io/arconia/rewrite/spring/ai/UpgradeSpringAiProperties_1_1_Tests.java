package io.arconia.rewrite.spring.ai;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;

/**
 * Tests for "io.arconia.rewrite.spring.ai.UpgradeSpringAiProperties_1_1".
 */
public class UpgradeSpringAiProperties_1_1_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.ai.UpgradeSpringAiProperties_1_1");
    }

    @Test
    void updateProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.ai.ollama.chat.options.use-n-u-m-a=true
                        spring.ai.ollama.chat.options.low-v-r-a-m=true
                        spring.ai.ollama.chat.options.f16-k-v=true
                        spring.ai.ollama.chat.options.logits-all=true
                        spring.ai.ollama.chat.options.vocab-only=true
                        spring.ai.ollama.chat.options.use-m-lock=true
                        spring.ai.ollama.chat.options.tfs-z=true
                        spring.ai.ollama.chat.options.mirostat=true
                        spring.ai.ollama.chat.options.mirostat-tau=true
                        spring.ai.ollama.chat.options.mirostat-eta=true
                        spring.ai.ollama.chat.options.penalize-newline=true
                        """,
                        """
                        # Deprecated. Not supported by Ollama Server any longer.
                        # spring.ai.ollama.chat.options.use-n-u-m-a=true
                        # Deprecated. Not supported by Ollama Server any longer.
                        # spring.ai.ollama.chat.options.low-v-r-a-m=true
                        # Deprecated. Not supported by Ollama Server any longer.
                        # spring.ai.ollama.chat.options.f16-k-v=true
                        # Deprecated. Not supported by Ollama Server any longer.
                        # spring.ai.ollama.chat.options.logits-all=true
                        # Deprecated. Not supported by Ollama Server any longer.
                        # spring.ai.ollama.chat.options.vocab-only=true
                        # Deprecated. Not supported by Ollama Server any longer.
                        # spring.ai.ollama.chat.options.use-m-lock=true
                        # Deprecated. Not supported by Ollama Server any longer.
                        # spring.ai.ollama.chat.options.tfs-z=true
                        # Deprecated. Not supported by Ollama Server any longer.
                        # spring.ai.ollama.chat.options.mirostat=true
                        # Deprecated. Not supported by Ollama Server any longer.
                        # spring.ai.ollama.chat.options.mirostat-tau=true
                        # Deprecated. Not supported by Ollama Server any longer.
                        # spring.ai.ollama.chat.options.mirostat-eta=true
                        # Deprecated. Not supported by Ollama Server any longer.
                        # spring.ai.ollama.chat.options.penalize-newline=true
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

}
