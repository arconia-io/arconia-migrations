package io.arconia.rewrite.framework.arconia0;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

class UpgradeArconia_0_32_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.framework.UpgradeArconia_0_32")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "arconia-core-0.31", "arconia-spring-boot-0.31",
                        "arconia-multitenancy-core-0.30", "arconia-multitenancy-web-0.30", "jakarta.servlet-api-6.1", "micrometer-observation-1.16", "spring-web-7.0", "spring-core-7.0", "spring-beans-7.0",
                        "spring-context-7.0", "arconia-data-jdbc-sqlite-0.31", "arconia-dev-services-api-0.31", "arconia-docling-0.31",
                        "arconia-opentelemetry-ai-semantic-conventions-0.31", "arconia-opentelemetry-semantic-conventions-0.31"));
    }

    @Test
    @DocumentExample
    void keycloakRealmImportPathRenamed() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.dev.services.keycloak.realm.import-path=classpath:keycloak/realm.json
                        arconia.dev.services.keycloak.client.id=my-app
                        """,
                        """
                        arconia.dev.services.keycloak.realm.import-paths=classpath:keycloak/realm.json
                        arconia.dev.services.keycloak.client.id=my-app
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void keycloakRealmImportPathRenamedInYaml() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          dev:
                            services:
                              keycloak:
                                realm:
                                  import-path: file:../keycloak/realm-config.json
                                client:
                                  enabled: false
                        """,
                        """
                        arconia:
                          dev:
                            services:
                              keycloak:
                                realm:
                                  import-paths: file:../keycloak/realm-config.json
                                client:
                                  enabled: false
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void doclingApiKeyHeaderNameMoved() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import io.arconia.docling.autoconfigure.DoclingProperties;

                        class DoclingHeaders {

                            String apiKeyHeader() {
                                return DoclingProperties.API_KEY_HEADER_NAME;
                            }

                        }
                        """,
                        """
                        package com.yourorg;

                        import io.arconia.docling.client.DoclingServeClient;

                        class DoclingHeaders {

                            String apiKeyHeader() {
                                return DoclingServeClient.API_KEY_HEADER_NAME;
                            }

                        }
                        """)
        );
    }

    @Test
    void doclingEnableUiRenamed() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.dev.services.docling.enable-ui=false
                        """,
                        """
                        arconia.dev.services.docling.ui-enabled=false
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void otelExemplarsDisabledMigratedToFilter() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.otel.metrics.exemplars.enabled=false
                        arconia.otel.metrics.cardinality-limit=500
                        """,
                        """
                        arconia.otel.metrics.exemplars.filter=always-off
                        arconia.otel.metrics.cardinality-limit=500
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void otelExemplarsEnabledRemoved() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.otel.metrics.exemplars.enabled=true
                        arconia.otel.metrics.exemplars.filter=always-on
                        """,
                        """
                        arconia.otel.metrics.exemplars.filter=always-on
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void otelExemplarsDisabledMigratedToFilterInYaml() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          otel:
                            metrics:
                              exemplars:
                                enabled: false
                                filter: always-on
                        """,
                        """
                        arconia:
                          otel:
                            metrics:
                              exemplars:
                                filter: always-off
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void httpConventionsIncludeUrlQueryRenamed() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.observations.conventions.opentelemetry.http.include-url-query=true
                        """,
                        """
                        arconia.observations.conventions.opentelemetry.http.capture-url-query=true
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void aiConventionsToolPropertiesRenamed() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.observations.conventions.opentelemetry.ai.include-tool-definitions=true
                        arconia.observations.conventions.opentelemetry.ai.include-tool-call-content=false
                        """,
                        """
                        arconia.observations.conventions.opentelemetry.ai.capture-tool-definitions=true
                        arconia.observations.conventions.opentelemetry.ai.capture-tool-call-content=false
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void aiConventionsCaptureContentNoneMigrated() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.observations.conventions.opentelemetry.ai.capture-content=none
                        """,
                        """
                        arconia.observations.conventions.opentelemetry.ai.capture-message-content=false
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void aiConventionsCaptureContentSpanAttributesMigrated() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.observations.conventions.opentelemetry.ai.capture-content=SPAN_ATTRIBUTES
                        """,
                        """
                        arconia.observations.conventions.opentelemetry.ai.capture-message-content=true
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void aiConventionsCaptureContentSpanEventsMigrated() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.observations.conventions.opentelemetry.ai.capture-content=span-events
                        """,
                        """
                        arconia.observations.conventions.opentelemetry.ai.inference-content-destination=span-events
                        arconia.observations.conventions.opentelemetry.ai.capture-message-content=true
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void aiConventionsCaptureContentSpanEventsMigratedInYaml() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          observations:
                            conventions:
                              opentelemetry:
                                ai:
                                  capture-content: span-events
                                  include-tool-definitions: true
                        """,
                        """
                        arconia:
                          observations:
                            conventions:
                              opentelemetry:
                                ai:
                                  capture-tool-definitions: true
                                  inference-content-destination: span-events
                                  capture-message-content: true
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    @Disabled("Enable once Arconia 0.32 is released: the recipe then upgrades the renamed artifacts to 0.32.x, "
            + "which needs their metadata on Maven Central")
    void coreModulesRenamed() {
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
                            implementation "io.arconia:arconia-core"
                            implementation "io.arconia:arconia-spring-boot"
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
                            implementation "io.arconia:arconia-core-annotations"
                            implementation "io.arconia:arconia-core-bootstrap"
                        }
                        """
                )
        );
    }

    @Test
    void corePackagesRenamed() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import io.arconia.boot.autoconfigure.bootstrap.ConditionalOnDevMode;
                        import io.arconia.boot.bootstrap.BootstrapMode;
                        import io.arconia.core.support.Incubating;

                        @Incubating
                        class Demo {
                            @ConditionalOnDevMode
                            BootstrapMode mode() {
                                return BootstrapMode.detect();
                            }
                        }
                        """,
                        """
                        package com.yourorg;

                        import io.arconia.core.bootstrap.autoconfigure.ConditionalOnDevMode;
                        import io.arconia.core.bootstrap.BootstrapMode;
                        import io.arconia.core.annotations.Incubating;

                        @Incubating
                        class Demo {
                            @ConditionalOnDevMode
                            BootstrapMode mode() {
                                return BootstrapMode.detect();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void sqliteDialectRenamed() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import io.arconia.data.jdbc.sqlite.dialect.JdbcSqliteDialect;
                        import io.arconia.data.jdbc.sqlite.dialect.SqliteDialect;

                        class DialectHolder {

                            private final SqliteDialect dialect = JdbcSqliteDialect.INSTANCE;

                        }
                        """,
                        """
                        package com.yourorg;

                        import io.arconia.data.jdbc.sqlite.dialect.JdbcSqliteDialect;

                        class DialectHolder {

                            private final JdbcSqliteDialect dialect = JdbcSqliteDialect.INSTANCE;

                        }
                        """
                )
        );
    }

    @Test
    void multitenancyPackagesRenamed() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import io.arconia.multitenancy.core.context.TenantContext;
                        import io.arconia.multitenancy.core.context.events.TenantContextAttachedEvent;
                        import io.arconia.multitenancy.core.context.resolvers.FixedTenantResolver;
                        import io.arconia.multitenancy.core.exceptions.TenantNotFoundException;
                        import io.arconia.multitenancy.core.exceptions.TenantVerificationException;
                        import io.arconia.multitenancy.core.tenantdetails.DefaultTenantIdentifierValidator;
                        import io.arconia.multitenancy.core.tenantdetails.TenantDetailsService;
                        import io.arconia.multitenancy.core.tenantdetails.TenantVerifier;
                        import io.arconia.multitenancy.web.context.annotations.TenantIdentifier;
                        import io.arconia.multitenancy.web.context.filters.TenantContextFilter;
                        import io.arconia.multitenancy.web.context.resolvers.HeaderTenantResolver;
                        import io.arconia.multitenancy.web.context.resolvers.HttpRequestTenantResolver;

                        class Demo {
                            FixedTenantResolver fixed = FixedTenantResolver.builder().build();
                            HttpRequestTenantResolver header = HeaderTenantResolver.builder().build();
                            TenantContextFilter filter = null;
                            TenantDetailsService tenantDetailsService = null;
                            TenantVerifier verifier = null;
                            DefaultTenantIdentifierValidator validator = DefaultTenantIdentifierValidator.builder().build();

                            String tenant(@TenantIdentifier String tenantId, TenantContextAttachedEvent event) {
                                try {
                                    return TenantContext.getRequiredTenantIdentifier();
                                }
                                catch (TenantNotFoundException | TenantVerificationException ex) {
                                    return tenantId;
                                }
                            }
                        }
                        """,
                        """
                        package com.yourorg;

                        import io.arconia.multitenancy.core.context.TenantContext;
                        import io.arconia.multitenancy.core.context.TenantContextAttachedEvent;
                        import io.arconia.multitenancy.core.context.TenantNotFoundException;
                        import io.arconia.multitenancy.core.resolution.DefaultTenantIdentifierValidator;
                        import io.arconia.multitenancy.core.resolution.FixedTenantResolver;
                        import io.arconia.multitenancy.core.resolution.TenantVerificationException;
                        import io.arconia.multitenancy.core.resolution.TenantVerifier;
                        import io.arconia.multitenancy.core.tenantdetails.TenantDetailsService;
                        import io.arconia.multitenancy.web.context.TenantIdentifier;
                        import io.arconia.multitenancy.web.context.TenantContextFilter;
                        import io.arconia.multitenancy.web.resolution.HeaderTenantResolver;
                        import io.arconia.multitenancy.web.resolution.HttpRequestTenantResolver;

                        class Demo {
                            FixedTenantResolver fixed = FixedTenantResolver.builder().build();
                            HttpRequestTenantResolver header = HeaderTenantResolver.builder().build();
                            TenantContextFilter filter = null;
                            TenantDetailsService tenantDetailsService = null;
                            TenantVerifier verifier = null;
                            DefaultTenantIdentifierValidator validator = DefaultTenantIdentifierValidator.builder().build();

                            String tenant(@TenantIdentifier String tenantId, TenantContextAttachedEvent event) {
                                try {
                                    return TenantContext.getRequiredTenantIdentifier();
                                }
                                catch (TenantNotFoundException | TenantVerificationException ex) {
                                    return tenantId;
                                }
                            }
                        }
                        """
                )
        );
    }

    @Test
    void propertiesTenantDetailsServiceRenamed() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import io.arconia.multitenancy.core.autoconfigure.PropertiesTenantDetailsService;

                        class Demo {
                            PropertiesTenantDetailsService service = null;
                        }
                        """,
                        """
                        package com.yourorg;

                        import io.arconia.multitenancy.core.tenantdetails.InMemoryTenantDetailsService;

                        class Demo {
                            InMemoryTenantDetailsService service = null;
                        }
                        """
                )
        );
    }

    @Test
    void defaultTenantKeyGeneratorRenamed() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import io.arconia.multitenancy.core.cache.DefaultTenantKeyGenerator;

                        class Demo {
                            DefaultTenantKeyGenerator keyGenerator = new DefaultTenantKeyGenerator();
                        }
                        """,
                        """
                        package com.yourorg;

                        import io.arconia.multitenancy.core.cache.TenantKeyGenerator;

                        class Demo {
                            TenantKeyGenerator keyGenerator = new TenantKeyGenerator();
                        }
                        """
                )
        );
    }

    @Test
    void multitenancyResolutionPropertiesRenamed() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.multitenancy.resolution.http.header.header-name=X-Custom-Tenant
                        arconia.multitenancy.resolution.http.cookie.cookie-name=MY-TENANT
                        """,
                        """
                        arconia.multitenancy.resolution.http.header.name=X-Custom-Tenant
                        arconia.multitenancy.resolution.http.cookie.name=MY-TENANT
                        """,
                        spec -> spec.path("src/main/resources/application.properties")
                )
        );
    }

    @Test
    void multitenancyBuilderMethodsRenamed() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import io.arconia.multitenancy.core.observability.MdcTenantEventListener;
                        import io.arconia.multitenancy.core.observability.TenantObservationFilter;
                        import io.arconia.multitenancy.web.context.resolvers.CookieTenantResolver;
                        import io.arconia.multitenancy.web.context.resolvers.HeaderTenantResolver;

                        class Demo {
                            void configure() {
                                var header = HeaderTenantResolver.builder().tenantHeaderName("X-Tenant").build();
                                var cookie = CookieTenantResolver.builder().tenantCookieName("TENANT").build();
                                var filter = TenantObservationFilter.builder().tenantIdentifierKey("tenant").build();
                                var listener = MdcTenantEventListener.builder().tenantIdentifierKey("tenant").build();
                                String names = header.getTenantHeaderName() + cookie.getTenantCookieName()
                                        + filter.getTenantIdentifierKey() + listener.getTenantIdentifierKey();
                            }
                        }
                        """,
                        """
                        package com.yourorg;

                        import io.arconia.multitenancy.core.observability.MdcTenantEventListener;
                        import io.arconia.multitenancy.core.observability.TenantObservationFilter;
                        import io.arconia.multitenancy.web.resolution.CookieTenantResolver;
                        import io.arconia.multitenancy.web.resolution.HeaderTenantResolver;

                        class Demo {
                            void configure() {
                                var header = HeaderTenantResolver.builder().headerName("X-Tenant").build();
                                var cookie = CookieTenantResolver.builder().cookieName("TENANT").build();
                                var filter = TenantObservationFilter.builder().keyName("tenant").build();
                                var listener = MdcTenantEventListener.builder().keyName("tenant").build();
                                String names = header.getHeaderName() + cookie.getCookieName()
                                        + filter.getKeyName() + listener.getKeyName();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void multitenancyFilterBuilderAndPropertiesMethodsRenamed() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import io.arconia.multitenancy.web.autoconfigure.HttpTenantResolutionProperties;
                        import io.arconia.multitenancy.web.context.filters.TenantContextFilter;
                        import io.arconia.multitenancy.web.context.resolvers.HeaderTenantResolver;

                        class Demo {
                            void configure(HttpTenantResolutionProperties properties) {
                                properties.getHeader().setHeaderName("X-Tenant");
                                var builder = TenantContextFilter.builder()
                                        .httpRequestTenantResolver(HeaderTenantResolver.builder().build());
                                String name = properties.getHeader().getHeaderName() + properties.getCookie().getCookieName();
                            }
                        }
                        """,
                        """
                        package com.yourorg;

                        import io.arconia.multitenancy.web.autoconfigure.HttpTenantResolutionProperties;
                        import io.arconia.multitenancy.web.context.TenantContextFilter;
                        import io.arconia.multitenancy.web.resolution.HeaderTenantResolver;

                        class Demo {
                            void configure(HttpTenantResolutionProperties properties) {
                                properties.getHeader().setName("X-Tenant");
                                var builder = TenantContextFilter.builder()
                                        .tenantResolver(HeaderTenantResolver.builder().build());
                                String name = properties.getHeader().getName() + properties.getCookie().getName();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void fixedTenantResolutionEnabledCommented() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.multitenancy.resolution.fixed.enabled=true
                        arconia.multitenancy.resolution.fixed.tenant-identifier=acme
                        """,
                        """
                        # Removed. The fixed tenant is now a resolution mode. Set 'arconia.multitenancy.resolution.http.resolution-mode' to 'fixed' for a single-tenant deployment, or list 'fixed' last, as in 'header, fixed', to use it as the default tenant.
                        # arconia.multitenancy.resolution.fixed.enabled=true
                        arconia.multitenancy.resolution.fixed.tenant-identifier=acme
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void openInferenceCompletionsPropertiesCommented() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.observations.conventions.openinference.ai.hide-prompts=true
                        arconia.observations.conventions.openinference.ai.hide-choices=true
                        arconia.observations.conventions.openinference.ai.hide-inputs=true
                        """,
                        """
                        # Removed. It hid the prompts of the completions API, which Spring AI doesn't use, so it had no effect. Use 'arconia.observations.conventions.openinference.ai.hide-input-messages' to hide the input messages of the chat completions API.
                        # arconia.observations.conventions.openinference.ai.hide-prompts=true
                        # Removed. It hid the choices of the completions API, which Spring AI doesn't use, so it had no effect. Use 'arconia.observations.conventions.openinference.ai.hide-output-messages' to hide the output messages of the chat completions API.
                        # arconia.observations.conventions.openinference.ai.hide-choices=true
                        arconia.observations.conventions.openinference.ai.hide-inputs=true
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void tenantDetailsHealthPropertyCommented() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        management.health.tenantdetails.enabled=false
                        """,
                        """
                        # Removed with the tenant details health indicator. The 'db' health indicator already reports whether the database holding the tenant details can be reached.
                        # management.health.tenantdetails.enabled=false
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void devServicesPropertiesRenamed() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import io.arconia.dev.services.api.config.BaseDevServicesProperties;

                        class FancyDbDevServicesProperties implements BaseDevServicesProperties {
                            @Override
                            public String getImageName() {
                                return "acme/fancydb";
                            }
                        }
                        """,
                        """
                        package com.yourorg;

                        import io.arconia.dev.services.api.config.DevServicesProperties;

                        class FancyDbDevServicesProperties implements DevServicesProperties {
                            @Override
                            public String getImageName() {
                                return "acme/fancydb";
                            }
                        }
                        """
                )
        );
    }

    @Test
    void devServiceRegistrationAccessorsRenamed() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import io.arconia.dev.services.api.registration.ContainerInfo;
                        import io.arconia.dev.services.api.registration.DevServiceRegistration;

                        class Demo {
                            void describe(DevServiceRegistration registration) {
                                String name = registration.name();
                                String description = registration.description();
                                DevServiceRegistration.Origin origin = registration.origin();
                                int links = registration.links().size();
                                String sslBundleName = registration.sslBundleName();
                                ContainerInfo containerInfo = registration.containerInfo().get();
                                String id = containerInfo.id();
                                String imageName = containerInfo.imageName();
                                int names = containerInfo.names().size();
                                int ports = containerInfo.ports().size();
                                int labels = containerInfo.labels().size();
                                String state = containerInfo.state();
                                String status = containerInfo.status();
                            }
                        }
                        """,
                        """
                        package com.yourorg;

                        import io.arconia.dev.services.api.registration.ContainerInfo;
                        import io.arconia.dev.services.api.registration.DevServiceRegistration;

                        class Demo {
                            void describe(DevServiceRegistration registration) {
                                String name = registration.getName();
                                String description = registration.getDescription();
                                DevServiceRegistration.Origin origin = registration.getOrigin();
                                int links = registration.getLinks().size();
                                String sslBundleName = registration.getSslBundleName();
                                ContainerInfo containerInfo = registration.getContainerInfo().get();
                                String id = containerInfo.getId();
                                String imageName = containerInfo.getImageName();
                                int names = containerInfo.getNames().size();
                                int ports = containerInfo.getPorts().size();
                                int labels = containerInfo.getLabels().size();
                                String state = containerInfo.getState();
                                String status = containerInfo.getStatus();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void langSmithObservationTypesReplaced() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import io.arconia.observation.opentelemetry.ai.autoconfigure.OpenTelemetryAiConventionsProperties;
                        import io.arconia.observation.opentelemetry.ai.instrumentation.langsmith.LangSmithAiAdvisorConvention;
                        import io.arconia.observation.opentelemetry.ai.instrumentation.langsmith.LangSmithChatModelObservationHandler;
                        import io.arconia.observation.opentelemetry.ai.instrumentation.langsmith.LangSmithEmbeddingModelObservationHandler;

                        class ObservationConfig {

                            LangSmithChatModelObservationHandler chatModelHandler(OpenTelemetryAiConventionsProperties properties) {
                                return new LangSmithChatModelObservationHandler(properties);
                            }

                            LangSmithEmbeddingModelObservationHandler embeddingModelHandler() {
                                return new LangSmithEmbeddingModelObservationHandler();
                            }

                            LangSmithAiAdvisorConvention advisorConvention() {
                                return new LangSmithAiAdvisorConvention();
                            }

                        }
                        """,
                        """
                        package com.yourorg;

                        import io.arconia.observation.opentelemetry.ai.autoconfigure.OpenTelemetryAiConventionsProperties;
                        import io.arconia.observation.opentelemetry.ai.instrumentation.langsmith.LangSmithAdvisorObservationConvention;
                        import io.arconia.observation.opentelemetry.ai.instrumentation.langsmith.LangSmithTokenUsageObservationHandler;

                        class ObservationConfig {

                            LangSmithTokenUsageObservationHandler chatModelHandler(OpenTelemetryAiConventionsProperties properties) {
                                return new LangSmithTokenUsageObservationHandler();
                            }

                            LangSmithTokenUsageObservationHandler embeddingModelHandler() {
                                return new LangSmithTokenUsageObservationHandler();
                            }

                            LangSmithAdvisorObservationConvention advisorConvention() {
                                return new LangSmithAdvisorObservationConvention();
                            }

                        }
                        """)
        );
    }

    @Test
    void openLitAndOpenLLMetryConventionsReplacedWithOpenTelemetryOnes() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import io.arconia.observation.opentelemetry.ai.autoconfigure.OpenTelemetryAiConventionsProperties;
                        import io.arconia.observation.opentelemetry.ai.instrumentation.openlit.OpenLitAdvisorObservationConvention;
                        import io.arconia.observation.opentelemetry.ai.instrumentation.openlit.OpenLitToolCallingObservationConvention;
                        import io.arconia.observation.opentelemetry.ai.instrumentation.openllmetry.OpenLLMetryEmbeddingModelObservationConvention;

                        class ObservationConfig {

                            OpenLitAdvisorObservationConvention advisorConvention() {
                                return new OpenLitAdvisorObservationConvention();
                            }

                            OpenLitToolCallingObservationConvention toolCallingConvention(OpenTelemetryAiConventionsProperties properties) {
                                return new OpenLitToolCallingObservationConvention(properties);
                            }

                            OpenLLMetryEmbeddingModelObservationConvention embeddingModelConvention() {
                                return new OpenLLMetryEmbeddingModelObservationConvention();
                            }

                        }
                        """,
                        """
                        package com.yourorg;

                        import io.arconia.observation.opentelemetry.ai.autoconfigure.OpenTelemetryAiConventionsProperties;
                        import io.arconia.observation.opentelemetry.ai.instrumentation.opentelemetry.OpenTelemetryAdvisorObservationConvention;
                        import io.arconia.observation.opentelemetry.ai.instrumentation.opentelemetry.OpenTelemetryEmbeddingModelObservationConvention;
                        import io.arconia.observation.opentelemetry.ai.instrumentation.opentelemetry.OpenTelemetryToolCallingObservationConvention;

                        class ObservationConfig {

                            OpenTelemetryAdvisorObservationConvention advisorConvention() {
                                return new OpenTelemetryAdvisorObservationConvention();
                            }

                            OpenTelemetryToolCallingObservationConvention toolCallingConvention(OpenTelemetryAiConventionsProperties properties) {
                                return new OpenTelemetryToolCallingObservationConvention(properties);
                            }

                            OpenTelemetryEmbeddingModelObservationConvention embeddingModelConvention() {
                                return new OpenTelemetryEmbeddingModelObservationConvention();
                            }

                        }
                        """)
        );
    }

    @Test
    void conventionsAccessorsAndConstantsMigrated() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import io.arconia.observation.opentelemetry.ai.autoconfigure.OpenTelemetryAiConventionsProperties;
                        import io.arconia.observation.opentelemetry.ai.instrumentation.shared.GenAiAttributes;
                        import io.arconia.observation.opentelemetry.instrumentation.http.OpenTelemetryHttpOptions;

                        class ConventionsSetup {

                            void configure(OpenTelemetryAiConventionsProperties ai, OpenTelemetryHttpOptions http) {
                                ai.setIncludeToolDefinitions(true);
                                ai.setIncludeToolCallContent(true);
                                boolean toolDefinitions = ai.isIncludeToolDefinitions();
                                boolean toolCallContent = ai.isIncludeToolCallContent();
                                http.setIncludeUrlQuery(true);
                                boolean urlQuery = http.isIncludeUrlQuery();
                            }

                            String eventName() {
                                return GenAiAttributes.GEN_AI_CLIENT_INFERENCE_OPERATION_DETAILS;
                            }

                            String metricName() {
                                return GenAiAttributes.GEN_AI_CLIENT_TOKEN_USAGE;
                            }

                        }
                        """,
                        """
                        package com.yourorg;

                        import io.arconia.observation.opentelemetry.ai.autoconfigure.OpenTelemetryAiConventionsProperties;
                        import io.arconia.observation.opentelemetry.instrumentation.http.OpenTelemetryHttpOptions;
                        import io.opentelemetry.semconv.incubating.GenAiIncubatingEvents;
                        import io.opentelemetry.semconv.incubating.GenAiIncubatingMetrics;

                        class ConventionsSetup {

                            void configure(OpenTelemetryAiConventionsProperties ai, OpenTelemetryHttpOptions http) {
                                ai.setCaptureToolDefinitions(true);
                                ai.setCaptureToolCallContent(true);
                                boolean toolDefinitions = ai.capturesToolDefinitions();
                                boolean toolCallContent = ai.capturesToolCallContent();
                                http.setCaptureUrlQuery(true);
                                boolean urlQuery = http.isCaptureUrlQuery();
                            }

                            String eventName() {
                                return GenAiIncubatingEvents.GEN_AI_CLIENT_INFERENCE_OPERATION_DETAILS;
                            }

                            String metricName() {
                                return GenAiIncubatingMetrics.GEN_AI_CLIENT_TOKEN_USAGE_NAME;
                            }

                        }
                        """)
        );
    }

}
