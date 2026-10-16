package io.arconia.rewrite.framework.arconia0;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

class UpgradeArconia_0_32_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.framework.UpgradeArconia_0_32");
    }

    @Test
    @DocumentExample
    void keycloakRealmImportPathRenamed() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.dev.services.keycloak.realm.import-path=classpath:keycloak/realm.json
                        arconia.dev.services.keycloak.client.id=my-app
                        """,
                        """
                        arconia.dev.services.keycloak.realm.import-paths=classpath:keycloak/realm.json
                        arconia.dev.services.keycloak.client.id=my-app
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void keycloakRealmImportPathRenamedInYaml() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          dev:
                            services:
                              keycloak:
                                realm:
                                  import-path: file:../keycloak/realm-config.json
                                client:
                                  enabled: false
                        """,
                        """
                        arconia:
                          dev:
                            services:
                              keycloak:
                                realm:
                                  import-paths: file:../keycloak/realm-config.json
                                client:
                                  enabled: false
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

}
