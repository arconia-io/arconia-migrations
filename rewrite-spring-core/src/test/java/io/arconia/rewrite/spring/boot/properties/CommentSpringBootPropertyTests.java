package io.arconia.rewrite.spring.boot.properties;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

/**
 * Unit tests for {@link CommentSpringBootProperty}.
 */
class CommentSpringBootPropertyTests implements RewriteTest {

    @Test
    void yamlCommentedOutWithComment() {
        rewriteRun(r -> r.recipe(new CommentSpringBootProperty(
                        "arconia.feature.toggle",
                        "This property has been removed from this version",
                        true)),
                //language=yaml
                yaml(
                        """
                        arconia:
                          feature:
                            toggle: true
                            tier: platinum
                        """,
                        """
                        arconia:
                          feature:
                            # This property has been removed from this version
                            # toggle: true
                            tier: platinum
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void yamlWithComment() {
        rewriteRun(r -> r.recipe(new CommentSpringBootProperty(
                        "arconia.feature.toggle",
                        "This property has been removed from this version",
                        false)),
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
                            # This property has been removed from this version
                            toggle: true
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void propertyCommentedOutWithComment() {
        rewriteRun(r -> r.recipe(new CommentSpringBootProperty(
                        "arconia.feature.toggle",
                        "This property has been removed from this version",
                        true)),
                //language=properties
                properties(
                       """
                       arconia.feature.toggle=true
                       """,
                       """
                       # This property has been removed from this version
                       # arconia.feature.toggle=true
                       """,
                       s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void propertyWithComment() {
        rewriteRun(r -> r.recipe(new CommentSpringBootProperty(
                        "arconia.feature.toggle",
                        "This property has been removed from this version",
                        false)),
                //language=properties
                properties(
                        """
                        arconia.feature.toggle=true
                        """,
                        """
                        # This property has been removed from this version
                        arconia.feature.toggle=true
                        """,
                        s -> s.path("src/main/resources/application-custom.properties"))
        );
    }

}
