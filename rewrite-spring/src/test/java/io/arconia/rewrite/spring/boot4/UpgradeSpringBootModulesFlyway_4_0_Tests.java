package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesFlyway_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateFlywayModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-actuator-3.5", "spring-boot-actuator-autoconfigure-3.5",
                        "spring-boot-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.autoconfigure.flyway.FlywayEndpointAutoConfiguration;
                        import org.springframework.boot.actuate.flyway.FlywayEndpoint;
                        import org.springframework.boot.autoconfigure.flyway.FlywayProperties;

                        class Demo {
                            FlywayEndpointAutoConfiguration endpointAutoConfiguration = null;
                            FlywayEndpoint endpoint = null;
                            FlywayProperties properties = null;
                        }
                        """,
                        """
                        import org.springframework.boot.flyway.autoconfigure.FlywayProperties;
                        import org.springframework.boot.flyway.actuate.endpoint.FlywayEndpoint;
                        import org.springframework.boot.flyway.autoconfigure.FlywayEndpointAutoConfiguration;

                        class Demo {
                            FlywayEndpointAutoConfiguration endpointAutoConfiguration = null;
                            FlywayEndpoint endpoint = null;
                            FlywayProperties properties = null;
                        }
                        """
                )
        );
    }

    @Test
    void dependencies() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                //language=groovy
                buildGradle(
                        """
                        plugins {
                            id 'java-library'
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation "org.flywaydb:flyway-core"
                        }
                        """,
                        """
                        plugins {
                            id 'java-library'
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation "org.springframework.boot:spring-boot-starter-flyway"

                            testImplementation "org.springframework.boot:spring-boot-starter-flyway-test"
                        }
                        """
                )
        );
    }

}
