package io.arconia.rewrite.openapi.swagger;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.test.SourceSpecs.text;
import static org.openrewrite.xml.Assertions.xml;

class UpgradeSwaggerCore_3_Tests implements RewriteTest {

    //language=xml
    private static final String POM_SWAGGER_2 = """
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.example</groupId>
                <artifactId>demo</artifactId>
                <version>0.0.1</version>
                <dependencies>
                    <dependency>
                        <groupId>io.swagger.core.v3</groupId>
                        <artifactId>swagger-jaxrs2-jakarta</artifactId>
                        <version>2.2.55</version>
                    </dependency>
                </dependencies>
                <repositories>
                    <repository>
                        <id>central-snapshots</id>
                        <url>https://central.sonatype.com/repository/maven-snapshots/</url>
                        <releases>
                            <enabled>false</enabled>
                        </releases>
                        <snapshots>
                            <enabled>true</enabled>
                        </snapshots>
                    </repository>
                </repositories>
            </project>
            """;

    //language=xml
    private static final String POM_SWAGGER_3 = """
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.example</groupId>
                <artifactId>demo</artifactId>
                <version>0.0.1</version>
                <dependencies>
                    <dependency>
                        <groupId>io.swagger.core.v3</groupId>
                        <artifactId>swagger-rest</artifactId>
                        <version>3.0.0-SNAPSHOT</version>
                    </dependency>
                </dependencies>
                <repositories>
                    <repository>
                        <id>central-snapshots</id>
                        <url>https://central.sonatype.com/repository/maven-snapshots/</url>
                        <releases>
                            <enabled>false</enabled>
                        </releases>
                        <snapshots>
                            <enabled>true</enabled>
                        </snapshots>
                    </repository>
                </repositories>
            </project>
            """;

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.openapi.swagger.UpgradeSwaggerCore_3")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "swagger-jaxrs2-jakarta-2.2", "swagger-integration-jakarta-2.2", "swagger-core-jakarta-2.2",
                        "swagger-models-jakarta-2.2", "jackson-databind-2.20", "jackson-core-2.20",
                        "jakarta.ws.rs-api-3.1", "javax.ws.rs-api-2.1"));
    }

    @Test
    @DocumentExample
    void restIntegration() {
        rewriteRun(
                mavenProject("demo",
                        pomXml(POM_SWAGGER_2, POM_SWAGGER_3),
                        //language=java
                        java(
                                """
                                import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
                                import io.swagger.v3.oas.integration.OpenApiConfigurationException;
                                import io.swagger.v3.oas.integration.SwaggerConfiguration;
                                import io.swagger.v3.oas.integration.api.OpenApiContext;

                                class OpenApiSetup {
                                    OpenApiContext context() throws OpenApiConfigurationException {
                                        SwaggerConfiguration configuration = new SwaggerConfiguration()
                                                .scannerClass("io.swagger.v3.jaxrs2.integration.JaxrsAnnotationScanner");
                                        return new JaxrsOpenApiContextBuilder<>()
                                                .openApiConfiguration(configuration)
                                                .buildContext(true);
                                    }
                                }
                                """,
                                """
                                import io.swagger.v3.oas.integration.OpenApiConfigurationException;
                                import io.swagger.v3.oas.integration.SwaggerConfiguration;
                                import io.swagger.v3.oas.integration.api.OpenApiContext;
                                import io.swagger.v3.rest.integration.RestOpenApiContextBuilder;

                                class OpenApiSetup {
                                    OpenApiContext context() throws OpenApiConfigurationException {
                                        SwaggerConfiguration configuration = new SwaggerConfiguration()
                                                .scannerClass("io.swagger.v3.rest.integration.RestAnnotationScanner");
                                        return new RestOpenApiContextBuilder<>()
                                                .openApiConfiguration(configuration)
                                                .buildContext(true);
                                    }
                                }
                                """
                        ),
                        //language=xml
                        xml(
                                """
                                <web-app xmlns="https://jakarta.ee/xml/ns/jakartaee" version="6.0">
                                    <servlet>
                                        <servlet-name>openapi</servlet-name>
                                        <servlet-class>io.swagger.v3.jaxrs2.integration.OpenApiServlet</servlet-class>
                                        <init-param>
                                            <param-name>openApi.configuration.scannerClass</param-name>
                                            <param-value>io.swagger.v3.jaxrs2.integration.JaxrsApplicationAndAnnotationScanner</param-value>
                                        </init-param>
                                    </servlet>
                                </web-app>
                                """,
                                """
                                <web-app xmlns="https://jakarta.ee/xml/ns/jakartaee" version="6.0">
                                    <servlet>
                                        <servlet-name>openapi</servlet-name>
                                        <servlet-class>io.swagger.v3.rest.integration.OpenApiServlet</servlet-class>
                                        <init-param>
                                            <param-name>openApi.configuration.scannerClass</param-name>
                                            <param-value>io.swagger.v3.rest.integration.RestApplicationAndAnnotationScanner</param-value>
                                        </init-param>
                                    </servlet>
                                </web-app>
                                """,
                                spec -> spec.path("src/main/webapp/WEB-INF/web.xml")
                        )
                )
        );
    }

    @Test
    void openApiExtension() {
        rewriteRun(
                mavenProject("demo",
                        pomXml(POM_SWAGGER_2, POM_SWAGGER_3),
                        //language=java
                        java(
                                """
                                package com.example;

                                import io.swagger.v3.jaxrs2.ext.AbstractOpenAPIExtension;

                                public class TenantExtension extends AbstractOpenAPIExtension {
                                }
                                """,
                                """
                                package com.example;

                                import io.swagger.v3.rest.ext.AbstractOpenAPIExtension;

                                public class TenantExtension extends AbstractOpenAPIExtension {
                                }
                                """
                        ),
                        text(
                                "com.example.TenantExtension",
                                spec -> spec.path("src/main/resources/META-INF/services/io.swagger.v3.jaxrs2.ext.OpenAPIExtension")
                                        .afterRecipe(file -> assertThat(file.getSourcePath())
                                                .hasToString("demo/src/main/resources/META-INF/services/io.swagger.v3.rest.ext.OpenAPIExtension"))
                        ),
                        text(
                                """
                                module com.example {
                                    requires io.swagger.v3.jaxrs2;
                                    requires io.swagger.v3.jaxrs2.integration.servlet;
                                    provides io.swagger.v3.jaxrs2.ext.OpenAPIExtension with com.example.TenantExtension;
                                }
                                """,
                                """
                                module com.example {
                                    requires io.swagger.v3.rest;
                                    requires io.swagger.v3.rest.integration.servlet;
                                    provides io.swagger.v3.rest.ext.OpenAPIExtension with com.example.TenantExtension;
                                }
                                """,
                                spec -> spec.path("src/main/java/module-info.java")
                        )
                )
        );
    }

    @Test
    void objectMapperProcessorMovesToJackson3() {
        rewriteRun(
                mavenProject("demo",
                        pomXml(POM_SWAGGER_2, POM_SWAGGER_3),
                        //language=java
                        java(
                                """
                                import com.fasterxml.jackson.databind.ObjectMapper;
                                import io.swagger.v3.oas.integration.api.ObjectMapperProcessor;

                                class ModulesProcessor implements ObjectMapperProcessor {
                                    @Override
                                    public void processJsonObjectMapper(ObjectMapper mapper) {
                                    }
                                }
                                """,
                                """
                                import tools.jackson.databind.ObjectMapper;
                                import io.swagger.v3.oas.integration.api.ObjectMapperProcessor;

                                class ModulesProcessor implements ObjectMapperProcessor {
                                    @Override
                                    public ObjectMapper processJsonObjectMapper(ObjectMapper mapper) {
                                        return mapper;
                                    }
                                }
                                """
                        )
                )
        );
    }

    @Test
    void dependenciesAndPlugins() {
        rewriteRun(
                mavenProject("demo",
                        //language=xml
                        pomXml(
                                """
                                <project>
                                    <modelVersion>4.0.0</modelVersion>
                                    <groupId>com.example</groupId>
                                    <artifactId>demo</artifactId>
                                    <version>0.0.1</version>
                                    <dependencies>
                                            <dependency>
                                                <groupId>io.swagger.core.v3</groupId>
                                                <artifactId>swagger-annotations-jakarta</artifactId>
                                                <version>2.2.55</version>
                                            </dependency>
                                            <dependency>
                                                <groupId>io.swagger.core.v3</groupId>
                                                <artifactId>swagger-jaxrs2-servlet-initializer-v2-jakarta</artifactId>
                                                <version>2.2.55</version>
                                            </dependency>
                                            <dependency>
                                                <groupId>jakarta.ws.rs</groupId>
                                                <artifactId>jakarta.ws.rs-api</artifactId>
                                                <version>3.1.0</version>
                                            </dependency>
                                            <dependency>
                                                <groupId>jakarta.servlet</groupId>
                                                <artifactId>jakarta.servlet-api</artifactId>
                                                <version>6.0.0</version>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.glassfish.jersey.media</groupId>
                                                <artifactId>jersey-media-json-jackson</artifactId>
                                                <version>3.1.11</version>
                                            </dependency>
                                    </dependencies>
                                    <build>
                                        <plugins>
                                            <plugin>
                                                <groupId>io.swagger.core.v3</groupId>
                                                <artifactId>swagger-maven-plugin-jakarta</artifactId>
                                                <version>2.2.55</version>
                                            </plugin>
                                        </plugins>
                                    </build>
                                    <repositories>
                                        <repository>
                                            <id>central-snapshots</id>
                                            <url>https://central.sonatype.com/repository/maven-snapshots/</url>
                                            <releases>
                                                <enabled>false</enabled>
                                            </releases>
                                            <snapshots>
                                                <enabled>true</enabled>
                                            </snapshots>
                                        </repository>
                                    </repositories>
                                    <pluginRepositories>
                                        <pluginRepository>
                                            <id>central-snapshots</id>
                                            <url>https://central.sonatype.com/repository/maven-snapshots/</url>
                                            <releases>
                                                <enabled>false</enabled>
                                            </releases>
                                            <snapshots>
                                                <enabled>true</enabled>
                                            </snapshots>
                                        </pluginRepository>
                                    </pluginRepositories>
                                </project>
                                """,
                                """
                                <project>
                                    <modelVersion>4.0.0</modelVersion>
                                    <groupId>com.example</groupId>
                                    <artifactId>demo</artifactId>
                                    <version>0.0.1</version>
                                    <dependencies>
                                            <dependency>
                                                <groupId>io.swagger.core.v3</groupId>
                                                <artifactId>swagger-annotations</artifactId>
                                                <version>3.0.0-SNAPSHOT</version>
                                            </dependency>
                                            <dependency>
                                                <groupId>io.swagger.core.v3</groupId>
                                                <artifactId>swagger-rest-servlet-initializer</artifactId>
                                                <version>3.0.0-SNAPSHOT</version>
                                            </dependency>
                                            <dependency>
                                                <groupId>jakarta.ws.rs</groupId>
                                                <artifactId>jakarta.ws.rs-api</artifactId>
                                                <version>4.0.0</version>
                                            </dependency>
                                            <dependency>
                                                <groupId>jakarta.servlet</groupId>
                                                <artifactId>jakarta.servlet-api</artifactId>
                                                <version>6.1.0</version>
                                            </dependency>
                                            <dependency>
                                                <groupId>org.glassfish.jersey.media</groupId>
                                                <artifactId>jersey-media-json-jackson3</artifactId>
                                                <version>4.0.3</version>
                                            </dependency>
                                    </dependencies>
                                    <build>
                                        <plugins>
                                            <plugin>
                                                <groupId>io.swagger.core.v3</groupId>
                                                <artifactId>swagger-maven-plugin</artifactId>
                                                <version>3.0.0-SNAPSHOT</version>
                                            </plugin>
                                        </plugins>
                                    </build>
                                    <repositories>
                                        <repository>
                                            <id>central-snapshots</id>
                                            <url>https://central.sonatype.com/repository/maven-snapshots/</url>
                                            <releases>
                                                <enabled>false</enabled>
                                            </releases>
                                            <snapshots>
                                                <enabled>true</enabled>
                                            </snapshots>
                                        </repository>
                                    </repositories>
                                    <pluginRepositories>
                                        <pluginRepository>
                                            <id>central-snapshots</id>
                                            <url>https://central.sonatype.com/repository/maven-snapshots/</url>
                                            <releases>
                                                <enabled>false</enabled>
                                            </releases>
                                            <snapshots>
                                                <enabled>true</enabled>
                                            </snapshots>
                                        </pluginRepository>
                                    </pluginRepositories>
                                </project>
                                """
                        )
                )
        );
    }

    @Test
    void javaxCodeLeavesProjectUnchanged() {
        rewriteRun(
                mavenProject("demo",
                        pomXml(POM_SWAGGER_2),
                        //language=java
                        java(
                                """
                                import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
                                import javax.ws.rs.ApplicationPath;
                                import javax.ws.rs.core.Application;

                                @ApplicationPath("/api")
                                class RestApplication extends Application {
                                    JaxrsOpenApiContextBuilder<?> openApi = new JaxrsOpenApiContextBuilder<>();
                                }
                                """
                        )
                )
        );
    }

    @Test
    void javaxSwaggerArtifactLeavesProjectUnchanged() {
        rewriteRun(
                mavenProject("demo",
                        //language=xml
                        pomXml(
                                """
                                <project>
                                    <modelVersion>4.0.0</modelVersion>
                                    <groupId>com.example</groupId>
                                    <artifactId>demo</artifactId>
                                    <version>0.0.1</version>
                                    <dependencies>
                                        <dependency>
                                            <groupId>io.swagger.core.v3</groupId>
                                            <artifactId>swagger-jaxrs2</artifactId>
                                            <version>2.2.55</version>
                                        </dependency>
                                    </dependencies>
                                </project>
                                """
                        )
                )
        );
    }

    @Test
    void eachModuleCheckedOnItsOwn() {
        rewriteRun(
                mavenProject("jakarta-api",
                        pomXml(POM_SWAGGER_2.replace("<artifactId>demo</artifactId>", "<artifactId>jakarta-api</artifactId>"),
                                POM_SWAGGER_3.replace("<artifactId>demo</artifactId>", "<artifactId>jakarta-api</artifactId>")),
                        //language=java
                        java(
                                """
                                import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
                                import jakarta.ws.rs.core.Application;

                                class RestApplication extends Application {
                                    JaxrsOpenApiContextBuilder<?> openApi = new JaxrsOpenApiContextBuilder<>();
                                }
                                """,
                                """
                                import io.swagger.v3.rest.integration.RestOpenApiContextBuilder;
                                import jakarta.ws.rs.core.Application;

                                class RestApplication extends Application {
                                    RestOpenApiContextBuilder<?> openApi = new RestOpenApiContextBuilder<>();
                                }
                                """
                        )
                ),
                mavenProject("javax-api",
                        pomXml(POM_SWAGGER_2.replace("<artifactId>demo</artifactId>", "<artifactId>javax-api</artifactId>")),
                        //language=java
                        java(
                                """
                                import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
                                import javax.ws.rs.core.Application;

                                class LegacyApplication extends Application {
                                    JaxrsOpenApiContextBuilder<?> openApi = new JaxrsOpenApiContextBuilder<>();
                                }
                                """
                        )
                )
        );
    }

}
