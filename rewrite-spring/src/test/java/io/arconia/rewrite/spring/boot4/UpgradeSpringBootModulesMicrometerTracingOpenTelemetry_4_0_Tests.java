package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesMicrometerTracingOpenTelemetry_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateMicrometerTracingOpenTelemetryModule")
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
                        import org.springframework.boot.actuate.autoconfigure.tracing.OpenTelemetryEventPublisherBeansApplicationListener;
                        import org.springframework.boot.actuate.autoconfigure.tracing.OpenTelemetryEventPublisherBeansTestExecutionListener;
                        import org.springframework.boot.actuate.autoconfigure.tracing.OpenTelemetryTracingAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.tracing.SdkTracerProviderBuilderCustomizer;
                        import org.springframework.boot.actuate.autoconfigure.tracing.SpanExporters;
                        import org.springframework.boot.actuate.autoconfigure.tracing.SpanProcessors;
                        import org.springframework.boot.actuate.autoconfigure.tracing.otlp.OtlpGrpcSpanExporterBuilderCustomizer;
                        import org.springframework.boot.actuate.autoconfigure.tracing.otlp.OtlpHttpSpanExporterBuilderCustomizer;
                        import org.springframework.boot.actuate.autoconfigure.tracing.otlp.OtlpTracingAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.tracing.otlp.OtlpTracingConnectionDetails;
                        import org.springframework.boot.actuate.autoconfigure.tracing.otlp.OtlpTracingProperties;
                        import org.springframework.boot.actuate.autoconfigure.tracing.otlp.Transport;

                        class Demo {
                            OpenTelemetryEventPublisherBeansApplicationListener applicationListener = null;
                            OpenTelemetryEventPublisherBeansTestExecutionListener testExecutionListener = null;
                            OpenTelemetryTracingAutoConfiguration tracingAutoConfiguration = null;
                            SdkTracerProviderBuilderCustomizer sdkTracerProviderBuilderCustomizer = null;
                            SpanExporters spanExporters = null;
                            SpanProcessors spanProcessors = null;
                            OtlpGrpcSpanExporterBuilderCustomizer otlpGrpcSpanExporterBuilderCustomizer = null;
                            OtlpHttpSpanExporterBuilderCustomizer otlpHttpSpanExporterBuilderCustomizer = null;
                            OtlpTracingAutoConfiguration otlpTracingAutoConfiguration = null;
                            OtlpTracingConnectionDetails otlpTracingConnectionDetails = null;
                            OtlpTracingProperties otlpTracingProperties = null;
                            Transport transport = null;
                        }
                        """,
                        """
                        import org.springframework.boot.micrometer.tracing.opentelemetry.autoconfigure.*;
                        import org.springframework.boot.micrometer.tracing.opentelemetry.autoconfigure.otlp.*;

                        class Demo {
                            OpenTelemetryEventPublisherBeansApplicationListener applicationListener = null;
                            OpenTelemetryEventPublisherBeansTestExecutionListener testExecutionListener = null;
                            OpenTelemetryTracingAutoConfiguration tracingAutoConfiguration = null;
                            SdkTracerProviderBuilderCustomizer sdkTracerProviderBuilderCustomizer = null;
                            SpanExporters spanExporters = null;
                            SpanProcessors spanProcessors = null;
                            OtlpGrpcSpanExporterBuilderCustomizer otlpGrpcSpanExporterBuilderCustomizer = null;
                            OtlpHttpSpanExporterBuilderCustomizer otlpHttpSpanExporterBuilderCustomizer = null;
                            OtlpTracingAutoConfiguration otlpTracingAutoConfiguration = null;
                            OtlpTracingConnectionDetails otlpTracingConnectionDetails = null;
                            OtlpTracingProperties otlpTracingProperties = null;
                            Transport transport = null;
                        }
                        """
                )
        );
    }

}
