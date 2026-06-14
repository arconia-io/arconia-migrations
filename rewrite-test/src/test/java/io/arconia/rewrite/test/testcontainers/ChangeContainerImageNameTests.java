package io.arconia.rewrite.test.testcontainers;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link ChangeContainerImageName}.
 */
class ChangeContainerImageNameTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ChangeContainerImageName(
                "org.testcontainers.containers.KafkaContainer",
                "confluentinc/cp-kafka",
                "apache/kafka-native",
                "latest",
                null
        )).parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                "testcontainers-1.21", "kafka-1.21"));
    }

    @Test
    @DocumentExample
    void rewritesMatchingLiteralImage() {
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
                        import org.testcontainers.containers.KafkaContainer;

                        class Demo {
                            KafkaContainer kafka = new KafkaContainer("apache/kafka-native:latest");
                        }
                        """
                )
        );
    }

    @Test
    void leavesNonMatchingLiteralImage() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.testcontainers.containers.KafkaContainer;

                        class Demo {
                            KafkaContainer kafka = new KafkaContainer("apache/kafka:3.5");
                        }
                        """
                )
        );
    }

    @Test
    void rewritesImageInsideDockerImageNameParse() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.testcontainers.containers.KafkaContainer;
                        import org.testcontainers.utility.DockerImageName;

                        class Demo {
                            KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.0"));
                        }
                        """,
                        """
                        import org.testcontainers.containers.KafkaContainer;
                        import org.testcontainers.utility.DockerImageName;

                        class Demo {
                            KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:latest"));
                        }
                        """
                )
        );
    }

    @Test
    void rewritesImageInsideChainedDockerImageNameCalls() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.testcontainers.containers.KafkaContainer;
                        import org.testcontainers.utility.DockerImageName;

                        class Demo {
                            KafkaContainer kafka = new KafkaContainer(
                                    DockerImageName.parse("confluentinc/cp-kafka:7.0").asCompatibleSubstituteFor("confluentinc/cp-kafka"));
                        }
                        """,
                        """
                        import org.testcontainers.containers.KafkaContainer;
                        import org.testcontainers.utility.DockerImageName;

                        class Demo {
                            KafkaContainer kafka = new KafkaContainer(
                                    DockerImageName.parse("apache/kafka-native:latest").asCompatibleSubstituteFor("confluentinc/cp-kafka"));
                        }
                        """
                )
        );
    }

    @Test
    void rewritesImageInsideDeprecatedNewDockerImageName() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.testcontainers.containers.KafkaContainer;
                        import org.testcontainers.utility.DockerImageName;

                        class Demo {
                            KafkaContainer kafka = new KafkaContainer(new DockerImageName("confluentinc/cp-kafka:7.0"));
                        }
                        """,
                        """
                        import org.testcontainers.containers.KafkaContainer;
                        import org.testcontainers.utility.DockerImageName;

                        class Demo {
                            KafkaContainer kafka = new KafkaContainer(new DockerImageName("apache/kafka-native:latest"));
                        }
                        """
                )
        );
    }

    @Test
    void rewritesNameAndTagInDeprecatedNewDockerImageNameWithSeparateTag() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.testcontainers.containers.KafkaContainer;
                        import org.testcontainers.utility.DockerImageName;

                        class Demo {
                            KafkaContainer kafka = new KafkaContainer(new DockerImageName("confluentinc/cp-kafka", "7.0"));
                        }
                        """,
                        """
                        import org.testcontainers.containers.KafkaContainer;
                        import org.testcontainers.utility.DockerImageName;

                        class Demo {
                            KafkaContainer kafka = new KafkaContainer(new DockerImageName("apache/kafka-native", "latest"));
                        }
                        """
                )
        );
    }

    @Test
    void leavesIndeterminateImageVariable() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.testcontainers.containers.KafkaContainer;

                        class Demo {
                            static final String IMAGE = "confluentinc/cp-kafka:7.0";
                            KafkaContainer kafka = new KafkaContainer(IMAGE);
                        }
                        """
                )
        );
    }

    @Test
    void preservesOriginalTagInLiteralWhenNewImageTagAbsent() {
        rewriteRun(
                spec -> spec.recipe(new ChangeContainerImageName(
                        "org.testcontainers.containers.KafkaContainer",
                        "confluentinc/cp-kafka",
                        "apache/kafka-native",
                        null,
                        null
                )),
                //language=java
                java(
                        """
                        import org.testcontainers.containers.KafkaContainer;

                        class Demo {
                            KafkaContainer kafka = new KafkaContainer("confluentinc/cp-kafka:7.0");
                        }
                        """,
                        """
                        import org.testcontainers.containers.KafkaContainer;

                        class Demo {
                            KafkaContainer kafka = new KafkaContainer("apache/kafka-native:7.0");
                        }
                        """
                )
        );
    }

    @Test
    void keepsExistingTagInDockerImageNameWhenNewImageTagAbsent() {
        rewriteRun(
                spec -> spec.recipe(new ChangeContainerImageName(
                        "org.testcontainers.containers.KafkaContainer",
                        "confluentinc/cp-kafka",
                        "apache/kafka-native",
                        null,
                        null
                )),
                //language=java
                java(
                        """
                        import org.testcontainers.containers.KafkaContainer;
                        import org.testcontainers.utility.DockerImageName;

                        class Demo {
                            KafkaContainer kafka = new KafkaContainer(new DockerImageName("confluentinc/cp-kafka", "7.0"));
                        }
                        """,
                        """
                        import org.testcontainers.containers.KafkaContainer;
                        import org.testcontainers.utility.DockerImageName;

                        class Demo {
                            KafkaContainer kafka = new KafkaContainer(new DockerImageName("apache/kafka-native", "7.0"));
                        }
                        """
                )
        );
    }

    @Test
    void addsInlineCommentWhenConfigured() {
        rewriteRun(
                spec -> spec.recipe(new ChangeContainerImageName(
                        "org.testcontainers.containers.KafkaContainer",
                        "confluentinc/cp-kafka",
                        "apache/kafka-native",
                        "latest",
                        "To keep using Confluent, switch to ConfluentKafkaContainer."
                )),
                //language=java
                java(
                        """
                        import org.testcontainers.containers.KafkaContainer;

                        class Demo {
                            KafkaContainer kafka = new KafkaContainer("confluentinc/cp-kafka:7.0");
                        }
                        """,
                        """
                        import org.testcontainers.containers.KafkaContainer;

                        class Demo {
                            KafkaContainer kafka = /* To keep using Confluent, switch to ConfluentKafkaContainer. */ new KafkaContainer("apache/kafka-native:latest");
                        }
                        """
                )
        );
    }

    @Test
    void doesNotAddCommentWhenImageIsNotRewritten() {
        rewriteRun(
                spec -> spec.recipe(new ChangeContainerImageName(
                        "org.testcontainers.containers.KafkaContainer",
                        "confluentinc/cp-kafka",
                        "apache/kafka-native",
                        "latest",
                        "To keep using Confluent, switch to ConfluentKafkaContainer."
                )),
                //language=java
                java(
                        """
                        import org.testcontainers.containers.KafkaContainer;

                        class Demo {
                            KafkaContainer kafka = new KafkaContainer("apache/kafka:3.5");
                        }
                        """
                )
        );
    }

    @Test
    void rewritesNameOnlyWhenTagArgumentIsNonLiteralAndNewImageTagAbsent() {
        rewriteRun(
                spec -> spec.recipe(new ChangeContainerImageName(
                        "org.testcontainers.containers.KafkaContainer",
                        "confluentinc/cp-kafka",
                        "apache/kafka-native",
                        null,
                        null
                )),
                //language=java
                java(
                        """
                        import org.testcontainers.containers.KafkaContainer;
                        import org.testcontainers.utility.DockerImageName;

                        class Demo {
                            static final String TAG = "7.0";
                            KafkaContainer kafka = new KafkaContainer(new DockerImageName("confluentinc/cp-kafka", TAG));
                        }
                        """,
                        """
                        import org.testcontainers.containers.KafkaContainer;
                        import org.testcontainers.utility.DockerImageName;

                        class Demo {
                            static final String TAG = "7.0";
                            KafkaContainer kafka = new KafkaContainer(new DockerImageName("apache/kafka-native", TAG));
                        }
                        """
                )
        );
    }

}
