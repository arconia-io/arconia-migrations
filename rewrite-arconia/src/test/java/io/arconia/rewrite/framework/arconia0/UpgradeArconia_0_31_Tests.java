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
                                discovery.shared(true);
                            }
                        }
                        """,
                        """
                        import io.arconia.dev.services.core.registration.ContainerSpec;
                        import io.arconia.dev.services.core.registration.DiscoverySpec;

                        class Demo {
                            void call(ContainerSpec container, DiscoverySpec discovery) {
                                discovery.shared(true);
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

}
