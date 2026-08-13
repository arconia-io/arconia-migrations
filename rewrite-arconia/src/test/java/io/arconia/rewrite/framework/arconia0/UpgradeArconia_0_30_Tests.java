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

class UpgradeArconia_0_30_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.framework.UpgradeArconia_0_30")
            .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                "arconia-core-0.29",
                "arconia-dev-services-api-0.29",
                "arconia-dev-services-oracle-xe-0.29",
                "arconia-dev-services-pulsar-0.29",
                "arconia-multitenancy-core-0.29",
                "arconia-multitenancy-web-0.29"));
    }

    @Test
    @DocumentExample
    void pulsarDevServicesPropertyChanges() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.dev.services.pulsar.management-console-port=8080
                        """,
                        """
                        arconia.dev.services.pulsar.admin-port=8080
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void pulsarDevServicesPropertyChangesInYaml() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          dev:
                            services:
                              pulsar:
                                management-console-port: 8080
                        """,
                        """
                        arconia:
                          dev:
                            services:
                              pulsar:
                                admin-port: 8080
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void tenantDetailsSourcePropertyCommented() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        arconia.multitenancy.details.source=properties
                        arconia.multitenancy.details.tenants[0].identifier=acme
                        """,
                        """
                        # Removed. Tenant details are auto-configured from the arconia.multitenancy.details.tenants list or from a tenant details dependency.
                        # arconia.multitenancy.details.source=properties
                        arconia.multitenancy.details.tenants[0].identifier=acme
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void tenantDetailsSourcePropertyCommentedInYaml() {
        rewriteRun(
                //language=yaml
                yaml(
                        """
                        arconia:
                          multitenancy:
                            details:
                              source: properties
                              tenants:
                                - identifier: acme
                        """,
                        """
                        arconia:
                          multitenancy:
                            details:
                              # Removed. Tenant details are auto-configured from the arconia.multitenancy.details.tenants list or from a tenant details dependency.
                              # source: properties
                              tenants:
                                - identifier: acme
                        """,
                        s -> s.path("src/main/resources/application.yml"))
        );
    }

    @Test
    void pulsarDevServicesMethodChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.dev.services.pulsar.PulsarDevServicesProperties;

                        class Demo {
                            void call(PulsarDevServicesProperties properties) {
                                properties.getManagementConsolePort();
                                properties.setManagementConsolePort(8080);
                            }
                        }
                        """,
                        """
                        import io.arconia.dev.services.pulsar.PulsarDevServicesProperties;

                        class Demo {
                            void call(PulsarDevServicesProperties properties) {
                                properties.getAdminPort();
                                properties.setAdminPort(8080);
                            }
                        }
                        """
                )
        );
    }

    @Test
    void oracleXeDevServicesTypeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.dev.services.oracle.OracleXeDevServicesAutoConfiguration;
                        import io.arconia.dev.services.oracle.OracleXeDevServicesProperties;

                        class Demo {
                            void call(OracleXeDevServicesProperties properties) {
                                Class<?> autoConfiguration = OracleXeDevServicesAutoConfiguration.class;
                            }
                        }
                        """,
                        """
                        import io.arconia.dev.services.oracle.xe.OracleXeDevServicesAutoConfiguration;
                        import io.arconia.dev.services.oracle.xe.OracleXeDevServicesProperties;

                        class Demo {
                            void call(OracleXeDevServicesProperties properties) {
                                Class<?> autoConfiguration = OracleXeDevServicesAutoConfiguration.class;
                            }
                        }
                        """
                )
        );
    }

    @Test
    void multitenancyCoreBuilderChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.multitenancy.core.context.resolvers.FixedTenantResolver;
                        import io.arconia.multitenancy.core.observability.Cardinality;
                        import io.arconia.multitenancy.core.observability.MdcTenantEventListener;
                        import io.arconia.multitenancy.core.observability.TenantObservationFilter;
                        import io.arconia.multitenancy.core.tenantdetails.Tenant;

                        class Demo {
                            void call(String tenantIdentifierKey, Cardinality cardinality) {
                                FixedTenantResolver resolver = new FixedTenantResolver("acme");
                                MdcTenantEventListener listener = new MdcTenantEventListener();
                                TenantObservationFilter filter = new TenantObservationFilter(tenantIdentifierKey, cardinality);
                                Tenant tenant = new Tenant.Builder().identifier("acme").build();
                            }
                        }
                        """,
                        """
                        import io.arconia.multitenancy.core.context.resolvers.FixedTenantResolver;
                        import io.arconia.multitenancy.core.observability.Cardinality;
                        import io.arconia.multitenancy.core.observability.MdcTenantEventListener;
                        import io.arconia.multitenancy.core.observability.TenantObservationFilter;
                        import io.arconia.multitenancy.core.tenantdetails.Tenant;

                        class Demo {
                            void call(String tenantIdentifierKey, Cardinality cardinality) {
                                FixedTenantResolver resolver = FixedTenantResolver.builder().tenantIdentifier("acme").build();
                                MdcTenantEventListener listener = MdcTenantEventListener.builder().build();
                                TenantObservationFilter filter = TenantObservationFilter.builder().tenantIdentifierKey(tenantIdentifierKey).cardinality(cardinality).build();
                                Tenant tenant = Tenant.builder().identifier("acme").build();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void multitenancyWebBuilderChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.multitenancy.web.context.resolvers.CookieTenantResolver;
                        import io.arconia.multitenancy.web.context.resolvers.HeaderTenantResolver;

                        class Demo {
                            void call(String name) {
                                HeaderTenantResolver headerResolver = new HeaderTenantResolver(name);
                                CookieTenantResolver cookieResolver = new CookieTenantResolver();
                            }
                        }
                        """,
                        """
                        import io.arconia.multitenancy.web.context.resolvers.CookieTenantResolver;
                        import io.arconia.multitenancy.web.context.resolvers.HeaderTenantResolver;

                        class Demo {
                            void call(String name) {
                                HeaderTenantResolver headerResolver = HeaderTenantResolver.builder().tenantHeaderName(name).build();
                                CookieTenantResolver cookieResolver = CookieTenantResolver.builder().build();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void incubatingSinceAttributeRemoved() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.core.support.Incubating;

                        @Incubating(since = "0.20.0")
                        class Demo {
                            @Incubating(since = "0.20.0")
                            void call() {}
                        }
                        """,
                        """
                        import io.arconia.core.support.Incubating;

                        @Incubating
                        class Demo {
                            @Incubating
                            void call() {}
                        }
                        """
                )
        );
    }

}
