package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesDataMongoDb_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateDataMongoDbModule")
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
                        import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
                        import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
                        import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration;
                        import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
                        import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
                        import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

                        class Demo {
                            MongoDataAutoConfiguration dataAutoConfiguration = null;
                            MongoReactiveDataAutoConfiguration reactiveDataAutoConfiguration = null;
                            MongoReactiveRepositoriesAutoConfiguration reactiveRepositoriesAutoConfiguration = null;
                            MongoRepositoriesAutoConfiguration repositoriesAutoConfiguration = null;
                            AutoConfigureDataMongo autoConfigureDataMongo = null;
                            DataMongoTest dataMongoTest = null;
                        }
                        """,
                        """
                        import org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration;
                        import org.springframework.boot.data.mongodb.autoconfigure.DataMongoReactiveAutoConfiguration;
                        import org.springframework.boot.data.mongodb.autoconfigure.DataMongoReactiveRepositoriesAutoConfiguration;
                        import org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration;
                        import org.springframework.boot.data.mongodb.test.autoconfigure.AutoConfigureDataMongo;
                        import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;

                        class Demo {
                            DataMongoAutoConfiguration dataAutoConfiguration = null;
                            DataMongoReactiveAutoConfiguration reactiveDataAutoConfiguration = null;
                            DataMongoReactiveRepositoriesAutoConfiguration reactiveRepositoriesAutoConfiguration = null;
                            DataMongoRepositoriesAutoConfiguration repositoriesAutoConfiguration = null;
                            AutoConfigureDataMongo autoConfigureDataMongo = null;
                            DataMongoTest dataMongoTest = null;
                        }
                        """
                )
        );
    }

}
