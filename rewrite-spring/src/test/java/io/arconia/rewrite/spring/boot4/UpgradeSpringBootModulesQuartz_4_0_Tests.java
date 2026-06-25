package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesQuartz_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateQuartzModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-actuator-3.5", "spring-boot-actuator-autoconfigure-3.5",
                        "spring-boot-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.autoconfigure.quartz.QuartzEndpointAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.quartz.QuartzEndpointProperties;
                        import org.springframework.boot.actuate.quartz.QuartzEndpoint;
                        import org.springframework.boot.actuate.quartz.QuartzEndpointWebExtension;
                        import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;

                        class Demo {
                            QuartzEndpointAutoConfiguration endpointAutoConfiguration = null;
                            QuartzEndpointProperties endpointProperties = null;
                            QuartzEndpoint endpoint = null;
                            QuartzEndpointWebExtension endpointWebExtension = null;
                            QuartzAutoConfiguration autoConfiguration = null;
                        }
                        """,
                        """
                        import org.springframework.boot.quartz.autoconfigure.QuartzAutoConfiguration;
                        import org.springframework.boot.quartz.actuate.endpoint.QuartzEndpoint;
                        import org.springframework.boot.quartz.actuate.endpoint.QuartzEndpointWebExtension;
                        import org.springframework.boot.quartz.autoconfigure.QuartzEndpointAutoConfiguration;
                        import org.springframework.boot.quartz.autoconfigure.QuartzEndpointProperties;

                        class Demo {
                            QuartzEndpointAutoConfiguration endpointAutoConfiguration = null;
                            QuartzEndpointProperties endpointProperties = null;
                            QuartzEndpoint endpoint = null;
                            QuartzEndpointWebExtension endpointWebExtension = null;
                            QuartzAutoConfiguration autoConfiguration = null;
                        }
                        """
                )
        );
    }

}
