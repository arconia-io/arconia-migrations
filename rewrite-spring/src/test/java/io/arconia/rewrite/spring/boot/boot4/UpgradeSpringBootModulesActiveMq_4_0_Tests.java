package io.arconia.rewrite.spring.boot.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesActiveMq_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateActiveMqModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5"));
    }

    @Test
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
                        import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQConnectionDetails;
                        import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQConnectionFactoryCustomizer;
                        import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties;

                        class Demo {
                            ActiveMQAutoConfiguration autoConfiguration = null;
                            ActiveMQConnectionDetails connectionDetails = null;
                            ActiveMQConnectionFactoryCustomizer connectionFactoryCustomizer = null;
                            ActiveMQProperties properties = null;
                        }
                        """,
                        """
                        import org.springframework.boot.activemq.autoconfigure.ActiveMQAutoConfiguration;
                        import org.springframework.boot.activemq.autoconfigure.ActiveMQConnectionDetails;
                        import org.springframework.boot.activemq.autoconfigure.ActiveMQConnectionFactoryCustomizer;
                        import org.springframework.boot.activemq.autoconfigure.ActiveMQProperties;

                        class Demo {
                            ActiveMQAutoConfiguration autoConfiguration = null;
                            ActiveMQConnectionDetails connectionDetails = null;
                            ActiveMQConnectionFactoryCustomizer connectionFactoryCustomizer = null;
                            ActiveMQProperties properties = null;
                        }
                        """
                )
        );
    }

}
