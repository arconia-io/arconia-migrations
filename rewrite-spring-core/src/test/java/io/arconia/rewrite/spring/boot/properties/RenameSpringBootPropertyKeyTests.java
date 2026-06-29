package io.arconia.rewrite.spring.boot.properties;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

/**
 * Unit tests for {@link RenameSpringBootPropertyKey}.
 */
class RenameSpringBootPropertyKeyTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new RenameSpringBootPropertyKey(
                "arconia.dev.profiles.test",
                "arconia.config.profiles.test",
                true, List.of()));
    }

    @Test
    void yamlKey() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          dev:
                            profiles:
                              test: local
                        """,
                        """
                        arconia:
                          config.profiles.test: local
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void propertyKey() {
        rewriteRun(
                //language=properties
                properties(
                       """
                       arconia.dev.profiles.test=local
                       """,
                       """
                       arconia.config.profiles.test=local
                       """,
                       s -> s.path("src/main/resources/application.properties"))
        );
    }

}
