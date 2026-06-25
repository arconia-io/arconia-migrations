package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesDataJdbc_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateDataJdbcModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-test-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.test.autoconfigure.data.jdbc.AutoConfigureDataJdbc;
                        import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;

                        class Demo {
                            AutoConfigureDataJdbc autoConfigureDataJdbc = null;
                            DataJdbcTest dataJdbcTest = null;
                        }
                        """,
                        """
                        import org.springframework.boot.data.jdbc.test.autoconfigure.AutoConfigureDataJdbc;
                        import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;

                        class Demo {
                            AutoConfigureDataJdbc autoConfigureDataJdbc = null;
                            DataJdbcTest dataJdbcTest = null;
                        }
                        """
                )
        );
    }

}
