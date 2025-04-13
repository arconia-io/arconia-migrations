package io.arconia.rewrite.spring.boot;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

/**
 * Unit tests for {@link ChangeSpringBootPropertyKey}.
 */
class ChangeSpringBootPropertyKeyTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ChangeSpringBootPropertyKey(
                "arconia.dev.profiles.test",
                "arconia.config.profiles.test",
                true, List.of()));
    }

    @Test
    void yamlKeyIsAlreadyChanged() {
        rewriteRun(
                //language=yaml
                yaml("""
                        arconia:
                          config:
                            profiles:
                              test: local
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void yamlKeyIsOld() {
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
    void propertyKeyIsAlreadyChanged() {
        rewriteRun(
                //language=properties
                properties("""
                        arconia.config.profiles.test=local
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void propertyKeyIsOld() {
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
