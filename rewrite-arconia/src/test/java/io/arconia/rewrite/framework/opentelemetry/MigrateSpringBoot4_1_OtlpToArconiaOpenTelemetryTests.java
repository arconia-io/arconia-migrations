package io.arconia.rewrite.framework.opentelemetry;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

class MigrateSpringBoot4_1_OtlpToArconiaOpenTelemetryTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.framework.opentelemetry.MigrateSpringBoot4_1_OtlpToArconiaOpenTelemetry");
    }

    @Test
    @DocumentExample
    void renameTracingProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        management.opentelemetry.enabled=true
                        management.opentelemetry.tracing.sampler=parent_based_trace_id_ratio
                        management.opentelemetry.tracing.export.otlp.ssl.bundle=otlp
                        management.opentelemetry.tracing.limits.max-attribute-value-length=1024
                        management.opentelemetry.tracing.limits.max-attributes=64
                        management.opentelemetry.tracing.limits.max-attributes-per-event=32
                        management.opentelemetry.tracing.limits.max-attributes-per-link=16
                        management.opentelemetry.tracing.limits.max-events=100
                        management.opentelemetry.tracing.limits.max-links=50
                        """,
                        """
                        arconia.otel.enabled=true
                        arconia.otel.traces.sampling.strategy=parent_based_trace_id_ratio
                        arconia.otel.traces.exporter.otlp.ssl.bundle=otlp
                        arconia.otel.traces.limits.max-attribute-value-length=1024
                        arconia.otel.traces.limits.max-number-of-attributes=64
                        arconia.otel.traces.limits.max-number-of-attributes-per-event=32
                        arconia.otel.traces.limits.max-number-of-attributes-per-link=16
                        arconia.otel.traces.limits.max-number-of-events=100
                        arconia.otel.traces.limits.max-number-of-links=50
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
                        management.opentelemetry.logging.export.max-batch-size=256
                        management.opentelemetry.logging.export.max-queue-size=1024
                        management.opentelemetry.logging.export.schedule-delay=2s
                        management.opentelemetry.logging.export.timeout=20s
                        management.opentelemetry.logging.export.otlp.ssl.bundle=otlp
                        management.opentelemetry.logging.limits.max-attribute-value-length=1024
                        management.opentelemetry.logging.limits.max-attributes=64
                        """,
                        """
                        arconia.otel.logs.processor.max-export-batch-size=256
                        arconia.otel.logs.processor.max-queue-size=1024
                        arconia.otel.logs.processor.schedule-delay=2s
                        arconia.otel.logs.processor.export-timeout=20s
                        arconia.otel.logs.exporter.otlp.ssl.bundle=otlp
                        arconia.otel.logs.limits.max-attribute-value-length=1024
                        arconia.otel.logs.limits.max-number-of-attributes=64
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
                        management.otlp.metrics.export.compression-mode=gzip
                        management.otlp.metrics.export.ssl.bundle=otlp
                        """,
                        """
                        arconia.otel.metrics.exporter.otlp.compression=gzip
                        arconia.otel.metrics.exporter.otlp.ssl.bundle=otlp
                        """,
                        s -> s.path("src/main/resources/application.properties")
                )
        );
    }

    @Test
    void commentUnsupportedProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        management.opentelemetry.map-environment-variables=false
                        management.otlp.metrics.export.publish-max-gauge-for-histograms=true
                        """,
                        """
                        # Not supported by Arconia OpenTelemetry, which always maps the OpenTelemetry environment variables.
                        # management.opentelemetry.map-environment-variables=false
                        # Not supported by Arconia OpenTelemetry.
                        # management.otlp.metrics.export.publish-max-gauge-for-histograms=true
                        """,
                        s -> s.path("src/main/resources/application.properties")
                )
        );
    }

    @Test
    void includesSpringBoot4_0Migration() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        management.opentelemetry.tracing.export.otlp.endpoint=http://localhost:4318/v1/traces
                        management.otlp.metrics.export.url=http://localhost:4318/v1/metrics
                        """,
                        """
                        arconia.otel.traces.exporter.otlp.endpoint=http://localhost:4318/v1/traces
                        arconia.otel.metrics.exporter.otlp.endpoint=http://localhost:4318/v1/metrics
                        """,
                        s -> s.path("src/main/resources/application.properties")
                )
        );
    }

    @Test
    void renameYamlProperties() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        management:
                          opentelemetry:
                            tracing:
                              sampler: always_on
                              limits:
                                max-attributes: 64
                        """,
                        """
                        arconia.otel.traces.sampling.strategy: always_on
                        arconia.otel.traces.limits.max-number-of-attributes: 64
                        """,
                        s -> s.path("src/main/resources/application.yml")
                )
        );
    }

}
