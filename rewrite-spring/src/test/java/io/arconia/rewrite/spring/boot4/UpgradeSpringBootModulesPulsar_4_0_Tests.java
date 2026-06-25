package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesPulsar_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigratePulsarModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.pulsar.PulsarAutoConfiguration;
                        import org.springframework.boot.autoconfigure.pulsar.PulsarConnectionDetails;
                        import org.springframework.boot.autoconfigure.pulsar.PulsarContainerFactoryCustomizer;
                        import org.springframework.boot.autoconfigure.pulsar.PulsarProperties;

                        class Demo {
                            PulsarAutoConfiguration autoConfiguration = null;
                            PulsarConnectionDetails connectionDetails = null;
                            PulsarContainerFactoryCustomizer containerFactoryCustomizer = null;
                            PulsarProperties properties = null;
                        }
                        """,
                        """
                        import org.springframework.boot.pulsar.autoconfigure.PulsarAutoConfiguration;
                        import org.springframework.boot.pulsar.autoconfigure.PulsarConnectionDetails;
                        import org.springframework.boot.pulsar.autoconfigure.PulsarContainerFactoryCustomizer;
                        import org.springframework.boot.pulsar.autoconfigure.PulsarProperties;

                        class Demo {
                            PulsarAutoConfiguration autoConfiguration = null;
                            PulsarConnectionDetails connectionDetails = null;
                            PulsarContainerFactoryCustomizer containerFactoryCustomizer = null;
                            PulsarProperties properties = null;
                        }
                        """
                )
        );
    }

}
