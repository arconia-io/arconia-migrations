package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesMicrometerTracing_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateMicrometerTracingModule")
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
                        import org.springframework.boot.actuate.autoconfigure.tracing.ConditionalOnEnabledTracing;
                        import org.springframework.boot.actuate.autoconfigure.tracing.MicrometerTracingAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.tracing.NoopTracerAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.tracing.TracingProperties;
                        import org.springframework.boot.actuate.autoconfigure.tracing.prometheus.PrometheusExemplarsAutoConfiguration;

                        class Demo {
                            ConditionalOnEnabledTracing conditionalOnEnabledTracing = null;
                            MicrometerTracingAutoConfiguration micrometerTracingAutoConfiguration = null;
                            NoopTracerAutoConfiguration noopTracerAutoConfiguration = null;
                            TracingProperties tracingProperties = null;
                            TracingProperties.Sampling sampling = null;
                            TracingProperties.Baggage baggage = null;
                            TracingProperties.Baggage.Correlation correlation = null;
                            TracingProperties.Propagation propagation = null;
                            TracingProperties.Propagation.PropagationType propagationType = null;
                            PrometheusExemplarsAutoConfiguration prometheusExemplarsAutoConfiguration = null;
                        }
                        """,
                        """
                        import org.springframework.boot.micrometer.tracing.autoconfigure.ConditionalOnEnabledTracingExport;
                        import org.springframework.boot.micrometer.tracing.autoconfigure.MicrometerTracingAutoConfiguration;
                        import org.springframework.boot.micrometer.tracing.autoconfigure.NoopTracerAutoConfiguration;
                        import org.springframework.boot.micrometer.tracing.autoconfigure.TracingProperties;
                        import org.springframework.boot.micrometer.tracing.autoconfigure.prometheus.PrometheusExemplarsAutoConfiguration;

                        class Demo {
                            ConditionalOnEnabledTracingExport conditionalOnEnabledTracing = null;
                            MicrometerTracingAutoConfiguration micrometerTracingAutoConfiguration = null;
                            NoopTracerAutoConfiguration noopTracerAutoConfiguration = null;
                            TracingProperties tracingProperties = null;
                            TracingProperties.Sampling sampling = null;
                            TracingProperties.Baggage baggage = null;
                            TracingProperties.Baggage.Correlation correlation = null;
                            TracingProperties.Propagation propagation = null;
                            TracingProperties.Propagation.PropagationType propagationType = null;
                            PrometheusExemplarsAutoConfiguration prometheusExemplarsAutoConfiguration = null;
                        }
                        """
                )
        );
    }

}
