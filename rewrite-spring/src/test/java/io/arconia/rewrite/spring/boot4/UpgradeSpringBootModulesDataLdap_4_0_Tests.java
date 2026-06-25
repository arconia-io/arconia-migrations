package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesDataLdap_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateDataLdapModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5", "spring-boot-test-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.data.ldap.LdapRepositoriesAutoConfiguration;
                        import org.springframework.boot.test.autoconfigure.data.ldap.AutoConfigureDataLdap;
                        import org.springframework.boot.test.autoconfigure.data.ldap.DataLdapTest;

                        class Demo {
                            LdapRepositoriesAutoConfiguration autoConfiguration = null;
                            AutoConfigureDataLdap autoConfigureDataLdap = null;
                            DataLdapTest dataLdapTest = null;
                        }
                        """,
                        """
                        import org.springframework.boot.data.ldap.autoconfigure.DataLdapRepositoriesAutoConfiguration;
                        import org.springframework.boot.data.ldap.test.autoconfigure.AutoConfigureDataLdap;
                        import org.springframework.boot.data.ldap.test.autoconfigure.DataLdapTest;

                        class Demo {
                            DataLdapRepositoriesAutoConfiguration autoConfiguration = null;
                            AutoConfigureDataLdap autoConfigureDataLdap = null;
                            DataLdapTest dataLdapTest = null;
                        }
                        """
                )
        );
    }

}
