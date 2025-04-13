package io.arconia.rewrite;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.yaml.Assertions.yaml;

class UpgradeArconia_0_10_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.UpgradeArconia_0_10");
    }

    @Test
    void yamlKeysAreAlreadyChanged() {
        rewriteRun(
                //language=yaml
                yaml("""
                        arconia:
                          config:
                            profiles:
                              development: dev
                              test: local
                              production: prof
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void yamlKeysAreOld() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          dev:
                            profiles:
                              development: dev
                              test: local
                        """,
                        """
                        arconia:
                          config.profiles.development: dev
                          config.profiles.test: local
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

}
