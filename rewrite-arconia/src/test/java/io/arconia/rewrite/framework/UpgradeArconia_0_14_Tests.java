package io.arconia.rewrite.framework;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.java.Assertions.srcMainJava;
import static org.openrewrite.properties.Assertions.properties;

class UpgradeArconia_0_14_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.UpgradeArconia_0_14")
            .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
            "arconia-dev-services-mariadb-0.13",
            "arconia-dev-services-rabbitmq-0.13",
            "arconia-multitenancy-spring-boot-autoconfigure-0.13",
            "arconia-opentelemetry-sdk-spring-boot-autoconfigure-0.13",
            "arconia-opentelemetry-instrumentation-spring-boot-autoconfigure-0.13"));
    }

    /********************
     * Dev Services
     ********************/

    @Test
    void changeDevServicesConnectionsDependency() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                //language=groovy
                buildGradle(
                        """
                          plugins {
                              id 'java-library'
                          }

                          repositories {
                              mavenCentral()
                          }

                          dependencies {
                              implementation 'io.arconia:arconia-dev-services-connections'
                          }
                          """,
                        """
                          plugins {
                              id 'java-library'
                          }

                          repositories {
                              mavenCentral()
                          }

                          dependencies {
                              implementation 'io.arconia:arconia-opentelemetry'
                          }
                          """
                )
        );
    }

    @Test
    void changeMariaDbDevServicesAutoConfigurationType() {
        rewriteRun(
                mavenProject("project",
                        srcMainJava(
                                //language=java
                                java(
                                        """
                                        package com.yourorg;

                                        import io.arconia.dev.services.mariadb.MariaDBDevServicesAutoConfiguration;

                                        class Demo {
                                            MariaDBDevServicesAutoConfiguration config = null;
                                        }
                                        """,
                                        """
                                        package com.yourorg;

                                        import io.arconia.dev.services.mariadb.MariaDbDevServicesAutoConfiguration;

                                        class Demo {
                                            MariaDbDevServicesAutoConfiguration config = null;
                                        }
                                        """
                                )
                        )
                )
        );
    }

    @Test
    void changeRabbitMqDevServicesAutoConfigurationType() {
        rewriteRun(
                mavenProject("project",
                        srcMainJava(
                                //language=java
                                java(
                                        """
                                        package com.yourorg;

                                        import io.arconia.dev.services.rabbitmq.RabbitMQDevServicesAutoConfiguration;

                                        class Demo {
                                            RabbitMQDevServicesAutoConfiguration config = null;
                                        }
                                        """,
                                        """
                                        package com.yourorg;

                                        import io.arconia.dev.services.rabbitmq.RabbitMqDevServicesAutoConfiguration;

                                        class Demo {
                                            RabbitMqDevServicesAutoConfiguration config = null;
                                        }
                                        """
                                )
                        )
                )
        );
    }

    /********************
     * Multitenancy
     ********************/

    @Test
    void changeMultitenancyDependencies() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                //language=groovy
                buildGradle(
                    """
                      plugins {
                          id 'java-library'
                      }

                      repositories {
                          mavenCentral()
                      }

                      dependencies {
                          implementation 'io.arconia:arconia-multitenancy-spring-boot-autoconfigure'
                      }
                      """,
                    """
                      plugins {
                          id 'java-library'
                      }

                      repositories {
                          mavenCentral()
                      }

                      dependencies {
                          implementation 'io.arconia:arconia-multitenancy-core'
                      }
                      """
                )
        );
    }

    @Test
    void changeMultitenancyCorePackage() {
        rewriteRun(
                mavenProject("project",
                        srcMainJava(
                                //language=java
                                java(
                                        """
                                        package com.yourorg;

                                        import io.arconia.multitenancy.autoconfigure.core.MultitenancyCoreAutoConfiguration;

                                        class Demo {
                                            MultitenancyCoreAutoConfiguration config = null;
                                        }
                                        """,
                                        """
                                        package com.yourorg;

                                        import io.arconia.multitenancy.core.autoconfigure.MultitenancyCoreAutoConfiguration;

                                        class Demo {
                                            MultitenancyCoreAutoConfiguration config = null;
                                        }
                                        """
                                )
                        )
                )
        );
    }

    @Test
    void changeMultitenancyWebPackage() {
        rewriteRun(
                mavenProject("project",
                        srcMainJava(
                                //language=java
                                java(
                                        """
                                        package com.yourorg;

                                        import io.arconia.multitenancy.autoconfigure.web.MultitenancyWebAutoConfiguration;

                                        class Demo {
                                            MultitenancyWebAutoConfiguration config = null;
                                        }
                                        """,
                                        """
                                        package com.yourorg;

                                        import io.arconia.multitenancy.web.autoconfigure.MultitenancyWebAutoConfiguration;

                                        class Demo {
                                            MultitenancyWebAutoConfiguration config = null;
                                        }
                                        """
                                )
                        )
                )
        );
    }

    /********************
     * OpenTelemetry
     ********************/

    @Test
    void changeOpenTelemetrySdkDependency() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                //language=groovy
                buildGradle(
                        """
                          plugins {
                              id 'java-library'
                          }

                          repositories {
                              mavenCentral()
                          }

                          dependencies {
                              implementation 'io.arconia:arconia-opentelemetry-sdk-spring-boot-autoconfigure'
                          }
                          """,
                        """
                          plugins {
                              id 'java-library'
                          }

                          repositories {
                              mavenCentral()
                          }

                          dependencies {
                              implementation 'io.arconia:arconia-opentelemetry'
                          }
                          """
                )
        );
    }

    @Test
    void changeOpenTelemetrySdkPackage() {
        rewriteRun(
                mavenProject("project",
                        srcMainJava(
                                //language=java
                                java(
                                        """
                                        package com.yourorg;

                                        import io.arconia.opentelemetry.autoconfigure.sdk.OpenTelemetryAutoConfiguration;

                                        class Demo {
                                            OpenTelemetryAutoConfiguration config = null;
                                        }
                                        """,
                                        """
                                        package com.yourorg;

                                        import io.arconia.opentelemetry.autoconfigure.OpenTelemetryAutoConfiguration;

                                        class Demo {
                                            OpenTelemetryAutoConfiguration config = null;
                                        }
                                        """
                                )
                        )
                )
        );
    }

    @Test
    void changeLogbackAppenderInstrumentationTypes() {
        rewriteRun(
                mavenProject("project",
                        srcMainJava(
                                //language=java
                                java(
                                        """
                                        package com.yourorg;

                                        import io.arconia.opentelemetry.autoconfigure.instrumentation.logback.LogbackAppenderInstrumentationAutoConfiguration;
                                        import io.arconia.opentelemetry.autoconfigure.instrumentation.logback.LogbackAppenderProperties;

                                        class Demo {
                                            LogbackAppenderInstrumentationAutoConfiguration config = null;
                                            LogbackAppenderProperties properties = null;
                                        }
                                        """,
                                        """
                                        package com.yourorg;

                                        import io.arconia.opentelemetry.logback.autoconfigure.LogbackOpenTelemetryBridgeAutoConfiguration;
                                        import io.arconia.opentelemetry.logback.autoconfigure.LogbackOpenTelemetryBridgeProperties;

                                        class Demo {
                                            LogbackOpenTelemetryBridgeAutoConfiguration config = null;
                                            LogbackOpenTelemetryBridgeProperties properties = null;
                                        }
                                        """
                                )
                        )
                )
        );
    }

    @Test
    void changeMicrometerInstrumentationType() {
        rewriteRun(
                mavenProject("project",
                        srcMainJava(
                                //language=java
                                java(
                                        """
                                        package com.yourorg;

                                        import io.arconia.opentelemetry.autoconfigure.instrumentation.micrometer.MicrometerInstrumentationAutoConfiguration;
                                        import io.arconia.opentelemetry.autoconfigure.instrumentation.micrometer.MicrometerProperties;

                                        class Demo {
                                            MicrometerInstrumentationAutoConfiguration config = null;
                                            MicrometerProperties properties = null;
                                        }
                                        """,
                                        """
                                        package com.yourorg;

                                        import io.arconia.opentelemetry.micrometer.metrics.autoconfigure.MicrometerMetricsOpenTelemetryBridgeAutoConfiguration;
                                        import io.arconia.opentelemetry.micrometer.metrics.autoconfigure.MicrometerMetricsOpenTelemetryBridgeProperties;

                                        class Demo {
                                            MicrometerMetricsOpenTelemetryBridgeAutoConfiguration config = null;
                                            MicrometerMetricsOpenTelemetryBridgeProperties properties = null;
                                        }
                                        """
                                )
                        )
                )
        );
    }

    /********************
     * Spring Boot
     ********************/

    @Test
    void changeSpringBootAutoConfigureDependency() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                //language=groovy
                buildGradle(
                        """
                          plugins {
                              id 'java-library'
                          }

                          repositories {
                              mavenCentral()
                          }

                          dependencies {
                              implementation 'io.arconia:arconia-spring-boot-autoconfigure'
                          }
                          """,
                        """
                          plugins {
                              id 'java-library'
                          }

                          repositories {
                              mavenCentral()
                          }

                          dependencies {
                              implementation 'io.arconia:arconia-spring-boot'
                          }
                          """
                )
        );
    }

    /********************
     * Properties
     ********************/

    @Test
    void renameLogbackBridgeProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.otel.instrumentation.logback-appender.enabled=true
                        arconia.otel.instrumentation.logback-appender.capture-arguments=false
                        arconia.otel.instrumentation.logback-appender.capture-code-attributes=true
                        arconia.otel.instrumentation.logback-appender.capture-experimental-attributes=false
                        arconia.otel.instrumentation.logback-appender.capture-key-value-pair-attributes=true
                        arconia.otel.instrumentation.logback-appender.capture-logger-context=false
                        arconia.otel.instrumentation.logback-appender.capture-logstash-attributes=true
                        arconia.otel.instrumentation.logback-appender.capture-marker-attribute=false
                        arconia.otel.instrumentation.logback-appender.capture-mdc-attributes=true
                        arconia.otel.instrumentation.logback-appender.num-logs-captured-before-otel-install=1000
                        """,
                        """
                        arconia.otel.logs.logback-bridge.enabled=true
                        arconia.otel.logs.logback-bridge.capture-arguments=false
                        arconia.otel.logs.logback-bridge.capture-code-attributes=true
                        arconia.otel.logs.logback-bridge.capture-experimental-attributes=false
                        arconia.otel.logs.logback-bridge.capture-key-value-pair-attributes=true
                        arconia.otel.logs.logback-bridge.capture-logger-context=false
                        arconia.otel.logs.logback-bridge.capture-logstash-attributes=true
                        arconia.otel.logs.logback-bridge.capture-marker-attribute=false
                        arconia.otel.logs.logback-bridge.capture-mdc-attributes=true
                        arconia.otel.logs.logback-bridge.num-logs-captured-before-otel-install=1000
                        """,
                        s -> s.path("src/main/resources/application.properties")
                )
        );
    }

    @Test
    void renameMicrometerBridgeProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.otel.instrumentation.micrometer.enabled=true
                        arconia.otel.instrumentation.micrometer.base-time-unit=MILLISECONDS
                        arconia.otel.instrumentation.micrometer.histogram-gauges=true
                        """,
                        """
                        arconia.otel.metrics.micrometer-bridge.enabled=true
                        arconia.otel.metrics.micrometer-bridge.base-time-unit=MILLISECONDS
                        arconia.otel.metrics.micrometer-bridge.histogram-gauges=true
                        """,
                        s -> s.path("src/main/resources/application.properties")
                )
        );
    }

    @Test
    void renameLogsLimitsProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.otel.logs.logs-limits.max-attribute-value-length=1024
                        arconia.otel.logs.logs-limits.max-number-of-attributes=128
                        """,
                        """
                        arconia.otel.logs.limits.max-attribute-value-length=1024
                        arconia.otel.logs.limits.max-number-of-attributes=128
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
                        arconia.otel.metrics.exemplar-filter=TRACE_BASED
                        arconia.otel.metrics.interval=30s
                        """,
                        """
                        arconia.otel.metrics.exemplars.filter=TRACE_BASED
                        arconia.otel.metrics.exporter.interval=30s
                        """,
                        s -> s.path("src/main/resources/application.properties")
                )
        );
    }

    @Test
    void renameTracesLimitsProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.otel.traces.span-limits.max-attribute-value-length=1024
                        arconia.otel.traces.span-limits.max-number-of-attributes=128
                        arconia.otel.traces.span-limits.max-number-of-events=128
                        arconia.otel.traces.span-limits.max-number-of-links=128
                        arconia.otel.traces.span-limits.max-number-of-attributes-per-event=128
                        arconia.otel.traces.span-limits.max-number-of-attributes-per-link=128
                        """,
                        """
                        arconia.otel.traces.limits.max-attribute-value-length=1024
                        arconia.otel.traces.limits.max-number-of-attributes=128
                        arconia.otel.traces.limits.max-number-of-events=128
                        arconia.otel.traces.limits.max-number-of-links=128
                        arconia.otel.traces.limits.max-number-of-attributes-per-event=128
                        arconia.otel.traces.limits.max-number-of-attributes-per-link=128
                        """,
                        s -> s.path("src/main/resources/application.properties")
                )
        );
    }

    @Test
    void renameCompatibilityProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.otel.compatibility.opentelemetry=true
                        """,
                        """
                        arconia.otel.compatibility.environment-variable-specification=true
                        """,
                        s -> s.path("src/main/resources/application.properties")
                )
        );
    }

    @Test
    void commentRemovedProperties() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.otel.instrumentation.resource.enabled=true
                        arconia.otel.resource.contributors.container.enabled=false
                        arconia.otel.resource.contributors.host-id.enabled=true
                        arconia.otel.compatibility.actuator=true
                        """,
                        """
                        # Removed. Check the Arconia documentation for options to bring back this experimental functionality.
                        # arconia.otel.instrumentation.resource.enabled=true
                        # Removed. Check the Arconia documentation for options to bring back this experimental functionality.
                        # arconia.otel.resource.contributors.container.enabled=false
                        # Removed. Check the Arconia documentation for options to bring back this experimental functionality.
                        # arconia.otel.resource.contributors.host-id.enabled=true
                        # Removed. Check the OpenTelemetry Migration Guides in the Arconia documentation for alternative options.
                        # arconia.otel.compatibility.actuator=true
                        """,
                        s -> s.path("src/main/resources/application.properties")
                )
        );
    }

}
