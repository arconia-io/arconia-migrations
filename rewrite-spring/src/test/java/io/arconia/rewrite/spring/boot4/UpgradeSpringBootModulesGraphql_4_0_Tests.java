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

class UpgradeSpringBootModulesGraphql_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateGraphQlModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5", "spring-boot-actuator-autoconfigure-3.5",
                        "spring-boot-test-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.autoconfigure.observation.graphql.GraphQlObservationAutoConfiguration;
                        import org.springframework.boot.autoconfigure.graphql.GraphQlAutoConfiguration;
                        import org.springframework.boot.test.autoconfigure.graphql.AutoConfigureGraphQl;
                        import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
                        import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;

                        class Demo {
                            GraphQlObservationAutoConfiguration observationAutoConfiguration = null;
                            GraphQlAutoConfiguration autoConfiguration = null;
                            AutoConfigureGraphQl autoConfigureGraphQl = null;
                            GraphQlTest graphQlTest = null;
                            AutoConfigureGraphQlTester autoConfigureGraphQlTester = null;
                        }
                        """,
                        """
                        import org.springframework.boot.graphql.autoconfigure.GraphQlAutoConfiguration;
                        import org.springframework.boot.graphql.autoconfigure.observation.GraphQlObservationAutoConfiguration;
                        import org.springframework.boot.graphql.test.autoconfigure.AutoConfigureGraphQl;
                        import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
                        import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;

                        class Demo {
                            GraphQlObservationAutoConfiguration observationAutoConfiguration = null;
                            GraphQlAutoConfiguration autoConfiguration = null;
                            AutoConfigureGraphQl autoConfigureGraphQl = null;
                            GraphQlTest graphQlTest = null;
                            AutoConfigureGraphQlTester autoConfigureGraphQlTester = null;
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
                            testImplementation "org.springframework.graphql:spring-graphql-test"
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
                            testImplementation "org.springframework.boot:spring-boot-starter-graphql-test"
                        }
                        """
                )
        );
    }

}
