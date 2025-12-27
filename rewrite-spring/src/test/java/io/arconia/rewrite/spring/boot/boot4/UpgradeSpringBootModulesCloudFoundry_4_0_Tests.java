package io.arconia.rewrite.spring.boot.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesCloudFoundry_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateCloudFoundryModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-actuator-autoconfigure-3.5"));
    }

    @Test
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.autoconfigure.cloudfoundry.CloudFoundryWebEndpointDiscoverer;
                        import org.springframework.boot.actuate.autoconfigure.cloudfoundry.servlet.CloudFoundryActuatorAutoConfiguration;

                        class Demo {
                            CloudFoundryWebEndpointDiscoverer webEndpointDiscoverer = null;
                            CloudFoundryActuatorAutoConfiguration autoConfiguration = null;
                        }
                        """,
                        """
                        import org.springframework.boot.cloudfoundry.autoconfigure.actuate.endpoint.CloudFoundryWebEndpointDiscoverer;
                        import org.springframework.boot.cloudfoundry.autoconfigure.actuate.endpoint.servlet.CloudFoundryActuatorAutoConfiguration;

                        class Demo {
                            CloudFoundryWebEndpointDiscoverer webEndpointDiscoverer = null;
                            CloudFoundryActuatorAutoConfiguration autoConfiguration = null;
                        }
                        """
                )
        );

    }

}
