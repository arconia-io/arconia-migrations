package io.arconia.rewrite.spring.cloud.cloud2025;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.properties.Assertions.properties;

class UpgradeSpringCloud_2025_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.cloud2025.UpgradeSpringCloud_2025_0");
    }

    @Test
    @DocumentExample
    void gatewayDependencyChanges() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                //language=groovy
                buildGradle(
                        """
                        plugins {
                            id "java-library"
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation "org.springframework.cloud:spring-cloud-gateway-server"
                            implementation "org.springframework.cloud:spring-cloud-gateway-server-mvc"
                            implementation "org.springframework.cloud:spring-cloud-starter-gateway-server"
                            implementation "org.springframework.cloud:spring-cloud-starter-gateway-server-mvc"
                            implementation "org.springframework.cloud:spring-cloud-gateway-mvc"
                            implementation "org.springframework.cloud:spring-cloud-gateway-webflux"
                        }
                        """,
                        """
                        plugins {
                            id "java-library"
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation "org.springframework.cloud:spring-cloud-gateway-server-webflux"
                            implementation "org.springframework.cloud:spring-cloud-gateway-server-webmvc"
                            implementation "org.springframework.cloud:spring-cloud-starter-gateway-server-webflux"
                            implementation "org.springframework.cloud:spring-cloud-starter-gateway-server-webmvc"
                            implementation "org.springframework.cloud:spring-cloud-gateway-proxyexchange-webmvc"
                            implementation "org.springframework.cloud:spring-cloud-gateway-proxyexchange-webflux"
                        }
                        """
                )
        );
    }

    @Test
    void gatewayEndpointAccessEnabled() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        management.endpoint.gateway.enabled=true
                        """,
                        """
                        management.endpoint.gateway.access=read_only
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void gatewayEndpointAccessDisabled() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        management.endpoint.gateway.enabled=false
                        """,
                        """
                        management.endpoint.gateway.access=none
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

    @Test
    void gatewayServerPropertyRename() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.cloud.gateway.default-filters=AddRequestHeader=X-Request-Foo, Bar
                        """,
                        """
                        spring.cloud.gateway.server.webflux.default-filters=AddRequestHeader=X-Request-Foo, Bar
                        """,
                        s -> s.path("src/main/resources/application.properties"))
        );
    }

}
