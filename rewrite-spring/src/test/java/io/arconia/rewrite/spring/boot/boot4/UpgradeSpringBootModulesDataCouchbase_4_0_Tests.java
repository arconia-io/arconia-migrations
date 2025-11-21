package io.arconia.rewrite.spring.boot.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesDataCouchbase_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateDataCouchbaseModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5.*", "spring-boot-test-autoconfigure-3.5.*"));
    }

    @Test
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.data.couchbase.CouchbaseDataAutoConfiguration;
                        import org.springframework.boot.autoconfigure.data.couchbase.CouchbaseDataProperties;
                        import org.springframework.boot.autoconfigure.data.couchbase.CouchbaseReactiveDataAutoConfiguration;
                        import org.springframework.boot.autoconfigure.data.couchbase.CouchbaseReactiveRepositoriesAutoConfiguration;
                        import org.springframework.boot.autoconfigure.data.couchbase.CouchbaseRepositoriesAutoConfiguration;
                        import org.springframework.boot.test.autoconfigure.data.couchbase.AutoConfigureDataCouchbase;
                        import org.springframework.boot.test.autoconfigure.data.couchbase.DataCouchbaseTest;

                        class Demo {
                            CouchbaseDataAutoConfiguration autoConfiguration = null;
                            CouchbaseDataProperties properties = null;
                            CouchbaseReactiveDataAutoConfiguration reactiveAutoConfiguration = null;
                            CouchbaseReactiveRepositoriesAutoConfiguration reactiveRepositoriesAutoConfiguration = null;
                            CouchbaseRepositoriesAutoConfiguration repositoriesAutoConfiguration = null;
                            AutoConfigureDataCouchbase autoConfigureDataCouchbase = null;
                            DataCouchbaseTest dataCouchbaseTest = null;
                        }
                        """,
                        """
                        import org.springframework.boot.data.couchbase.autoconfigure.*;
                        import org.springframework.boot.data.couchbase.test.autoconfigure.AutoConfigureDataCouchbase;
                        import org.springframework.boot.data.couchbase.test.autoconfigure.DataCouchbaseTest;

                        class Demo {
                            DataCouchbaseAutoConfiguration autoConfiguration = null;
                            DataCouchbaseProperties properties = null;
                            DataCouchbaseReactiveAutoConfiguration reactiveAutoConfiguration = null;
                            DataCouchbaseReactiveRepositoriesAutoConfiguration reactiveRepositoriesAutoConfiguration = null;
                            DataCouchbaseRepositoriesAutoConfiguration repositoriesAutoConfiguration = null;
                            AutoConfigureDataCouchbase autoConfigureDataCouchbase = null;
                            DataCouchbaseTest dataCouchbaseTest = null;
                        }
                        """
                )
        );
    }

}
