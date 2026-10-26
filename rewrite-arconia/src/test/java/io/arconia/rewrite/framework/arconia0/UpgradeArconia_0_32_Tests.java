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

class UpgradeArconia_0_32_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.framework.UpgradeArconia_0_32")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "arconia-multitenancy-core-0.30", "arconia-multitenancy-web-0.30", "jakarta.servlet-api-6.1",
                        "spring-context-7.0", "arconia-dev-services-api-0.31"));
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

}
