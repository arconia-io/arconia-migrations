package io.arconia.rewrite.spring.cloud2026;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThat;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.properties.Assertions.properties;
import static org.openrewrite.yaml.Assertions.yaml;

class MigrateSpringCloudContractToStubbornContract_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.cloud2026.MigrateSpringCloudContractToStubbornContract")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-cloud-contract-stub-runner-4.3", "spring-cloud-contract-wiremock-4.3", "junit-4.13"));
    }

    @Test
    @DocumentExample
    void gradleDependenciesAndPlugin() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                //language=groovy
                buildGradle(
                        """
                        plugins {
                            id 'java-library'
                            id 'org.springframework.cloud.contract' version '5.0.3'
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            testImplementation "org.springframework.cloud:spring-cloud-starter-contract-stub-runner"
                            testImplementation "org.springframework.cloud:spring-cloud-contract-wiremock"
                        }
                        """,
                        spec -> spec.after(actual -> {
                            assertThat(actual)
                                    .as("Gradle plugin migrated to Stubborn Contract")
                                    .containsPattern("id 'sh\\.stubborn\\.contract' version '0\\.1\\.\\d+'")
                                    .as("stub runner starter migrated to Stubborn Contract")
                                    .containsPattern("sh\\.stubborn:stubborn-contract-starter-stub-runner:0\\.1\\.\\d+")
                                    .as("WireMock module migrated to the Stubborn Contract Spring variant")
                                    .containsPattern("sh\\.stubborn:stubborn-contract-wiremock-spring:0\\.1\\.\\d+")
                                    .doesNotContain("org.springframework.cloud");
                            return actual;
                        })
                )
        );
    }

    @Test
    void mavenBomAndPlugin() {
        rewriteRun(
                //language=xml
                pomXml(
                        """
                        <project>
                            <modelVersion>4.0.0</modelVersion>
                            <groupId>com.example</groupId>
                            <artifactId>demo</artifactId>
                            <version>0.0.1</version>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>org.springframework.cloud</groupId>
                                        <artifactId>spring-cloud-contract-dependencies</artifactId>
                                        <version>5.0.3</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                            <dependencies>
                                <dependency>
                                    <groupId>org.springframework.cloud</groupId>
                                    <artifactId>spring-cloud-starter-contract-verifier</artifactId>
                                    <scope>test</scope>
                                </dependency>
                            </dependencies>
                            <build>
                                <plugins>
                                    <plugin>
                                        <groupId>org.springframework.cloud</groupId>
                                        <artifactId>spring-cloud-contract-maven-plugin</artifactId>
                                        <version>5.0.3</version>
                                        <extensions>true</extensions>
                                    </plugin>
                                </plugins>
                            </build>
                        </project>
                        """,
                        spec -> spec.after(actual -> {
                            assertThat(actual)
                                    .as("Contract BOM migrated to the Stubborn Contract BOM")
                                    .containsPattern("<artifactId>stubborn-contract-dependencies</artifactId>\\s*<version>0\\.1\\.\\d+</version>")
                                    .as("verifier starter migrated to Stubborn Contract")
                                    .contains("<artifactId>stubborn-contract-starter-verifier</artifactId>")
                                    .as("Maven plugin migrated to Stubborn Contract and upgraded to the latest 0.1.x patch")
                                    .containsPattern("<artifactId>stubborn-contract-maven-plugin</artifactId>\\s*<version>0\\.1\\.\\d+</version>")
                                    .doesNotContain("<version>0.1.0</version>")
                                    .doesNotContain("org.springframework.cloud");
                            return actual;
                        })
                )
        );
    }

    @Test
    void propertyPrefixes() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        spring.cloud.contract.stubrunner.ids=com.example:producer:+:stubs:8080
                        spring.cloud.contract.stubrunner.stubs-mode=LOCAL
                        spring.cloud.contract.verifier.base-package-for-tests=com.example.contracts
                        """,
                        """
                        stubborn.contract.stubrunner.ids=com.example:producer:+:stubs:8080
                        stubborn.contract.stubrunner.stubs-mode=LOCAL
                        stubborn.contract.verifier.base-package-for-tests=com.example.contracts
                        """,
                        s -> s.path("src/test/resources/application.properties")),
                //language=yaml
                yaml(
                        """
                        spring:
                          cloud:
                            contract:
                              stubrunner:
                                ids: com.example:producer:+:stubs:8080
                                stubs-mode: LOCAL
                        """,
                        """
                        stubborn.contract.stubrunner:
                          ids: com.example:producer:+:stubs:8080
                          stubs-mode: LOCAL
                        """,
                        s -> s.path("src/test/resources/application.yml"))
        );
    }

    @Test
    void javaTypesAndStubRunnerRule() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.junit.Rule;
                        import org.springframework.cloud.contract.stubrunner.junit.StubRunnerRule;
                        import org.springframework.cloud.contract.wiremock.WireMockSpring;

                        class ConsumerTests {

                            @Rule
                            public StubRunnerRule stubRunner = new StubRunnerRule();

                            WireMockSpring wireMock = null;
                        }
                        """,
                        """
                        import org.junit.jupiter.api.extension.RegisterExtension;
                        import sh.stubborn.contract.wiremock.WireMockSpring;
                        import sh.stubborn.contract.stubrunner.junit.StubRunnerExtension;

                        class ConsumerTests {

                            @RegisterExtension
                            public StubRunnerExtension stubRunner = new StubRunnerExtension();

                            WireMockSpring wireMock = null;
                        }
                        """
                )
        );
    }

}
