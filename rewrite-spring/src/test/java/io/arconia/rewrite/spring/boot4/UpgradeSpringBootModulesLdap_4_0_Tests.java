package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesLdap_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateLdapModule")
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
                        import org.springframework.boot.actuate.autoconfigure.ldap.LdapHealthContributorAutoConfiguration;
                        import org.springframework.boot.actuate.ldap.LdapHealthIndicator;
                        import org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration;
                        import org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapAutoConfiguration;

                        class Demo {
                            LdapHealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            LdapHealthIndicator healthIndicator = null;
                            LdapAutoConfiguration autoConfiguration = null;
                            EmbeddedLdapAutoConfiguration embeddedAutoConfiguration = null;
                        }
                        """,
                        """
                        import org.springframework.boot.ldap.autoconfigure.LdapAutoConfiguration;
                        import org.springframework.boot.ldap.autoconfigure.embedded.EmbeddedLdapAutoConfiguration;
                        import org.springframework.boot.ldap.autoconfigure.health.LdapHealthContributorAutoConfiguration;
                        import org.springframework.boot.ldap.health.LdapHealthIndicator;

                        class Demo {
                            LdapHealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            LdapHealthIndicator healthIndicator = null;
                            LdapAutoConfiguration autoConfiguration = null;
                            EmbeddedLdapAutoConfiguration embeddedAutoConfiguration = null;
                        }
                        """
                )
        );
    }

}
