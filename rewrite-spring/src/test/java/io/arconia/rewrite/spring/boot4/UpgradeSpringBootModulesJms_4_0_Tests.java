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

class UpgradeSpringBootModulesJms_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateJmsModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-actuator-3.5", "spring-boot-autoconfigure-3.5",
                        "spring-boot-actuator-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.autoconfigure.jms.JmsHealthContributorAutoConfiguration;
                        import org.springframework.boot.actuate.jms.JmsHealthIndicator;
                        import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;

                        class Demo {
                            JmsHealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            JmsHealthIndicator healthIndicator = null;
                            JmsAutoConfiguration jmsAutoConfiguration = null;
                        }
                        """,
                        """
                        import org.springframework.boot.jms.autoconfigure.JmsAutoConfiguration;
                        import org.springframework.boot.jms.autoconfigure.health.JmsHealthContributorAutoConfiguration;
                        import org.springframework.boot.jms.health.JmsHealthIndicator;

                        class Demo {
                            JmsHealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            JmsHealthIndicator healthIndicator = null;
                            JmsAutoConfiguration jmsAutoConfiguration = null;
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
                              implementation "org.springframework:spring-jms"
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
                              implementation "org.springframework.boot:spring-boot-starter-jms"

                              testImplementation "org.springframework.boot:spring-boot-starter-jms-test"
                          }
                          """
                )
        );
    }

}
