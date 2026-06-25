package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesDataR2dbc_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateDataR2dbcModule")
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
                        import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;
                        import org.springframework.boot.test.autoconfigure.data.r2dbc.AutoConfigureDataR2dbc;
                        import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;

                        class Demo {
                            R2dbcDataAutoConfiguration autoConfiguration = null;
                            AutoConfigureDataR2dbc autoConfigureDataR2dbc = null;
                            DataR2dbcTest dataR2dbcTest = null;
                        }
                        """,
                        """
                        import org.springframework.boot.data.r2dbc.autoconfigure.R2dbcRepositoriesAutoConfiguration;
                        import org.springframework.boot.data.r2dbc.test.autoconfigure.AutoConfigureDataR2dbc;
                        import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;

                        class Demo {
                            R2dbcRepositoriesAutoConfiguration autoConfiguration = null;
                            AutoConfigureDataR2dbc autoConfigureDataR2dbc = null;
                            DataR2dbcTest dataR2dbcTest = null;
                        }
                        """
                )
        );
    }

}
