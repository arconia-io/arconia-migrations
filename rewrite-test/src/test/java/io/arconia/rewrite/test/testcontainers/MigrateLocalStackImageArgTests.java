package io.arconia.rewrite.test.testcontainers;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link MigrateLocalStackImageArg}.
 */
class MigrateLocalStackImageArgTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new MigrateLocalStackImageArg())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "testcontainers-1.21", "localstack-1.21", "junit-4.13"));
    }

    @Test
    @DocumentExample
    void rewritesBareTagToFullImageName() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.testcontainers.containers.localstack.LocalStackContainer;

                        class Demo {
                            LocalStackContainer localstack = new LocalStackContainer("0.11.2");
                        }
                        """,
                        """
                        import org.testcontainers.containers.localstack.LocalStackContainer;

                        class Demo {
                            LocalStackContainer localstack = new LocalStackContainer("localstack/localstack:0.11.2");
                        }
                        """
                )
        );
    }

    @Test
    void leavesFullImageNameAlone() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.testcontainers.containers.localstack.LocalStackContainer;

                        class Demo {
                            LocalStackContainer localstack = new LocalStackContainer("localstack/localstack:1.0");
                        }
                        """
                )
        );
    }

    @Test
    void leavesDockerImageNameArgAlone() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.testcontainers.containers.localstack.LocalStackContainer;
                        import org.testcontainers.utility.DockerImageName;

                        class Demo {
                            LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:1.0"));
                        }
                        """
                )
        );
    }

    @Test
    void leavesNonLiteralArgAlone() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.testcontainers.containers.localstack.LocalStackContainer;

                        class Demo {
                            static final String IMAGE = "0.11.2";
                            LocalStackContainer localstack = new LocalStackContainer(IMAGE);
                        }
                        """
                )
        );
    }

}
