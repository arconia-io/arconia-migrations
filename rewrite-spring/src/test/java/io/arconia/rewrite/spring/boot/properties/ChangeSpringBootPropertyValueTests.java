package io.arconia.rewrite.spring.boot.properties;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

/**
 * Unit tests for {@link ChangeSpringBootPropertyValue}.
 */
class ChangeSpringBootPropertyValueTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ChangeSpringBootPropertyValue(
                "arconia.feature.toggle",
                "true",
                "enabled",
                false,
                true,
                List.of()));
    }

    @Test
    void yamlValue() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          feature:
                            toggle: true
                        """,
                        """
                        arconia:
                          feature:
                            toggle: enabled
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void yamlValueWithDifferentPath() {
        rewriteRun(
                spec -> spec.recipe(new ChangeSpringBootPropertyValue(
                        "custom.property.value",
                        "oldVal",
                        "newVal",
                        false,
                        true,
                        List.of("**/application-custom.yml"))),
                //language=yaml
                yaml(
                        """
                        custom:
                          property:
                            value: oldVal
                        """,
                        """
                        custom:
                          property:
                            value: newVal
                        """,
                        s -> s.path("src/main/resources/application-custom.yml"))
        );
    }

    @Test
    void propertyValue() {
        rewriteRun(
                //language=properties
                properties(
                       """
                       arconia.feature.toggle=true
                       """,
                       """
                       arconia.feature.toggle=enabled
                       """,
                       s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void propertyValueWithDifferentPath() {
        rewriteRun(
                spec -> spec.recipe(new ChangeSpringBootPropertyValue(
                        "custom.property.value",
                        "oldVal",
                        "newVal",
                        false,
                        true,
                        List.of("**/application-custom.properties"))),
                //language=properties
                properties(
                        """
                        custom.property.value=oldVal
                        """,
                        """
                        custom.property.value=newVal
                        """,
                        s -> s.path("src/main/resources/application-custom.properties"))
        );
    }

    @Test
    void yamlValueRegex() {
        rewriteRun(
                spec -> spec.recipe(new ChangeSpringBootPropertyValue(
                        "arconia.service.url",
                        "http://localhost:(.*)/api",
                        "https://production.example.com/api",
                        true,
                        true, // relaxedBinding
                        List.of())),
                //language=yaml
                yaml(
                        """
                        arconia:
                          service:
                            url: http://localhost:9000/api
                        """,
                        """
                        arconia:
                          service:
                            url: https://production.example.com/api
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void propertyValueRegex() {
        rewriteRun(
                spec -> spec.recipe(new ChangeSpringBootPropertyValue(
                        "arconia.service.url",
                        "http://localhost:(.*)/api",
                        "https://production.example.com/api",
                        true,
                        true, // relaxedBinding
                        List.of())),
                //language=properties
                properties(
                        """
                        arconia.service.url=http://localhost:8080/api
                        """,
                        """
                        arconia.service.url=https://production.example.com/api
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

}
