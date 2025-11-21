package io.arconia.rewrite.spring.boot.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesBatch_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateBatchModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5.*", "spring-boot-actuator-autoconfigure-3.5.*"));
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
                              testImplementation "org.springframework.batch:spring-batch-test"
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
                              testImplementation "org.springframework.boot:spring-boot-starter-batch-test"
                          }
                          """
                )
        );
    }

    @Test
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
                        import org.springframework.boot.autoconfigure.batch.BatchConversionServiceCustomizer;
                        import org.springframework.boot.autoconfigure.batch.BatchProperties;
                        import org.springframework.boot.autoconfigure.batch.BatchTaskExecutor;
                        import org.springframework.boot.autoconfigure.batch.BatchTransactionManager;
                        import org.springframework.boot.autoconfigure.batch.JobExecutionEvent;
                        import org.springframework.boot.autoconfigure.batch.JobExecutionExitCodeGenerator;
                        import org.springframework.boot.autoconfigure.batch.JobLauncherApplicationRunner;
                        import org.springframework.boot.actuate.autoconfigure.observation.batch.BatchObservationAutoConfiguration;

                        class Demo {
                            BatchAutoConfiguration autoConfiguration = null;
                            BatchConversionServiceCustomizer conversionServiceCustomizer = null;
                            BatchProperties properties = null;
                            BatchTaskExecutor taskExecutor = null;
                            BatchTransactionManager transactionManager = null;
                            JobExecutionEvent jobExecutionEvent = null;
                            JobExecutionExitCodeGenerator jobExecutionExitCodeGenerator = null;
                            JobLauncherApplicationRunner jobLauncherApplicationRunner = null;
                            BatchObservationAutoConfiguration batchObservationAutoConfiguration = null;
                        }
                        """,
                        """
                        import org.springframework.boot.batch.autoconfigure.*;
                        import org.springframework.boot.batch.autoconfigure.observation.BatchObservationAutoConfiguration;

                        class Demo {
                            BatchAutoConfiguration autoConfiguration = null;
                            BatchConversionServiceCustomizer conversionServiceCustomizer = null;
                            BatchProperties properties = null;
                            BatchTaskExecutor taskExecutor = null;
                            BatchTransactionManager transactionManager = null;
                            JobExecutionEvent jobExecutionEvent = null;
                            JobExecutionExitCodeGenerator jobExecutionExitCodeGenerator = null;
                            JobLauncherApplicationRunner jobLauncherApplicationRunner = null;
                            BatchObservationAutoConfiguration batchObservationAutoConfiguration = null;
                        }
                        """
                )
        );
    }

}
