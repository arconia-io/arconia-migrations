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
                "arconia-dev-services-api-0.29",
                "arconia-dev-services-oracle-xe-0.29",
                "arconia-dev-services-pulsar-0.29"));
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

}
