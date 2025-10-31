package io.arconia.rewrite.test.testcontainers.testcontainers2;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for "io.arconia.rewrite.test.testcontainers.UpgradeTestcontainers_2".
 */
class UpgradeTestcontainers_2_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.test.testcontainers.UpgradeTestcontainers_2")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "testcontainers-1.21.*"));
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
