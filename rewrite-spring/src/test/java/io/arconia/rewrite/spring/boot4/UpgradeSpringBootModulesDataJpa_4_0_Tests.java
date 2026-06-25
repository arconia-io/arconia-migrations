package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesDataJpa_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateDataJpaModule")
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
                        import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
                        import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
                        import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

                        class Demo {
                            JpaRepositoriesAutoConfiguration autoConfiguration = null;
                            AutoConfigureDataJpa autoConfigureDataJpa = null;
                            DataJpaTest dataJpaTest = null;
                        }
                        """,
                        """
                        import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
                        import org.springframework.boot.data.jpa.test.autoconfigure.AutoConfigureDataJpa;
                        import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

                        class Demo {
                            DataJpaRepositoriesAutoConfiguration autoConfiguration = null;
                            AutoConfigureDataJpa autoConfigureDataJpa = null;
                            DataJpaTest dataJpaTest = null;
                        }
                        """
                )
        );
    }

}
