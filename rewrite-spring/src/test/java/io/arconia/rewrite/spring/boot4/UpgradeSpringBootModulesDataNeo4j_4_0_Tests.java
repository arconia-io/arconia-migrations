package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesDataNeo4j_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateDataNeo4jModule")
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
                        import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration;
                        import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataProperties;
                        import org.springframework.boot.autoconfigure.data.neo4j.Neo4jReactiveDataAutoConfiguration;
                        import org.springframework.boot.autoconfigure.data.neo4j.Neo4jReactiveRepositoriesAutoConfiguration;
                        import org.springframework.boot.autoconfigure.data.neo4j.Neo4jRepositoriesAutoConfiguration;
                        import org.springframework.boot.test.autoconfigure.data.neo4j.AutoConfigureDataNeo4j;
                        import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;

                        class Demo {
                            Neo4jDataAutoConfiguration dataAutoConfiguration = null;
                            Neo4jDataProperties dataProperties = null;
                            Neo4jReactiveDataAutoConfiguration reactiveDataAutoConfiguration = null;
                            Neo4jReactiveRepositoriesAutoConfiguration reactiveRepositoriesAutoConfiguration = null;
                            Neo4jRepositoriesAutoConfiguration repositoriesAutoConfiguration = null;
                            AutoConfigureDataNeo4j autoConfigureDataNeo4j = null;
                            DataNeo4jTest dataNeo4jTest = null;
                        }
                        """,
                        """
                        import org.springframework.boot.data.neo4j.autoconfigure.*;
                        import org.springframework.boot.data.neo4j.test.autoconfigure.AutoConfigureDataNeo4j;
                        import org.springframework.boot.data.neo4j.test.autoconfigure.DataNeo4jTest;

                        class Demo {
                            DataNeo4jAutoConfiguration dataAutoConfiguration = null;
                            DataNeo4jProperties dataProperties = null;
                            DataNeo4jReactiveAutoConfiguration reactiveDataAutoConfiguration = null;
                            DataNeo4jReactiveRepositoriesAutoConfiguration reactiveRepositoriesAutoConfiguration = null;
                            DataNeo4jRepositoriesAutoConfiguration repositoriesAutoConfiguration = null;
                            AutoConfigureDataNeo4j autoConfigureDataNeo4j = null;
                            DataNeo4jTest dataNeo4jTest = null;
                        }
                        """
                )
        );
    }

}
