package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesIntegration_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateIntegrationModule")
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
                        import org.springframework.boot.actuate.integration.IntegrationGraphEndpoint;
                        import org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration;

                        class Demo {
                            IntegrationGraphEndpoint integrationGraphEndpoint = null;
                            IntegrationAutoConfiguration integrationAutoConfiguration = null;
                        }
                        """,
                        """
                        import org.springframework.boot.integration.autoconfigure.IntegrationAutoConfiguration;
                        import org.springframework.boot.integration.actuate.endpoint.IntegrationGraphEndpoint;

                        class Demo {
                            IntegrationGraphEndpoint integrationGraphEndpoint = null;
                            IntegrationAutoConfiguration integrationAutoConfiguration = null;
                        }
                        """
                )
        );
    }

}
