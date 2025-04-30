package io.arconia.rewrite.spring.boot;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

/**
 * Unit tests for {@link DeleteSpringBootPropertyKey}.
 */
class DeleteSpringBootPropertyKeyTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new DeleteSpringBootPropertyKey(
                "arconia.modes.name",
                true, List.of()));
    }

    @Test
    void deleteYamlProperty() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          modes:
                            name: fantasy
                        """,
                        """
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void deleteProperty() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.modes.name=fantasy
                        """,
                        """
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

}
