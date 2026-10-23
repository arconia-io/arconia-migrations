package io.arconia.rewrite.spring.boot.properties;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

/**
 * Unit tests for {@link AddSpringBootProperty}.
 */
class AddSpringBootPropertyTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AddSpringBootProperty("arconia.modes.enabled", "true", null, null, null));
    }

    @Test
    @DocumentExample
    void addYaml() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          modes:
                            name: fantasy
                        ---
                        spring:
                          config:
                            activate:
                              on-profile: dev
                        """,
                        """
                        arconia:
                          modes:
                            name: fantasy
                            enabled: true
                        ---
                        spring:
                          config:
                            activate:
                              on-profile: dev
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void addProperty() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.modes.name=fantasy
                        """,
                        """
                        arconia.modes.name=fantasy
                        arconia.modes.enabled=true
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void existingPropertyUnchanged() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.modes.enabled=false
                        """,
                        s -> s.path("src/main/resources/application.properties")),
                //language=yaml
                yaml(
                        """
                        arconia:
                          modes:
                            name: fantasy
                        ---
                        arconia.modes.enabled: false
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void otherFilesUnchanged() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.modes.name=fantasy
                        """,
                        s -> s.path("src/main/resources/messages.properties"))
        );
    }

    @Test
    void onlyWhereRequiredPropertyIsSet() {
        rewriteRun(
                spec -> spec.recipe(new AddSpringBootProperty("arconia.modes.enabled", "true", "arconia.modes.name", null, null)),
                //language=yaml
                yaml(
                        """
                        arconia:
                          modes:
                            reusable: true
                        ---
                        spring:
                          config:
                            activate:
                              on-profile: dev
                        arconia:
                          modes:
                            name: fantasy
                        """,
                        """
                        arconia:
                          modes:
                            reusable: true
                        ---
                        spring:
                          config:
                            activate:
                              on-profile: dev
                        arconia:
                          modes:
                            name: fantasy
                            enabled: true
                        """,
                        s -> s.path("src/main/resources/application.yml")),
                //language=yaml
                yaml(
                        """
                        arconia.modes.name: fantasy
                        """,
                        """
                        arconia.modes.name: fantasy
                        arconia:
                          modes:
                            enabled: true
                        """,
                        s -> s.path("src/main/resources/application-flat.yml")),
                //language=properties
                properties(
                        """
                        arconia.modes.reusable=true
                        """,
                        s -> s.path("src/main/resources/application.properties")),
                //language=properties
                properties(
                        """
                        arconia.modes.name=fantasy
                        """,
                        """
                        arconia.modes.name=fantasy
                        arconia.modes.enabled=true
                        """,
                        s -> s.path("src/main/resources/application-dev.properties"))
        );
    }

}
