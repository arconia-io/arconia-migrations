package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesOpenTelemetry_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateOpenTelemetryModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-actuator-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.autoconfigure.opentelemetry.OpenTelemetryAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.opentelemetry.OpenTelemetryResourceAttributes;
                        import org.springframework.boot.actuate.autoconfigure.opentelemetry.OpenTelemetryProperties;
                        import org.springframework.boot.actuate.autoconfigure.logging.ConditionalOnEnabledLoggingExport;
                        import org.springframework.boot.actuate.autoconfigure.logging.OpenTelemetryLoggingAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.logging.SdkLoggerProviderBuilderCustomizer;
                        import org.springframework.boot.actuate.autoconfigure.logging.otlp.OtlpLoggingAutoConfiguration;

                        class Demo {
                            OpenTelemetryAutoConfiguration autoConfiguration = null;
                            OpenTelemetryResourceAttributes resourceAttributes = null;
                            OpenTelemetryProperties properties = null;
                            ConditionalOnEnabledLoggingExport conditionalOnEnabledLoggingExport = null;
                            OpenTelemetryLoggingAutoConfiguration loggingAutoConfiguration = null;
                            SdkLoggerProviderBuilderCustomizer loggerProviderBuilderCustomizer = null;
                            OtlpLoggingAutoConfiguration otlpLoggingAutoConfiguration = null;
                        }
                        """,
                        """
                        import org.springframework.boot.opentelemetry.autoconfigure.OpenTelemetryProperties;
                        import org.springframework.boot.opentelemetry.autoconfigure.OpenTelemetryResourceAttributes;
                        import org.springframework.boot.opentelemetry.autoconfigure.OpenTelemetrySdkAutoConfiguration;
                        import org.springframework.boot.opentelemetry.autoconfigure.logging.ConditionalOnEnabledLoggingExport;
                        import org.springframework.boot.opentelemetry.autoconfigure.logging.OpenTelemetryLoggingAutoConfiguration;
                        import org.springframework.boot.opentelemetry.autoconfigure.logging.SdkLoggerProviderBuilderCustomizer;
                        import org.springframework.boot.opentelemetry.autoconfigure.logging.otlp.OtlpLoggingAutoConfiguration;

                        class Demo {
                            OpenTelemetrySdkAutoConfiguration autoConfiguration = null;
                            OpenTelemetryResourceAttributes resourceAttributes = null;
                            OpenTelemetryProperties properties = null;
                            ConditionalOnEnabledLoggingExport conditionalOnEnabledLoggingExport = null;
                            OpenTelemetryLoggingAutoConfiguration loggingAutoConfiguration = null;
                            SdkLoggerProviderBuilderCustomizer loggerProviderBuilderCustomizer = null;
                            OtlpLoggingAutoConfiguration otlpLoggingAutoConfiguration = null;
                        }
                        """
                )
        );
    }

}
