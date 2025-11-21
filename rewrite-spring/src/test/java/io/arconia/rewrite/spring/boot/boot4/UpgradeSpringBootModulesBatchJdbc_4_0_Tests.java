package io.arconia.rewrite.spring.boot.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesBatchJdbc_4_0_Tests implements RewriteTest {

    @Test
    void recipeConfigured() {
        assertRecipesConfigure("io.arconia.rewrite.spring.boot4.MigrateBatchJdbcModule");
    }

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateBatchJdbcModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5.*"));
    }

    @Test
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.batch.BatchDataSource;
                        import org.springframework.boot.autoconfigure.batch.BatchDataSourceScriptDatabaseInitializer;

                        class Demo {
                            BatchDataSource batchDataSource = null;
                            BatchDataSourceScriptDatabaseInitializer batchDataSourceScriptDatabaseInitializer = null;
                        }
                        """,
                        """
                        import org.springframework.boot.batch.jdbc.autoconfigure.BatchDataSource;
                        import org.springframework.boot.batch.jdbc.autoconfigure.BatchDataSourceScriptDatabaseInitializer;

                        class Demo {
                            BatchDataSource batchDataSource = null;
                            BatchDataSourceScriptDatabaseInitializer batchDataSourceScriptDatabaseInitializer = null;
                        }
                        """
                )
        );
    }

}
