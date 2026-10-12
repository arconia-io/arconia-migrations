package io.arconia.rewrite.framework.arconia0;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

class UpgradeArconia_0_31_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.framework.UpgradeArconia_0_31")
            .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                "arconia-dev-services-api-0.30",
                "arconia-dev-services-core-0.30"));
    }

    @Test
    @DocumentExample
    void devServicesSpecTypeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.dev.services.core.registration.DevServicesRegistry;

                        class Demo {
                            void call(DevServicesRegistry.ServiceSpec service) {
                                service.name("fancydb");
                            }
                        }
                        """,
                        """
                        import io.arconia.dev.services.core.registration.ServiceSpec;

                        class Demo {
                            void call(ServiceSpec service) {
                                service.name("fancydb");
                            }
                        }
                        """
                )
        );
    }

    @Test
    void devServicesContainerAndDiscoverySpecTypeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.dev.services.core.registration.DevServicesRegistry;

                        class Demo {
                            void call(DevServicesRegistry.ContainerSpec container, DevServicesRegistry.DiscoverySpec discovery) {
                                container.serviceConnectionName("fancydb");
                            }
                        }
                        """,
                        """
                        import io.arconia.dev.services.core.registration.ContainerSpec;
                        import io.arconia.dev.services.core.registration.DiscoverySpec;

                        class Demo {
                            void call(ContainerSpec container, DiscoverySpec discovery) {
                                container.serviceConnectionName("fancydb");
                            }
                        }
                        """
                )
        );
    }

    @Test
    void devServicesDiscoverySharedCallRemoved() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.dev.services.core.registration.DevServicesRegistry;

                        class Demo {
                            void call(DevServicesRegistry.DiscoverySpec discovery) {
                                discovery.shared(true);
                            }

                            DevServicesRegistry.DiscoverySpec chain(DevServicesRegistry.DiscoverySpec discovery) {
                                return discovery.shared(true);
                            }
                        }
                        """,
                        """
                        import io.arconia.dev.services.core.registration.DiscoverySpec;

                        class Demo {
                            void call(DiscoverySpec discovery) {
                            }

                            DiscoverySpec chain(DiscoverySpec discovery) {
                                return discovery;
                            }
                        }
                        """
                )
        );
    }

    @Test
    void devServiceLabelsMovedToApiModule() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.dev.services.core.container.DevServiceLabels;

                        class Demo {
                            String call() {
                                return DevServiceLabels.NAME;
                            }
                        }
                        """,
                        """
                        import io.arconia.dev.services.api.registration.DevServiceLabels;

                        class Demo {
                            String call() {
                                return DevServiceLabels.NAME;
                            }
                        }
                        """
                )
        );
    }

    @Test
    void devServiceLinkProviderMethodRenamed() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.List;

                        import io.arconia.dev.services.api.registration.DevServiceLink;
                        import io.arconia.dev.services.api.registration.DevServiceLinkProvider;

                        class Demo implements DevServiceLinkProvider {
                            @Override
                            public List<DevServiceLink> devServiceLinks() {
                                return List.of();
                            }
                        }
                        """,
                        """
                        import java.util.List;

                        import io.arconia.dev.services.api.registration.DevServiceLink;
                        import io.arconia.dev.services.api.registration.DevServiceLinkProvider;

                        class Demo implements DevServiceLinkProvider {
                            @Override
                            public List<DevServiceLink> devServiceLinkDefinitions() {
                                return List.of();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void jdbcDevServicesDefaultConstantsReplaced() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.dev.services.api.config.JdbcDevServicesProperties;

                        class Demo {
                            String username = JdbcDevServicesProperties.DEFAULT_USERNAME;
                            String password = JdbcDevServicesProperties.DEFAULT_PASSWORD;
                            String dbName = JdbcDevServicesProperties.DEFAULT_DB_NAME;
                        }
                        """,
                        """
                        class Demo {
                            String username = "arconia";
                            String password = "arconia";
                            String dbName = "arconia";
                        }
                        """
                )
        );
    }

    @Test
    void startupLogLevelPropertyCommented() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.dev.services.startup.log-level=debug
                        arconia.dev.services.enabled=true
                        """,
                        """
                        # Removed as redundant. Testcontainers logs startup errors to the console already.
                        # arconia.dev.services.startup.log-level=debug
                        arconia.dev.services.enabled=true
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void startupLogLevelPropertyCommentedInYaml() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          dev:
                            services:
                              enabled: true
                              startup:
                                log-level: debug
                        """,
                        """
                        arconia:
                          dev:
                            services:
                              enabled: true
                                # Removed as redundant. Testcontainers logs startup errors to the console already.
                                # log-level: debug
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void logbackBridgeCapturePropertiesMigrated() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.otel.logs.logback-bridge.capture-key-value-pair-attributes=true
                        arconia.otel.logs.logback-bridge.capture-logstash-marker-attributes=true
                        arconia.otel.logs.logback-bridge.capture-logger-context=true
                        arconia.otel.logs.logback-bridge.capture-mdc-attributes=tenant.id,request.id
                        arconia.otel.logs.logback-bridge.capture-marker-attribute=true
                        """,
                        """
                        # Replaced by the 'structured-attributes' selector, which also covers Logstash markers and structured arguments. Use 'structured-attributes.included: "*"' to capture all key-value pairs.
                        # arconia.otel.logs.logback-bridge.capture-key-value-pair-attributes=true
                        # Replaced by the 'structured-attributes' selector, which also covers SLF4J key-value pairs and Logstash structured arguments. Use 'structured-attributes.included: "*"' to capture all Logstash marker attributes.
                        # arconia.otel.logs.logback-bridge.capture-logstash-marker-attributes=true
                        # Replaced by the 'logger-context-attributes' selector. Use 'logger-context-attributes.included: "*"' to capture all logger context properties.
                        # arconia.otel.logs.logback-bridge.capture-logger-context=true
                        arconia.otel.logs.logback-bridge.mdc-attributes.included=tenant.id,request.id
                        arconia.otel.logs.logback-bridge.capture-marker-attribute=true
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void logbackBridgeCapturePropertiesMigratedInYaml() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          otel:
                            logs:
                              logback-bridge:
                                capture-logger-context: true
                                capture-mdc-attributes: "*"
                        """,
                        """
                        arconia:
                          otel:
                            logs:
                              logback-bridge:
                                # Replaced by the 'logger-context-attributes' selector. Use 'logger-context-attributes.included: "*"' to capture all logger context properties.
                                # capture-logger-context: true
                                mdc-attributes.included: "*"
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void reuseAndSharedPropertiesCommented() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.dev.services.rabbitmq.shared=false
                        arconia.dev.services.postgresql.reuse=true
                        arconia.dev.services.enabled=true
                        """,
                        """
                        # Replaced by 'reuse-strategy'. Use 'reuse-strategy: none' instead of 'shared: false'. Sharing ('framework') is the default.
                        # arconia.dev.services.rabbitmq.shared=false
                        # Replaced by 'reuse-strategy'. Use 'reuse-strategy: testcontainers' instead of 'reuse: true'.
                        # arconia.dev.services.postgresql.reuse=true
                        arconia.dev.services.enabled=true
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

}
