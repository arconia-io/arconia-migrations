package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesWebServices_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateWebServicesModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.webservices.WebServicesAutoConfiguration;
                        import org.springframework.boot.autoconfigure.webservices.WebServicesProperties;
                        import org.springframework.boot.autoconfigure.webservices.client.WebServiceTemplateAutoConfiguration;

                        class Demo {
                            WebServicesAutoConfiguration autoConfiguration = null;
                            WebServicesProperties properties = null;
                            WebServiceTemplateAutoConfiguration templateAutoConfiguration = null;
                        }
                        """,
                        """
                        import org.springframework.boot.webservices.autoconfigure.WebServicesAutoConfiguration;
                        import org.springframework.boot.webservices.autoconfigure.WebServicesProperties;
                        import org.springframework.boot.webservices.autoconfigure.client.WebServiceTemplateAutoConfiguration;

                        class Demo {
                            WebServicesAutoConfiguration autoConfiguration = null;
                            WebServicesProperties properties = null;
                            WebServiceTemplateAutoConfiguration templateAutoConfiguration = null;
                        }
                        """
                )
        );
    }

    @Test
    void dependencies() {
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
                            implementation "org.springframework.boot:spring-boot-starter-web-services"
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
                            implementation "org.springframework.boot:spring-boot-starter-webservices"

                            testImplementation "org.springframework.boot:spring-boot-starter-webservices-test"
                        }
                        """
                )
        );
    }

}
