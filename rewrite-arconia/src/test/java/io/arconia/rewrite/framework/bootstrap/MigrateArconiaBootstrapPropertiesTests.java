package io.arconia.rewrite.framework.bootstrap;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

/**
 * Tests for {@link MigrateArconiaBootstrapProperties}.
 */
class MigrateArconiaBootstrapPropertiesTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new MigrateArconiaBootstrapProperties());
    }

    @Test
    @DocumentExample
    void migratePropertiesFile() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.bootstrap.profiles.enabled=false
                        arconia.dev.profiles=local
                        spring.application.name=demo
                        """,
                        """
                        spring.application.name=demo
                        """,
                        s -> s.path("src/main/resources/application.properties")),
                //language=properties
                properties(
                        null,
                        """
                        arconia.bootstrap.profiles.enabled=false
                        arconia.dev.profiles=local
                        """,
                        s -> s.path("src/main/resources/META-INF/arconia-bootstrap.properties"))
        );
    }

    @Test
    void migrateNestedYamlAndCleanUpEmptyParents() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          bootstrap:
                            profiles:
                              enabled: false
                          dev:
                            profiles:
                              - local
                              - debug
                        spring:
                          application:
                            name: demo
                        """,
                        """
                        spring:
                          application:
                            name: demo
                        """,
                        s -> s.path("src/main/resources/application.yml")),
                //language=properties
                properties(
                        null,
                        """
                        arconia.bootstrap.profiles.enabled=false
                        arconia.dev.profiles=local,debug
                        """,
                        s -> s.path("src/main/resources/META-INF/arconia-bootstrap.properties"))
        );
    }

    @Test
    void migrateFlatYamlKeysAndInlineSequence() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia.dev.profiles: [local, debug]
                        spring.application.name: demo
                        """,
                        """
                        spring.application.name: demo
                        """,
                        s -> s.path("src/main/resources/application.yml")),
                //language=properties
                properties(
                        null,
                        """
                        arconia.dev.profiles=local,debug
                        """,
                        s -> s.path("src/main/resources/META-INF/arconia-bootstrap.properties"))
        );
    }

    @Test
    void mergeIntoExistingBootstrapFileWithoutOverwriting() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.bootstrap.profiles.enabled=false
                        arconia.dev.profiles=local
                        spring.application.name=demo
                        """,
                        """
                        spring.application.name=demo
                        """,
                        s -> s.path("src/main/resources/application.properties")),
                //language=properties
                properties(
                        """
                        arconia.dev.profiles=cloud
                        """,
                        """
                        arconia.dev.profiles=cloud
                        arconia.bootstrap.profiles.enabled=false
                        """,
                        s -> s.path("src/main/resources/META-INF/arconia-bootstrap.properties"))
        );
    }

    @Test
    void markPropertyWithPlaceholder() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.dev.profiles=${my.profiles}
                        spring.application.name=demo
                        """,
                        """
                        ~~(Move this property to META-INF/arconia-bootstrap.properties manually. The file does not resolve property placeholders.)~~>arconia.dev.profiles=${my.profiles}
                        spring.application.name=demo
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void markPropertyInProfileSpecificFile() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.dev.profiles=local
                        """,
                        """
                        ~~(Move this property to META-INF/arconia-bootstrap.properties manually. The file does not support profile-specific configuration.)~~>arconia.dev.profiles=local
                        """,
                        s -> s.path("src/main/resources/application-dev.properties"))
        );
    }

    @Test
    void markPropertyInProfileActivatedYamlDocument() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          dev:
                            profiles: local
                        spring:
                          application:
                            name: demo
                        ---
                        spring:
                          config:
                            activate:
                              on-profile: staging
                        arconia:
                          dev:
                            profiles: staging
                        """,
                        """
                        spring:
                          application:
                            name: demo
                        ---
                        spring:
                          config:
                            activate:
                              on-profile: staging
                        arconia:
                          dev:
                            ~~(Move this property to META-INF/arconia-bootstrap.properties manually. The file does not support profile-specific configuration.)~~>profiles: staging
                        """,
                        s -> s.path("src/main/resources/application.yml")),
                //language=properties
                properties(
                        null,
                        """
                        arconia.dev.profiles=local
                        """,
                        s -> s.path("src/main/resources/META-INF/arconia-bootstrap.properties"))
        );
    }

    @Test
    void markConflictingValuesAcrossFilesInSameSourceSet() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.dev.profiles=local
                        """,
                        """
                        ~~(Move this property to META-INF/arconia-bootstrap.properties manually. Conflicting values were found in this source set.)~~>arconia.dev.profiles=local
                        """,
                        s -> s.path("src/main/resources/application.properties")),
                //language=yaml
                yaml(
                        """
                        arconia:
                          dev:
                            profiles: cloud
                        """,
                        """
                        arconia:
                          dev:
                            ~~(Move this property to META-INF/arconia-bootstrap.properties manually. Conflicting values were found in this source set.)~~>profiles: cloud
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void migrateMainAndTestSourceSetsIndependently() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.dev.profiles=local
                        spring.application.name=demo
                        """,
                        """
                        spring.application.name=demo
                        """,
                        s -> s.path("src/main/resources/application.properties")),
                //language=properties
                properties(
                        """
                        arconia.test.profiles=integration
                        spring.application.name=demo
                        """,
                        """
                        spring.application.name=demo
                        """,
                        s -> s.path("src/test/resources/application.properties")),
                //language=properties
                properties(
                        null,
                        """
                        arconia.dev.profiles=local
                        """,
                        s -> s.path("src/main/resources/META-INF/arconia-bootstrap.properties")),
                //language=properties
                properties(
                        null,
                        """
                        arconia.test.profiles=integration
                        """,
                        s -> s.path("src/test/resources/META-INF/arconia-bootstrap.properties"))
        );
    }

    @Test
    void noChangesWhenBootstrapPropertiesAbsent() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.bootstrap.mode=dev
                        spring.application.name=demo
                        """,
                        s -> s.path("src/main/resources/application.properties")),
                //language=yaml
                yaml(
                        """
                        spring:
                          application:
                            name: demo
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void noChangesWhenAlreadyMigrated() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.application.name=demo
                        """,
                        s -> s.path("src/main/resources/application.properties")),
                //language=properties
                properties(
                        """
                        arconia.bootstrap.profiles.enabled=false
                        arconia.dev.profiles=local
                        """,
                        s -> s.path("src/main/resources/META-INF/arconia-bootstrap.properties"))
        );
    }

}
