package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesJooq_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateJooqModule")
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
                        import org.springframework.boot.autoconfigure.jooq.JooqProperties;
                        import org.springframework.boot.test.autoconfigure.jooq.AutoConfigureJooq;
                        import org.springframework.boot.test.autoconfigure.jooq.JooqTest;

                        class Demo {
                            JooqProperties jooqProperties = null;
                            AutoConfigureJooq autoConfigureJooq = null;
                            JooqTest jooqTest = null;
                        }
                        """,
                        """
                        import org.springframework.boot.jooq.autoconfigure.JooqProperties;
                        import org.springframework.boot.jooq.test.autoconfigure.AutoConfigureJooq;
                        import org.springframework.boot.jooq.test.autoconfigure.JooqTest;

                        class Demo {
                            JooqProperties jooqProperties = null;
                            AutoConfigureJooq autoConfigureJooq = null;
                            JooqTest jooqTest = null;
                        }
                        """
                )
        );
    }

}
