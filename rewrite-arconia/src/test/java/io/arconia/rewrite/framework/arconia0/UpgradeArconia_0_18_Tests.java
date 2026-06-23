package io.arconia.rewrite.framework.arconia0;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.properties.Assertions.properties;

class UpgradeArconia_0_18_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.framework.UpgradeArconia_0_18")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "arconia-dev-services-mongodb-atlas-0.17"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import io.arconia.dev.services.mongodb.MongoDbAtlasDevServicesAutoConfiguration;
                        import io.arconia.dev.services.mongodb.MongoDbAtlasDevServicesProperties;

                        class Demo {
                            MongoDbAtlasDevServicesAutoConfiguration config = null;
                            MongoDbAtlasDevServicesProperties properties = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import io.arconia.dev.services.mongodb.atlas.MongoDbAtlasDevServicesAutoConfiguration;
                        import io.arconia.dev.services.mongodb.atlas.MongoDbAtlasDevServicesProperties;

                        class Demo {
                            MongoDbAtlasDevServicesAutoConfiguration config = null;
                            MongoDbAtlasDevServicesProperties properties = null;
                        }
                        """
                )
        );
    }

    @Test
    void renameProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.observability.openinference.enabled=true
                        """,
                        """
                        arconia.observations.generative-ai.openinference.enabled=true
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

}
