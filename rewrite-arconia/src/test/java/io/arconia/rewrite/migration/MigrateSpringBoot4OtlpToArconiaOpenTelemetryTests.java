package io.arconia.rewrite.migration;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.java.Assertions.srcMainJava;
import static org.openrewrite.properties.Assertions.properties;

/**
 * Unit tests for "io.arconia.rewrite.MigrateSpringBoot4_0_OtlpToArconiaOpenTelemetry".
 */
class MigrateSpringBoot4OtlpToArconiaOpenTelemetryTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.MigrateSpringBoot4_0_OtlpToArconiaOpenTelemetry")
            .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                "arconia-opentelemetry-0.25.0", "spring-boot-opentelemetry-4.0", "spring-boot-micrometer-tracing-opentelemetry-4.0"));
    }

    @Test
    void renameResourceAttributesProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        management.opentelemetry.resource-attributes=service.name=my-service,service.version=1.0.0,deployment.environment=production
                        """,
                        """
                        arconia.otel.resource.attributes=service.name=my-service,service.version=1.0.0,deployment.environment=production
                        """,
                        s -> s.path("src/main/resources/application.properties")
                )
        );
    }

    @Test
    void renameLoggingProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        management.logging.export.otlp.enabled=true
                        management.opentelemetry.logging.export.otlp.transport=http
                        management.opentelemetry.logging.export.otlp.endpoint=http://localhost:4318/v1/logs
                        management.opentelemetry.logging.export.otlp.headers=Authorization=Bearer token123,Custom-Header=value
                        management.opentelemetry.logging.export.otlp.compression=gzip
                        management.opentelemetry.logging.export.otlp.timeout=30s
                        management.opentelemetry.logging.export.otlp.connect-timeout=10s
                        """,
                        """
                        arconia.otel.logs.enabled=true
                        arconia.otel.logs.exporter.otlp.protocol=http_protobuf
                        arconia.otel.logs.exporter.otlp.endpoint=http://localhost:4318/v1/logs
                        arconia.otel.logs.exporter.otlp.headers=Authorization=Bearer token123,Custom-Header=value
                        arconia.otel.logs.exporter.otlp.compression=gzip
                        arconia.otel.logs.exporter.otlp.timeout=30s
                        arconia.otel.logs.exporter.otlp.connect-timeout=10s
                        """,
                        s -> s.path("src/main/resources/application.properties")
                )
        );
    }

    @Test
    void renameLoggingTransportWithGrpcValue() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        management.opentelemetry.logging.export.otlp.transport=grpc
                        """,
                        """
                        arconia.otel.logs.exporter.otlp.protocol=grpc
                        """,
                        s -> s.path("src/main/resources/application.properties")
                )
        );
    }

    @Test
    void renameMetricsProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        management.otlp.metrics.export.enabled=true
                        management.otlp.metrics.export.aggregation-temporality=CUMULATIVE
                        management.otlp.metrics.export.base-time-unit=milliseconds
                        management.otlp.metrics.export.headers=Authorization=Bearer token123
                        management.otlp.metrics.export.histogram-flavor=EXPONENTIAL_BUCKETS
                        management.otlp.metrics.export.read-timeout=30s
                        management.otlp.metrics.export.step=60s
                        management.otlp.metrics.export.url=http://localhost:4318/v1/metrics
                        management.otlp.metrics.export.connect-timeout=10s
                        """,
                        """
                        arconia.otel.metrics.enabled=true
                        arconia.otel.metrics.exporter.aggregation-temporality=CUMULATIVE
                        arconia.otel.exporter.otlp.micrometer.base-time-unit=milliseconds
                        arconia.otel.metrics.exporter.otlp.headers=Authorization=Bearer token123
                        arconia.otel.metrics.exporter.histogram-aggregation=EXPONENTIAL_BUCKETS
                        arconia.otel.metrics.exporter.otlp.timeout=30s
                        arconia.otel.metrics.exporter.interval=60s
                        arconia.otel.metrics.exporter.otlp.endpoint=http://localhost:4318/v1/metrics
                        arconia.otel.metrics.exporter.otlp.connect-timeout=10s
                        """,
                        s -> s.path("src/main/resources/application.properties")
                )
        );
    }

    @Test
    void renameTracingProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        management.tracing.export.otlp.enabled=true
                        management.opentelemetry.tracing.export.otlp.transport=http
                        management.opentelemetry.tracing.export.otlp.endpoint=http://localhost:4318/v1/traces
                        management.opentelemetry.tracing.export.otlp.headers=Authorization=Bearer token123,Custom-Header=value
                        management.opentelemetry.tracing.export.otlp.compression=gzip
                        management.opentelemetry.tracing.export.otlp.timeout=30s
                        management.opentelemetry.tracing.export.otlp.connect-timeout=10s
                        """,
                        """
                        arconia.otel.traces.enabled=true
                        arconia.otel.traces.exporter.otlp.protocol=http_protobuf
                        arconia.otel.traces.exporter.otlp.endpoint=http://localhost:4318/v1/traces
                        arconia.otel.traces.exporter.otlp.headers=Authorization=Bearer token123,Custom-Header=value
                        arconia.otel.traces.exporter.otlp.compression=gzip
                        arconia.otel.traces.exporter.otlp.timeout=30s
                        arconia.otel.traces.exporter.otlp.connect-timeout=10s
                        """,
                        s -> s.path("src/main/resources/application.properties")
                )
        );
    }

    @Test
    void renameTracingTransportWithGrpcValue() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        management.opentelemetry.tracing.export.otlp.transport=grpc
                        """,
                        """
                        arconia.otel.traces.exporter.otlp.protocol=grpc
                        """,
                        s -> s.path("src/main/resources/application.properties")
                )
        );
    }

    @Test
    void renameTracingProcessorProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        management.opentelemetry.tracing.export.include-unsampled=true
                        management.opentelemetry.tracing.export.max-batch-size=512
                        management.opentelemetry.tracing.export.max-queue-size=2048
                        management.opentelemetry.tracing.export.schedule-delay=5s
                        management.opentelemetry.tracing.export.timeout=30s
                        """,
                        """
                        arconia.otel.traces.processor.export-unsampled-spans=true
                        arconia.otel.traces.processor.max-export-batch-size=512
                        arconia.otel.traces.processor.max-queue-size=2048
                        arconia.otel.traces.processor.schedule-delay=5s
                        arconia.otel.traces.processor.export-timeout=30s
                        """,
                        s -> s.path("src/main/resources/application.properties")
                )
        );
    }

    @Test
    void commentUnsupportedMetricsProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        management.otlp.metrics.export.batch-size=512
                        management.otlp.metrics.export.max-bucket-count=160
                        management.otlp.metrics.export.max-scale=20
                        """,
                        """
                        # Not supported by Arconia OpenTelemetry.
                        # management.otlp.metrics.export.batch-size=512
                        arconia.otel.exporter.otlp.micrometer.max-bucket-count=160
                        arconia.otel.exporter.otlp.micrometer.max-scale=20
                        """,
                        s -> s.path("src/main/resources/application.properties")
                )
        );
    }

    @Test
    void changeSdkBuilderCustomizerTypes() {
        rewriteRun(
                mavenProject("project",
                        srcMainJava(
                                //language=java
                                java(
                                        """
                                        package com.yourorg;

                                        import org.springframework.boot.opentelemetry.autoconfigure.logging.SdkLoggerProviderBuilderCustomizer;
                                        import org.springframework.boot.micrometer.tracing.opentelemetry.autoconfigure.SdkTracerProviderBuilderCustomizer;

                                        class Demo {
                                            SdkLoggerProviderBuilderCustomizer customizer = null;
                                            SdkTracerProviderBuilderCustomizer customizer = null;
                                        }
                                        """,
                                        """
                                        package com.yourorg;

                                        import io.arconia.opentelemetry.autoconfigure.logs.OpenTelemetryLoggerProviderBuilderCustomizer;
                                        import io.arconia.opentelemetry.autoconfigure.traces.OpenTelemetryTracerProviderBuilderCustomizer;

                                        class Demo {
                                            OpenTelemetryLoggerProviderBuilderCustomizer customizer = null;
                                            OpenTelemetryTracerProviderBuilderCustomizer customizer = null;
                                        }
                                        """
                                )
                        )
                )
        );
    }

}
