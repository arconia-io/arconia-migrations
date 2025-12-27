package io.arconia.rewrite.spring.boot.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesArtemis_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateArtemisModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5"));
    }

    @Test
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;
                        import org.springframework.boot.autoconfigure.jms.artemis.ArtemisConfigurationCustomizer;
                        import org.springframework.boot.autoconfigure.jms.artemis.ArtemisConnectionDetails;
                        import org.springframework.boot.autoconfigure.jms.artemis.ArtemisMode;
                        import org.springframework.boot.autoconfigure.jms.artemis.ArtemisNoOpBindingRegistry;
                        import org.springframework.boot.autoconfigure.jms.artemis.ArtemisProperties;

                        class Demo {
                            ArtemisAutoConfiguration autoConfiguration = null;
                            ArtemisConfigurationCustomizer configurationCustomizer = null;
                            ArtemisConnectionDetails connectionDetails = null;
                            ArtemisMode mode = null;
                            ArtemisNoOpBindingRegistry noOpBindingRegistry = null;
                            ArtemisProperties properties = null;
                        }
                        """,
                        """
                        import org.springframework.boot.artemis.autoconfigure.*;

                        class Demo {
                            ArtemisAutoConfiguration autoConfiguration = null;
                            ArtemisConfigurationCustomizer configurationCustomizer = null;
                            ArtemisConnectionDetails connectionDetails = null;
                            ArtemisMode mode = null;
                            ArtemisNoOpBindingRegistry noOpBindingRegistry = null;
                            ArtemisProperties properties = null;
                        }
                        """
                )
        );
    }

}
