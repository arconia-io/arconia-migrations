package io.arconia.rewrite.spring.boot.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesDataCassandra_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateDataCassandraModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5.*", "spring-boot-test-autoconfigure-3.5.*"));
    }

    @Test
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
                        import org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveDataAutoConfiguration;
                        import org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveRepositoriesAutoConfiguration;
                        import org.springframework.boot.autoconfigure.data.cassandra.CassandraRepositoriesAutoConfiguration;
                        import org.springframework.boot.test.autoconfigure.data.cassandra.AutoConfigureDataCassandra;
                        import org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTest;

                        class Demo {
                            CassandraDataAutoConfiguration autoConfiguration = null;
                            CassandraReactiveDataAutoConfiguration reactiveAutoConfiguration = null;
                            CassandraReactiveRepositoriesAutoConfiguration reactiveRepositoriesAutoConfiguration = null;
                            CassandraRepositoriesAutoConfiguration repositoriesAutoConfiguration = null;
                            AutoConfigureDataCassandra autoConfigureDataCassandra = null;
                            DataCassandraTest dataCassandraTest = null;
                        }
                        """,
                        """
                        import org.springframework.boot.data.cassandra.autoconfigure.DataCassandraAutoConfiguration;
                        import org.springframework.boot.data.cassandra.autoconfigure.DataCassandraReactiveAutoConfiguration;
                        import org.springframework.boot.data.cassandra.autoconfigure.DataCassandraReactiveRepositoriesAutoConfiguration;
                        import org.springframework.boot.data.cassandra.autoconfigure.DataCassandraRepositoriesAutoConfiguration;
                        import org.springframework.boot.data.cassandra.test.autoconfigure.AutoConfigureDataCassandra;
                        import org.springframework.boot.data.cassandra.test.autoconfigure.DataCassandraTest;

                        class Demo {
                            DataCassandraAutoConfiguration autoConfiguration = null;
                            DataCassandraReactiveAutoConfiguration reactiveAutoConfiguration = null;
                            DataCassandraReactiveRepositoriesAutoConfiguration reactiveRepositoriesAutoConfiguration = null;
                            DataCassandraRepositoriesAutoConfiguration repositoriesAutoConfiguration = null;
                            AutoConfigureDataCassandra autoConfigureDataCassandra = null;
                            DataCassandraTest dataCassandraTest = null;
                        }
                        """
                )
        );
    }

}
