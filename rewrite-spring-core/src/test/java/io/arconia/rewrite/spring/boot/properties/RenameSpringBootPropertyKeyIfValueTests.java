package io.arconia.rewrite.spring.boot.properties;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

/**
 * Unit tests for {@link RenameSpringBootPropertyKeyIfValue}.
 */
class RenameSpringBootPropertyKeyIfValueTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new RenameSpringBootPropertyKeyIfValue(
                "arconia.feature.mode",
                "advanced",
                "arconia.feature.advanced-mode",
                true,
                List.of()));
    }

    @Test
    @DocumentExample
    void yamlKeyRenamedWhenValueMatches() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          feature:
                            mode: advanced
                        """,
                        """
                        arconia:
                          feature:
                            advanced-mode: advanced
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void yamlKeyNotRenamedWhenValueDoesNotMatch() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          feature:
                            mode: basic
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void propertyKeyRenamedWhenValueMatches() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.feature.mode=advanced
                        """,
                        """
                        arconia.feature.advanced-mode=advanced
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void propertyKeyNotRenamedWhenValueDoesNotMatch() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.feature.mode=basic
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void yamlKeyRenamedWithDeeperNesting() {
        rewriteRun(
                spec -> spec.recipe(new RenameSpringBootPropertyKeyIfValue(
                        "arconia.observations.conventions.type",
                        "langsmith",
                        "arconia.observations.conventions.opentelemetry.ai.flavor",
                        true,
                        List.of())),
                //language=yaml
                yaml(
                        """
                        arconia:
                          observations:
                            conventions:
                              type: langsmith
                        """,
                        """
                        arconia:
                          observations:
                            conventions:
                              opentelemetry:
                                ai:
                                  flavor: langsmith
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void yamlKeyRenamedWithCustomPathExpression() {
        rewriteRun(
                spec -> spec.recipe(new RenameSpringBootPropertyKeyIfValue(
                        "arconia.feature.mode",
                        "advanced",
                        "arconia.feature.advanced-mode",
                        true,
                        List.of("**/application-custom.yml"))),
                //language=yaml
                yaml(
                        """
                        arconia:
                          feature:
                            mode: advanced
                        """,
                        """
                        arconia:
                          feature:
                            advanced-mode: advanced
                        """,
                        s -> s.path("src/main/resources/application-custom.yml"))
        );
    }

    @Test
    void propertyKeyRenamedWithCustomPathExpression() {
        rewriteRun(
                spec -> spec.recipe(new RenameSpringBootPropertyKeyIfValue(
                        "arconia.feature.mode",
                        "advanced",
                        "arconia.feature.advanced-mode",
                        true,
                        List.of("**/application-custom.properties"))),
                //language=properties
                properties(
                        """
                        arconia.feature.mode=advanced
                        """,
                        """
                        arconia.feature.advanced-mode=advanced
                        """,
                        s -> s.path("src/main/resources/application-custom.properties"))
        );
    }

}
