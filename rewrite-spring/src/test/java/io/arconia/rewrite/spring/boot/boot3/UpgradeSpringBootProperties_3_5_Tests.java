package io.arconia.rewrite.spring.boot.boot3;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

/**
 * Tests for "io.arconia.rewrite.spring.boot.UpgradeSpringBootProperties_3_5".
 */
class UpgradeSpringBootProperties_3_5_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot.UpgradeSpringBootProperties_3_5");
    }

    @Test
    void shouldRenameMvcAndCodecProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.mvc.converters.preferred-json-mapper=jackson
                        spring.codec.log-request-details=true
                        spring.codec.max-in-memory-size=256KB
                        """,
                        """
                        spring.http.converters.preferred-json-mapper=jackson
                        spring.http.codecs.log-request-details=true
                        spring.http.codecs.max-in-memory-size=256KB
                        """,
                        s -> s.path("src/main/resources/application.properties")),
                //language=yaml
                yaml(
                        """
                        spring:
                          mvc:
                            converters:
                              preferred-json-mapper: jackson
                          codec:
                            log-request-details: true
                            max-in-memory-size: 256KB
                        """,
                        """
                        spring:
                          http.converters.preferred-json-mapper: jackson
                          http.codecs.log-request-details: true
                          http.codecs.max-in-memory-size: 256KB
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void shouldRenameGraphQLProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.graphql.path=/graphql
                        spring.graphql.sse.timeout=30s
                        """,
                        """
                        spring.graphql.http.path=/graphql
                        spring.graphql.http.sse.timeout=30s
                        """,
                        s -> s.path("src/main/resources/application.properties")),
                //language=yaml
                yaml(
                        """
                        spring:
                          graphql:
                            path: /graphql
                            sse:
                              timeout: 30s
                        """,
                        """
                        spring:
                          graphql:
                              http.path: /graphql
                              http.sse.timeout: 30s
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void shouldRenameGroovyTemplateProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.groovy.template.configuration.auto-escape=true
                        spring.groovy.template.configuration.auto-indent=true
                        spring.groovy.template.configuration.auto-indent-string=
                        spring.groovy.template.configuration.auto-new-line=true
                        spring.groovy.template.configuration.base-template-class=groovy.text.markup.BaseTemplate
                        spring.groovy.template.configuration.cache-templates=true
                        spring.groovy.template.configuration.declaration-encoding=UTF-8
                        spring.groovy.template.configuration.expand-empty-elements=false
                        spring.groovy.template.configuration.locale=en_US
                        spring.groovy.template.configuration.new-line-string=\\n
                        spring.groovy.template.configuration.resource-loader-path=classpath:/templates/
                        spring.groovy.template.configuration.use-double-quotes=false
                        """,
                        """
                        spring.groovy.template.auto-escape=true
                        spring.groovy.template.auto-indent=true
                        spring.groovy.template.auto-indent-string=
                        spring.groovy.template.auto-new-line=true
                        spring.groovy.template.base-template-class=groovy.text.markup.BaseTemplate
                        spring.groovy.template.cache=true
                        spring.groovy.template.declaration-encoding=UTF-8
                        spring.groovy.template.expand-empty-elements=false
                        spring.groovy.template.locale=en_US
                        spring.groovy.template.new-line-string=\\n
                        spring.groovy.template.resource-loader-path=classpath:/templates/
                        spring.groovy.template.use-double-quotes=false
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void shouldRenameOpenTelemetryProperties() {
        rewriteRun(
//                //language=properties
//                properties(
//                        """
//                        management.otlp.metrics.export.resource-attributes.service.name=my-service
//                        management.otlp.metrics.export.resource-attributes.service.version=1.0.0
//                        """,
//                        """
//                        management.opentelemetry.resource-attributes.service.name=my-service
//                        management.opentelemetry.resource-attributes.service.version=1.0.0
//                        """,
//                        s -> s.path("src/main/resources/application.properties")),
                //language=yaml
                yaml(
                        """
                        management:
                          otlp:
                            metrics:
                              export:
                                resource-attributes:
                                  service:
                                    name: my-service
                        """,
                        """
                        management:
                          opentelemetry.resource-attributes:
                            service:
                              name: my-service
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void shouldRenamePrometheusProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        management.prometheus.metrics.export.pushgateway.base-url=http://localhost:9091
                        """,
                        """
                        # Ensure the value is in the form host:port.
                        management.prometheus.metrics.export.pushgateway.address=http://localhost:9091
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void shouldCommentDeprecatedPrometheusHistogramProperty() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        management.prometheus.metrics.export.histogram-flavor=exponential
                        """,
                        """
                        # Deprecated property removed in Spring Boot 3.5.
                        # management.prometheus.metrics.export.histogram-flavor=exponential
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void shouldCommentUnbindableProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.datasource.dbcp2.driver=com.mysql.cj.jdbc.Driver
                        spring.datasource.hikari.metrics-tracker-factory=com.example.MetricsTracker
                        spring.datasource.hikari.scheduled-executor=myExecutor
                        spring.datasource.oracleucp.connection-wait-duration-in-millis=5000
                        spring.datasource.oracleucp.hostname-resolver=com.example.HostnameResolver
                        """,
                        """
                        # Unbindable property removed in Spring Boot 3.5.
                        # spring.datasource.dbcp2.driver=com.mysql.cj.jdbc.Driver
                        # Unbindable property removed in Spring Boot 3.5.
                        # spring.datasource.hikari.metrics-tracker-factory=com.example.MetricsTracker
                        # Unbindable property removed in Spring Boot 3.5.
                        # spring.datasource.hikari.scheduled-executor=myExecutor
                        # Unbindable property removed in Spring Boot 3.5.
                        # spring.datasource.oracleucp.connection-wait-duration-in-millis=5000
                        # Unbindable property removed in Spring Boot 3.5.
                        # spring.datasource.oracleucp.hostname-resolver=com.example.HostnameResolver
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void shouldCommentDeprecatedSignalFxProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        management.signalfx.metrics.export.access-token=my-token
                        management.signalfx.metrics.export.batch-size=10000
                        management.signalfx.metrics.export.connect-timeout=1s
                        management.signalfx.metrics.export.enabled=true
                        management.signalfx.metrics.export.published-histogram-type=cumulative
                        management.signalfx.metrics.export.read-timeout=10s
                        management.signalfx.metrics.export.source=my-source
                        management.signalfx.metrics.export.step=1m
                        management.signalfx.metrics.export.uri=https://ingest.signalfx.com
                        """,
                        """
                        # Deprecated in Micrometer 1.15.0.
                        management.signalfx.metrics.export.access-token=my-token
                        # Deprecated in Micrometer 1.15.0.
                        management.signalfx.metrics.export.batch-size=10000
                        # Deprecated in Micrometer 1.15.0.
                        management.signalfx.metrics.export.connect-timeout=1s
                        # Deprecated in Micrometer 1.15.0.
                        management.signalfx.metrics.export.enabled=true
                        # Deprecated in Micrometer 1.15.0.
                        management.signalfx.metrics.export.published-histogram-type=cumulative
                        # Deprecated in Micrometer 1.15.0.
                        management.signalfx.metrics.export.read-timeout=10s
                        # Deprecated in Micrometer 1.15.0.
                        management.signalfx.metrics.export.source=my-source
                        # Deprecated in Micrometer 1.15.0.
                        management.signalfx.metrics.export.step=1m
                        # Deprecated in Micrometer 1.15.0.
                        management.signalfx.metrics.export.uri=https://ingest.signalfx.com
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

}
