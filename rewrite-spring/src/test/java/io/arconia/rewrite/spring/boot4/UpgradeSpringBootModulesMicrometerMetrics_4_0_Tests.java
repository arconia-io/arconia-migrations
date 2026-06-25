package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesMicrometerMetrics_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateMicrometerMetricsModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-actuator-3.5", "spring-boot-actuator-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.JvmMetricsAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.Log4J2MetricsAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.LogbackMetricsAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
                        import org.springframework.boot.actuate.autoconfigure.metrics.MeterValue;
                        import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAspectsAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.MetricsEndpointAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
                        import org.springframework.boot.actuate.autoconfigure.metrics.PropertiesMeterFilter;
                        import org.springframework.boot.actuate.autoconfigure.metrics.ServiceLevelObjectiveBoundary;
                        import org.springframework.boot.actuate.autoconfigure.metrics.SystemMetricsAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.export.ConditionalOnEnabledMetricsExport;
                        import org.springframework.boot.actuate.autoconfigure.metrics.export.appoptics.AppOpticsMetricsExportAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.export.atlas.AtlasMetricsExportAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.export.datadog.DatadogMetricsExportAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.export.dynatrace.DynatraceMetricsExportAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.export.elastic.ElasticMetricsExportAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.export.ganglia.GangliaMetricsExportAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.export.graphite.GraphiteMetricsExportAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.export.humio.HumioMetricsExportAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.export.influx.InfluxMetricsExportAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.export.jmx.JmxMetricsExportAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.export.kairos.KairosMetricsExportAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.export.newrelic.NewRelicMetricsExportAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.export.otlp.OtlpMetricsConnectionDetails;
                        import org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.PropertiesConfigAdapter;
                        import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.export.stackdriver.StackdriverMetricsExportAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.export.statsd.StatsdMetricsExportAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.startup.StartupTimeMetricsListenerAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.task.TaskExecutorMetricsAutoConfiguration;
                        import org.springframework.boot.actuate.metrics.MetricsEndpoint;
                        import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusPushGatewayManager;
                        import org.springframework.boot.actuate.metrics.startup.StartupTimeMetricsListener;
                        import org.springframework.boot.actuate.metrics.system.DiskSpaceMetricsBinder;

                        class Demo {
                            MetricsEndpoint metricsEndpoint = null;
                            PrometheusPushGatewayManager prometheusPushGatewayManager = null;
                            StartupTimeMetricsListener startupTimeMetricsListener = null;
                            DiskSpaceMetricsBinder diskSpaceMetricsBinder = null;
                            CompositeMeterRegistryAutoConfiguration compositeMeterRegistryAutoConfiguration = null;
                            MeterRegistryCustomizer meterRegistryCustomizer = null;
                            MeterValue meterValue = null;
                            MetricsAspectsAutoConfiguration metricsAspectsAutoConfiguration = null;
                            MetricsAutoConfiguration metricsAutoConfiguration = null;
                            MetricsEndpointAutoConfiguration metricsEndpointAutoConfiguration = null;
                            MetricsProperties metricsProperties = null;
                            PropertiesMeterFilter propertiesMeterFilter = null;
                            ServiceLevelObjectiveBoundary serviceLevelObjectiveBoundary = null;
                            JvmMetricsAutoConfiguration jvmMetricsAutoConfiguration = null;
                            Log4J2MetricsAutoConfiguration log4J2MetricsAutoConfiguration = null;
                            LogbackMetricsAutoConfiguration logbackMetricsAutoConfiguration = null;
                            StartupTimeMetricsListenerAutoConfiguration startupTimeMetricsListenerAutoConfiguration = null;
                            SystemMetricsAutoConfiguration systemMetricsAutoConfiguration = null;
                            TaskExecutorMetricsAutoConfiguration taskExecutorMetricsAutoConfiguration = null;
                            ConditionalOnEnabledMetricsExport conditionalOnEnabledMetricsExport = null;
                            AppOpticsMetricsExportAutoConfiguration appOpticsMetricsExportAutoConfiguration = null;
                            AtlasMetricsExportAutoConfiguration atlasMetricsExportAutoConfiguration = null;
                            DatadogMetricsExportAutoConfiguration datadogMetricsExportAutoConfiguration = null;
                            DynatraceMetricsExportAutoConfiguration dynatraceMetricsExportAutoConfiguration = null;
                            ElasticMetricsExportAutoConfiguration elasticMetricsExportAutoConfiguration = null;
                            GangliaMetricsExportAutoConfiguration gangliaMetricsExportAutoConfiguration = null;
                            GraphiteMetricsExportAutoConfiguration graphiteMetricsExportAutoConfiguration = null;
                            HumioMetricsExportAutoConfiguration humioMetricsExportAutoConfiguration = null;
                            InfluxMetricsExportAutoConfiguration influxMetricsExportAutoConfiguration = null;
                            JmxMetricsExportAutoConfiguration jmxMetricsExportAutoConfiguration = null;
                            KairosMetricsExportAutoConfiguration kairosMetricsExportAutoConfiguration = null;
                            NewRelicMetricsExportAutoConfiguration newRelicMetricsExportAutoConfiguration = null;
                            OtlpMetricsConnectionDetails otlpMetricsConnectionDetails = null;
                            PrometheusMetricsExportAutoConfiguration prometheusMetricsExportAutoConfiguration = null;
                            PropertiesConfigAdapter propertiesConfigAdapter = null;
                            SimpleMetricsExportAutoConfiguration simpleMetricsExportAutoConfiguration = null;
                            StackdriverMetricsExportAutoConfiguration stackdriverMetricsExportAutoConfiguration = null;
                            StatsdMetricsExportAutoConfiguration statsdMetricsExportAutoConfiguration = null;
                        }
                        """,
                        """
                        import org.springframework.boot.micrometer.metrics.autoconfigure.export.ConditionalOnEnabledMetricsExport;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.export.appoptics.AppOpticsMetricsExportAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.export.atlas.AtlasMetricsExportAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.export.datadog.DatadogMetricsExportAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.export.dynatrace.DynatraceMetricsExportAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.export.elastic.ElasticMetricsExportAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.export.ganglia.GangliaMetricsExportAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.export.graphite.GraphiteMetricsExportAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.export.humio.HumioMetricsExportAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.export.influx.InfluxMetricsExportAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.export.jmx.JmxMetricsExportAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.export.kairos.KairosMetricsExportAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.export.newrelic.NewRelicMetricsExportAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.export.otlp.OtlpMetricsConnectionDetails;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusMetricsExportAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.export.properties.PropertiesConfigAdapter;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.export.simple.SimpleMetricsExportAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.export.stackdriver.StackdriverMetricsExportAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.export.statsd.StatsdMetricsExportAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.jvm.JvmMetricsAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.logging.log4j2.Log4J2MetricsAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.logging.logback.LogbackMetricsAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.startup.StartupTimeMetricsListenerAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.system.SystemMetricsAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.task.TaskExecutorMetricsAutoConfiguration;
                        import org.springframework.boot.micrometer.metrics.actuate.endpoint.MetricsEndpoint;
                        import org.springframework.boot.micrometer.metrics.autoconfigure.*;
                        import org.springframework.boot.micrometer.metrics.export.prometheus.PrometheusPushGatewayManager;
                        import org.springframework.boot.micrometer.metrics.startup.StartupTimeMetricsListener;
                        import org.springframework.boot.micrometer.metrics.system.DiskSpaceMetricsBinder;

                        class Demo {
                            MetricsEndpoint metricsEndpoint = null;
                            PrometheusPushGatewayManager prometheusPushGatewayManager = null;
                            StartupTimeMetricsListener startupTimeMetricsListener = null;
                            DiskSpaceMetricsBinder diskSpaceMetricsBinder = null;
                            CompositeMeterRegistryAutoConfiguration compositeMeterRegistryAutoConfiguration = null;
                            MeterRegistryCustomizer meterRegistryCustomizer = null;
                            MeterValue meterValue = null;
                            MetricsAspectsAutoConfiguration metricsAspectsAutoConfiguration = null;
                            MetricsAutoConfiguration metricsAutoConfiguration = null;
                            MetricsEndpointAutoConfiguration metricsEndpointAutoConfiguration = null;
                            MetricsProperties metricsProperties = null;
                            PropertiesMeterFilter propertiesMeterFilter = null;
                            ServiceLevelObjectiveBoundary serviceLevelObjectiveBoundary = null;
                            JvmMetricsAutoConfiguration jvmMetricsAutoConfiguration = null;
                            Log4J2MetricsAutoConfiguration log4J2MetricsAutoConfiguration = null;
                            LogbackMetricsAutoConfiguration logbackMetricsAutoConfiguration = null;
                            StartupTimeMetricsListenerAutoConfiguration startupTimeMetricsListenerAutoConfiguration = null;
                            SystemMetricsAutoConfiguration systemMetricsAutoConfiguration = null;
                            TaskExecutorMetricsAutoConfiguration taskExecutorMetricsAutoConfiguration = null;
                            ConditionalOnEnabledMetricsExport conditionalOnEnabledMetricsExport = null;
                            AppOpticsMetricsExportAutoConfiguration appOpticsMetricsExportAutoConfiguration = null;
                            AtlasMetricsExportAutoConfiguration atlasMetricsExportAutoConfiguration = null;
                            DatadogMetricsExportAutoConfiguration datadogMetricsExportAutoConfiguration = null;
                            DynatraceMetricsExportAutoConfiguration dynatraceMetricsExportAutoConfiguration = null;
                            ElasticMetricsExportAutoConfiguration elasticMetricsExportAutoConfiguration = null;
                            GangliaMetricsExportAutoConfiguration gangliaMetricsExportAutoConfiguration = null;
                            GraphiteMetricsExportAutoConfiguration graphiteMetricsExportAutoConfiguration = null;
                            HumioMetricsExportAutoConfiguration humioMetricsExportAutoConfiguration = null;
                            InfluxMetricsExportAutoConfiguration influxMetricsExportAutoConfiguration = null;
                            JmxMetricsExportAutoConfiguration jmxMetricsExportAutoConfiguration = null;
                            KairosMetricsExportAutoConfiguration kairosMetricsExportAutoConfiguration = null;
                            NewRelicMetricsExportAutoConfiguration newRelicMetricsExportAutoConfiguration = null;
                            OtlpMetricsConnectionDetails otlpMetricsConnectionDetails = null;
                            PrometheusMetricsExportAutoConfiguration prometheusMetricsExportAutoConfiguration = null;
                            PropertiesConfigAdapter propertiesConfigAdapter = null;
                            SimpleMetricsExportAutoConfiguration simpleMetricsExportAutoConfiguration = null;
                            StackdriverMetricsExportAutoConfiguration stackdriverMetricsExportAutoConfiguration = null;
                            StatsdMetricsExportAutoConfiguration statsdMetricsExportAutoConfiguration = null;
                        }
                        """
                )
        );
    }

}
