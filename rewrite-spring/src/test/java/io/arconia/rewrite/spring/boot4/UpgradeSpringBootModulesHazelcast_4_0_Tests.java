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

class UpgradeSpringBootModulesHazelcast_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateHazelcastModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5", "spring-boot-actuator-3.5",
                        "spring-boot-actuator-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.hazelcast.HazelcastHealthContributorAutoConfiguration;
                        import org.springframework.boot.actuate.hazelcast.HazelcastHealthIndicator;

                        class Demo {
                            HazelcastAutoConfiguration autoConfiguration = null;
                            HazelcastHealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            HazelcastHealthIndicator healthIndicator = null;
                        }
                        """,
                        """
                        import org.springframework.boot.hazelcast.autoconfigure.HazelcastAutoConfiguration;
                        import org.springframework.boot.hazelcast.autoconfigure.health.HazelcastHealthContributorAutoConfiguration;
                        import org.springframework.boot.hazelcast.health.HazelcastHealthIndicator;

                        class Demo {
                            HazelcastAutoConfiguration autoConfiguration = null;
                            HazelcastHealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            HazelcastHealthIndicator healthIndicator = null;
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
                            implementation "com.hazelcast:hazelcast"
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
                            implementation "org.springframework.boot:spring-boot-starter-hazelcast"

                            testImplementation "org.springframework.boot:spring-boot-starter-hazelcast-test"
                        }
                        """
                )
        );
    }

}
