package io.arconia.rewrite.spring.boot.properties;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

/**
 * Unit tests for {@link FindSpringBootConfigFiles}.
 */
class FindSpringBootConfigFilesTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new FindSpringBootConfigFiles(List.of()));
    }

    @Test
    void findApplicationYml() {
        rewriteRun(
                //language=yaml
                yaml(
                        "spring.application.name: myapp",
                        "spring.application.name: myapp",
                        s -> s.path("src/main/resources/application.yml")
                )
        );
    }

    @Test
    void findApplicationYaml() {
        rewriteRun(
                //language=yaml
                yaml(
                        "spring.application.name: myapp",
                        "spring.application.name: myapp",
                        s -> s.path("src/main/resources/application.yaml")
                )
        );
    }

    @Test
    void findApplicationProfileYml() {
        rewriteRun(
                //language=yaml
                yaml(
                        "spring.application.name: myapp",
                        "spring.application.name: myapp",
                        s -> s.path("src/main/resources/application-dev.yml")
                )
        );
    }

    @Test
    void findApplicationProfileYaml() {
        rewriteRun(
                //language=yaml
                yaml(
                        "spring.application.name: myapp",
                        "spring.application.name: myapp",
                        s -> s.path("src/main/resources/application-dev.yaml")
                )
        );
    }

    @Test
    void findApplicationProperties() {
        rewriteRun(
                //language=properties
                properties(
                        "spring.application.name=myapp",
                        "~~>spring.application.name=myapp",
                        s -> s.path("src/main/resources/application.properties")
                )
        );
    }

    @Test
    void findApplicationProfileProperties() {
        rewriteRun(
                //language=properties
                properties(
                        "spring.application.name=myapp",
                        "~~>spring.application.name=myapp",
                        s -> s.path("src/main/resources/application-dev.properties")
                )
        );
    }

    @Test
    void findApplicationConfigYml() {
        rewriteRun(
                //language=yaml
                yaml(
                        "spring.application.name: myapp",
                        "spring.application.name: myapp",
                        s -> s.path("config/application.yml")
                )
        );
    }

    @Test
    void findApplicationKubernetesYml() {
        rewriteRun(
                //language=yaml
                yaml(
                        "spring.application.name: myapp",
                        "spring.application.name: myapp",
                        s -> s.path("k8s/application.yml")
                )
        );
    }

}
