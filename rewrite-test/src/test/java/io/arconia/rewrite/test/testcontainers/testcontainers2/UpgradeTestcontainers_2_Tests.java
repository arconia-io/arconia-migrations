package io.arconia.rewrite.test.testcontainers.testcontainers2;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
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
                        "testcontainers-1.21", "postgresql-1.21", "cassandra-1.21", "kafka-1.21",
                        "localstack-1.21", "junit-4.13"));
    }

    @Test
    void migratesDependencies() {
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
                            testImplementation "org.testcontainers:dynalite"
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
    @DocumentExample
    void migratesContainerClassToNewPackageAndRawType() {
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
    void migratesCassandraSupportClasses() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.testcontainers.containers.ContainerState;
                        import org.testcontainers.containers.delegate.CassandraDatabaseDelegate;
                        import org.testcontainers.containers.wait.CassandraQueryWaitStrategy;

                        class Demo {
                            CassandraDatabaseDelegate delegate(ContainerState state) {
                                return new CassandraDatabaseDelegate(state);
                            }

                            CassandraQueryWaitStrategy strategy() {
                                return new CassandraQueryWaitStrategy();
                            }
                        }
                        """,
                        """
                        import org.testcontainers.cassandra.CassandraDatabaseDelegate;
                        import org.testcontainers.cassandra.CassandraQueryWaitStrategy;
                        import org.testcontainers.containers.ContainerState;

                        class Demo {
                            CassandraDatabaseDelegate delegate(ContainerState state) {
                                return new CassandraDatabaseDelegate(state);
                            }

                            CassandraQueryWaitStrategy strategy() {
                                return new CassandraQueryWaitStrategy();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void migratesKafkaContainerToApacheAndRewritesConfluentImage() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.testcontainers.containers.KafkaContainer;

                        class Demo {
                            KafkaContainer kafka = new KafkaContainer("confluentinc/cp-kafka:7.0");
                        }
                        """,
                        """
                        import org.testcontainers.kafka.KafkaContainer;

                        class Demo {
                            KafkaContainer kafka = /* To keep using Confluent, switch to ConfluentKafkaContainer. */ new KafkaContainer("apache/kafka-native:latest");
                        }
                        """
                )
        );
    }

    @Test
    void migratesLocalStackContainerToNewPackageAndApi() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.net.URI;
                        import org.testcontainers.containers.localstack.LocalStackContainer;

                        class Demo {
                            LocalStackContainer localstack = new LocalStackContainer("0.11.2")
                                    .withServices(LocalStackContainer.Service.S3, LocalStackContainer.Service.SQS);

                            URI s3Endpoint() {
                                return localstack.getEndpointOverride(LocalStackContainer.Service.S3);
                            }
                        }
                        """,
                        """
                        import org.testcontainers.localstack.LocalStackContainer;

                        import java.net.URI;

                        class Demo {
                            LocalStackContainer localstack = new LocalStackContainer("localstack/localstack:0.11.2")
                                    .withServices("s3", "sqs");

                            URI s3Endpoint() {
                                return localstack.getEndpoint();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void renamesGetContainerIpAddressToGetHost() {
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
