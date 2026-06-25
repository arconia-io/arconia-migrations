package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesDataRest_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateDataRestModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
                        import org.springframework.boot.autoconfigure.data.rest.RepositoryRestProperties;

                        class Demo {
                            RepositoryRestMvcAutoConfiguration autoConfiguration = null;
                            RepositoryRestProperties properties = null;
                        }
                        """,
                        """
                        import org.springframework.boot.data.rest.autoconfigure.DataRestAutoConfiguration;
                        import org.springframework.boot.data.rest.autoconfigure.DataRestProperties;

                        class Demo {
                            DataRestAutoConfiguration autoConfiguration = null;
                            DataRestProperties properties = null;
                        }
                        """
                )
        );
    }

}
