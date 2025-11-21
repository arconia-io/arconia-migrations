package io.arconia.rewrite.test.testcontainers.testcontainers2;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;

class UpgradeTestcontainers_2_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.test.testcontainers.UpgradeTestcontainers_2")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "testcontainers-1.21.*", "postgresql-1.21.*"));
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
                            testImplementation "org.testcontainers:activemq"
                            testImplementation "org.testcontainers:azure"
                            testImplementation "org.testcontainers:cassandra"
                            testImplementation "org.testcontainers:clickhouse"
                            testImplementation "org.testcontainers:cockroachdb"
                            testImplementation "org.testcontainers:elasticsearch"
                            testImplementation "org.testcontainers:kafka"
                            testImplementation "org.testcontainers:mongodb"
                            testImplementation "org.testcontainers:mysql"
                            testImplementation "org.testcontainers:postgresql"
                            testImplementation "org.testcontainers:rabbitmq"
                            testImplementation "org.testcontainers:selenium"
                            testImplementation "org.testcontainers:junit-jupiter"
                            testImplementation "org.testcontainers:localstack"
                            testImplementation "org.testcontainers:mariadb"
                            testImplementation "org.testcontainers:mssqlserver"
                            testImplementation "org.testcontainers:neo4j"
                            testImplementation "org.testcontainers:nginx"
                            testImplementation "org.testcontainers:oracle-xe"
                            testImplementation "org.testcontainers:pulsar"
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
                            testImplementation "org.testcontainers:testcontainers-activemq"
                            testImplementation "org.testcontainers:testcontainers-azure"
                            testImplementation "org.testcontainers:testcontainers-cassandra"
                            testImplementation "org.testcontainers:testcontainers-clickhouse"
                            testImplementation "org.testcontainers:testcontainers-cockroachdb"
                            testImplementation "org.testcontainers:testcontainers-elasticsearch"
                            testImplementation "org.testcontainers:testcontainers-kafka"
                            testImplementation "org.testcontainers:testcontainers-mongodb"
                            testImplementation "org.testcontainers:testcontainers-mysql"
                            testImplementation "org.testcontainers:testcontainers-postgresql"
                            testImplementation "org.testcontainers:testcontainers-rabbitmq"
                            testImplementation "org.testcontainers:testcontainers-selenium"
                            testImplementation "org.testcontainers:testcontainers-junit-jupiter"
                            testImplementation "org.testcontainers:testcontainers-localstack"
                            testImplementation "org.testcontainers:testcontainers-mariadb"
                            testImplementation "org.testcontainers:testcontainers-mssqlserver"
                            testImplementation "org.testcontainers:testcontainers-neo4j"
                            testImplementation "org.testcontainers:testcontainers-nginx"
                            testImplementation "org.testcontainers:testcontainers-oracle-xe"
                            testImplementation "org.testcontainers:testcontainers-pulsar"
                        }
                        """
                )
        );
    }

    @Test
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.testcontainers.containers.PostgreSQLContainer;

                        class Demo {
                            PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest");

                            PostgreSQLContainer<?> container() {
                                return postgresContainer;
                            }
                        }
                        """,
                        """
                        import org.testcontainers.postgresql.PostgreSQLContainer;

                        class Demo {
                            PostgreSQLContainer postgresContainer = new PostgreSQLContainer("postgres:latest");

                            PostgreSQLContainer container() {
                                return postgresContainer;
                            }
                        }
                        """
                )
        );
    }

    @Test
    void methodNameChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.testcontainers.containers.ContainerState;

                        class Demo {
                            String example(ContainerState containerState) {
                                return containerState.getContainerIpAddress();
                            }
                        }
                        """,
                        """
                        import org.testcontainers.containers.ContainerState;

                        class Demo {
                            String example(ContainerState containerState) {
                                return containerState.getHost();
                            }
                        }
                        """
                )
        );
    }

}
