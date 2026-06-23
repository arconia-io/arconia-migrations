package io.arconia.rewrite.framework.arconia0;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeArconia_0_12_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.framework.UpgradeArconia_0_12")
            .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "arconia-opentelemetry-sdk-spring-boot-autoconfigure-0.11"));
    }

    // Type Changes

    @Test
    @DocumentExample
    void renameSdkBuilderCustomizers() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import io.arconia.opentelemetry.autoconfigure.sdk.metrics.SdkMeterProviderBuilderCustomizer;
                        import io.arconia.opentelemetry.autoconfigure.sdk.resource.SdkResourceBuilderCustomizer;

                        class Demo {
                            SdkMeterProviderBuilderCustomizer meterCustomizer = null;
                            SdkResourceBuilderCustomizer resourceCustomizer = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import io.arconia.opentelemetry.autoconfigure.sdk.metrics.OpenTelemetryMeterProviderBuilderCustomizer;
                        import io.arconia.opentelemetry.autoconfigure.sdk.resource.OpenTelemetryResourceBuilderCustomizer;

                        class Demo {
                            OpenTelemetryMeterProviderBuilderCustomizer meterCustomizer = null;
                            OpenTelemetryResourceBuilderCustomizer resourceCustomizer = null;
                        }
                        """
                )
        );
    }

}
