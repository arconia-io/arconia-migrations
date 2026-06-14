package io.arconia.rewrite.test.junit.junit6;

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

class UpgradeJUnit6_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.test.junit.UpgradeJUnit_6")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "junit-jupiter-api-5.13", "junit-jupiter-engine-5.13",
                        "junit-jupiter-params-5.13", "junit-platform-suite-api-1.13",
                        "junit-platform-testkit-1.13", "junit-platform-engine-1.13"));
    }

    @Test
    @DocumentExample
    void addsLauncherDependency() {
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
                            testImplementation "org.junit.jupiter:junit-jupiter"
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
                            testImplementation "org.junit.jupiter:junit-jupiter"

                            testRuntimeOnly "org.junit.platform:junit-platform-launcher"
                        }
                        """
                )
        );
    }

    @Test
    void removesDeprecatedPlatformDependencies() {
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
                            testImplementation "org.junit.jupiter:junit-jupiter"
                            testImplementation "org.junit.platform:junit-platform-jfr"
                            testImplementation "org.junit.platform:junit-platform-runner"
                            testImplementation "org.junit.platform:junit-platform-suite-commons"
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
                            testImplementation "org.junit.jupiter:junit-jupiter"

                            testRuntimeOnly "org.junit.platform:junit-platform-launcher"
                        }
                        """
                )
        );
    }

    @Test
    void removesDeprecatedJupiterMigrationSupportDependency() {
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
                            testImplementation "org.junit.jupiter:junit-jupiter"
                            testImplementation "org.junit.jupiter:junit-jupiter-migrationsupport"
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
                            testImplementation "org.junit.jupiter:junit-jupiter"

                            testRuntimeOnly "org.junit.platform:junit-platform-launcher"
                        }
                        """
                )
        );
    }

    @Test
    void migratesAlphanumericMethodOrdererToMethodName() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.junit.jupiter.api.MethodOrderer;
                        import org.junit.jupiter.api.TestMethodOrder;

                        @TestMethodOrder(MethodOrderer.Alphanumeric.class)
                        class DemoTests {
                        }
                        """,
                        """
                        import org.junit.jupiter.api.MethodOrderer;
                        import org.junit.jupiter.api.MethodOrderer.MethodName;
                        import org.junit.jupiter.api.TestMethodOrder;

                        @TestMethodOrder(MethodName.class)
                        class DemoTests {
                        }
                        """
                )
        );
    }

    @Test
    void migratesEngineConstantsToApiConstants() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.junit.jupiter.engine.Constants;

                        class DemoTests {
                            String key = Constants.EXTENSIONS_AUTODETECTION_INCLUDE_PROPERTY_NAME;
                        }
                        """,
                        """
                        import org.junit.jupiter.api.Constants;

                        class DemoTests {
                            String key = Constants.EXTENSIONS_AUTODETECTION_INCLUDE_PROPERTY_NAME;
                        }
                        """
                )
        );
    }

    @Test
    void removesUseTechnicalNamesAnnotation() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.junit.platform.suite.api.SelectClasses;
                        import org.junit.platform.suite.api.Suite;
                        import org.junit.platform.suite.api.UseTechnicalNames;

                        @Suite
                        @SelectClasses(SomeTests.class)
                        @UseTechnicalNames
                        class TestSuite {
                        }

                        class SomeTests {
                        }
                        """,
                        """
                        import org.junit.platform.suite.api.SelectClasses;
                        import org.junit.platform.suite.api.Suite;

                        @Suite
                        @SelectClasses(SomeTests.class)
                        class TestSuite {
                        }

                        class SomeTests {
                        }
                        """
                )
        );
    }

    @Test
    void removesCsvFileSourceLineSeparatorAttribute() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.junit.jupiter.params.ParameterizedTest;
                        import org.junit.jupiter.params.provider.CsvFileSource;

                        class DemoTests {
                            @ParameterizedTest
                            @CsvFileSource(resources = "/data.csv", lineSeparator = "\\n")
                            void test(String value) {
                            }
                        }
                        """,
                        """
                        import org.junit.jupiter.params.ParameterizedTest;
                        import org.junit.jupiter.params.provider.CsvFileSource;

                        class DemoTests {
                            @ParameterizedTest
                            @CsvFileSource(resources = "/data.csv")
                            void test(String value) {
                            }
                        }
                        """
                )
        );
    }

    @Test
    void deletesTempDirScopeProperty() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        junit.jupiter.tempdir.scope=PER_CONTEXT
                        junit.jupiter.execution.parallel.enabled=true
                        """,
                        """
                        junit.jupiter.execution.parallel.enabled=true
                        """,
                        spec -> spec.path("src/test/resources/junit-platform.properties")
                )
        );
    }

    @Test
    void deletesLocaleFormatProperty() {
        rewriteRun(
                //language=properties
                properties(
                        """
                        junit.jupiter.params.arguments.conversion.locale.format=true
                        junit.jupiter.execution.parallel.enabled=true
                        """,
                        """
                        junit.jupiter.execution.parallel.enabled=true
                        """,
                        spec -> spec.path("src/test/resources/junit-platform.properties")
                )
        );
    }

    @Test
    void renamesEngineTestKitExecutionsStartedToFinished() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.junit.platform.testkit.engine.Executions;

                        class DemoTests {
                            long count(Executions executions) {
                                return executions.started().count();
                            }
                        }
                        """,
                        """
                        import org.junit.platform.testkit.engine.Executions;

                        class DemoTests {
                            long count(Executions executions) {
                                return executions.finished().count();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void renamesExtensionContextStoreGetOrComputeIfAbsent() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.junit.jupiter.api.extension.ExtensionContext.Store;

                        class DemoExtension {
                            String fetch(Store store) {
                                return store.getOrComputeIfAbsent("key", k -> "value", String.class);
                            }
                        }
                        """,
                        """
                        import org.junit.jupiter.api.extension.ExtensionContext.Store;

                        class DemoExtension {
                            String fetch(Store store) {
                                return store.computeIfAbsent("key", k -> "value", String.class);
                            }
                        }
                        """
                )
        );
    }

    @Test
    void renamesNamespacedHierarchicalStoreGetOrComputeIfAbsent() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.junit.platform.engine.support.store.Namespace;
                        import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;

                        class DemoTests {
                            String fetch(NamespacedHierarchicalStore<Namespace> store, Namespace ns) {
                                return (String) store.getOrComputeIfAbsent(ns, "key", k -> "value");
                            }
                        }
                        """,
                        """
                        import org.junit.platform.engine.support.store.Namespace;
                        import org.junit.platform.engine.support.store.NamespacedHierarchicalStore;

                        class DemoTests {
                            String fetch(NamespacedHierarchicalStore<Namespace> store, Namespace ns) {
                                return (String) store.computeIfAbsent(ns, "key", k -> "value");
                            }
                        }
                        """
                )
        );
    }

}
