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

class UpgradeSpringBootModulesLiquibase_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateLiquibaseModule")
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
                        import org.springframework.boot.actuate.autoconfigure.liquibase.LiquibaseEndpointAutoConfiguration;
                        import org.springframework.boot.actuate.liquibase.LiquibaseEndpoint;
                        import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;

                        class Demo {
                            LiquibaseEndpointAutoConfiguration endpointAutoConfiguration = null;
                            LiquibaseEndpoint endpoint = null;
                            LiquibaseProperties properties = null;
                        }
                        """,
                        """
                        import org.springframework.boot.liquibase.autoconfigure.LiquibaseProperties;
                        import org.springframework.boot.liquibase.actuate.endpoint.LiquibaseEndpoint;
                        import org.springframework.boot.liquibase.autoconfigure.LiquibaseEndpointAutoConfiguration;

                        class Demo {
                            LiquibaseEndpointAutoConfiguration endpointAutoConfiguration = null;
                            LiquibaseEndpoint endpoint = null;
                            LiquibaseProperties properties = null;
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
                            implementation "org.liquibase:liquibase-core"
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
                            implementation "org.springframework.boot:spring-boot-starter-liquibase"

                            testImplementation "org.springframework.boot:spring-boot-starter-liquibase-test"
                        }
                        """
                )
        );
    }

}
