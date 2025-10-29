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
 * Unit tests for "io.arconia.rewrite.MigrateSpringBoot3OtlpToArconiaOpenTelemetry".
 */
class MigrateSpringBoot3OtlpToArconiaOpenTelemetryTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.MigrateSpringBoot3OtlpToArconiaOpenTelemetry")
            .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                "spring-boot-actuator-autoconfigure-3.5.*",
                "arconia-opentelemetry-0.14.0"));
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
                        management.otlp.logging.export.enabled=true
                        management.otlp.logging.transport=http
                        management.otlp.logging.endpoint=http://localhost:4318/v1/logs
                        management.otlp.logging.headers=Authorization=Bearer token123,Custom-Header=value
                        management.otlp.logging.compression=gzip
                        management.otlp.logging.timeout=30s
                        management.otlp.logging.connect-timeout=10s
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
                        management.otlp.logging.transport=grpc
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
                        management.otlp.tracing.export.enabled=true
                        management.otlp.tracing.transport=http
                        management.otlp.tracing.endpoint=http://localhost:4318/v1/traces
                        management.otlp.tracing.headers=Authorization=Bearer token123,Custom-Header=value
                        management.otlp.tracing.compression=gzip
                        management.otlp.tracing.timeout=30s
                        management.otlp.tracing.connect-timeout=10s
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
                        management.otlp.tracing.transport=grpc
                        """,
                        """
                        arconia.otel.traces.exporter.otlp.protocol=grpc
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
                        # Not supported by Arconia OpenTelemetry.
                        # management.otlp.metrics.export.max-bucket-count=160
                        # Not supported by Arconia OpenTelemetry.
                        # management.otlp.metrics.export.max-scale=20
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

                                        import org.springframework.boot.actuate.autoconfigure.logging.SdkLoggerProviderBuilderCustomizer;
                                        import org.springframework.boot.actuate.autoconfigure.tracing.SdkTracerProviderBuilderCustomizer;

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
