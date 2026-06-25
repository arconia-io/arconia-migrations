package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesMail_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateMailModule")
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
                        import org.springframework.boot.actuate.autoconfigure.mail.MailHealthContributorAutoConfiguration;
                        import org.springframework.boot.actuate.mail.MailHealthIndicator;
                        import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;

                        class Demo {
                            MailHealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            MailHealthIndicator healthIndicator = null;
                            MailSenderAutoConfiguration mailSenderAutoConfiguration = null;
                        }
                        """,
                        """
                        import org.springframework.boot.mail.autoconfigure.MailSenderAutoConfiguration;
                        import org.springframework.boot.mail.health.MailHealthIndicator;
                        import org.springframework.boot.mail.autoconfigure.MailHealthContributorAutoConfiguration;

                        class Demo {
                            MailHealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            MailHealthIndicator healthIndicator = null;
                            MailSenderAutoConfiguration mailSenderAutoConfiguration = null;
                        }
                        """
                )
        );
    }

}
