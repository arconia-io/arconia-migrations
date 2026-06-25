package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesSql_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateSqlModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-3.5", "spring-boot-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.sql.init.OnDatabaseInitializationCondition;
                        import org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties;
                        import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;

                        class Demo {
                            OnDatabaseInitializationCondition onDatabaseInitializationCondition = null;
                            SqlInitializationProperties sqlInitializationProperties = null;
                            EntityManagerFactoryBuilder entityManagerFactoryBuilder = null;
                        }
                        """,
                        """
                        import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
                        import org.springframework.boot.sql.autoconfigure.init.OnDatabaseInitializationCondition;
                        import org.springframework.boot.sql.autoconfigure.init.SqlInitializationProperties;

                        class Demo {
                            OnDatabaseInitializationCondition onDatabaseInitializationCondition = null;
                            SqlInitializationProperties sqlInitializationProperties = null;
                            EntityManagerFactoryBuilder entityManagerFactoryBuilder = null;
                        }
                        """
                )
        );
    }

}
