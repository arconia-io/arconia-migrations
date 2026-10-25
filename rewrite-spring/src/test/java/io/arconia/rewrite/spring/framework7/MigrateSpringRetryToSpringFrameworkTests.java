package io.arconia.rewrite.spring.framework7;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.maven.Assertions.pomXml;

class MigrateSpringRetryToSpringFrameworkTests implements RewriteTest {

    //language=xml
    private static final String POM_WITH_SPRING_RETRY = """
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.example</groupId>
                <artifactId>demo</artifactId>
                <version>0.0.1</version>
                <dependencies>
                    <dependency>
                        <groupId>org.springframework.retry</groupId>
                        <artifactId>spring-retry</artifactId>
                        <version>2.0.13</version>
                    </dependency>
                </dependencies>
            </project>
            """;

    //language=xml
    private static final String POM_WITHOUT_SPRING_RETRY = """
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.example</groupId>
                <artifactId>demo</artifactId>
                <version>0.0.1</version>
            </project>
            """;

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.framework7.MigrateSpringRetryToSpringFramework")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-retry-2.0"));
    }

    @Test
    @DocumentExample
    void annotationBasedRetry() {
        rewriteRun(
                mavenProject("demo",
                        pomXml(POM_WITH_SPRING_RETRY, POM_WITHOUT_SPRING_RETRY),
                        //language=java
                        java(
                                """
                                import org.springframework.retry.annotation.EnableRetry;

                                @EnableRetry
                                class RetryConfiguration {
                                }
                                """,
                                """
                                import org.springframework.resilience.annotation.EnableResilientMethods;

                                @EnableResilientMethods
                                class RetryConfiguration {
                                }
                                """
                        ),
                        //language=java
                        java(
                                """
                                import java.io.IOException;

                                import org.springframework.retry.annotation.Backoff;
                                import org.springframework.retry.annotation.Retryable;

                                class NotificationService {
                                    @Retryable(retryFor = IOException.class, maxAttempts = 5, backoff = @Backoff(delay = 100, multiplier = 2))
                                    void send() throws IOException {
                                    }
                                }
                                """,
                                """
                                import org.springframework.resilience.annotation.Retryable;

                                import java.io.IOException;

                                class NotificationService {
                                    @Retryable(includes = IOException.class, maxRetries = 4, delay = 100, multiplier = 2, maxDelay = 30000)
                                    void send() throws IOException {
                                    }
                                }
                                """
                        )
                )
        );
    }

    @Test
    void retryAndBackoffSettings() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.io.IOException;
                        import java.io.UncheckedIOException;

                        import org.springframework.retry.annotation.Backoff;
                        import org.springframework.retry.annotation.Retryable;

                        class Demo {
                            static final int MAX_ATTEMPTS = 4;

                            @Retryable
                            void defaults() {
                            }

                            @Retryable(IOException.class)
                            void includes() throws IOException {
                            }

                            @Retryable(noRetryFor = {IllegalArgumentException.class, UncheckedIOException.class}, label = "excludes")
                            void excludes() {
                            }

                            @Retryable(maxAttempts = MAX_ATTEMPTS, backoff = @Backoff(500))
                            void fixedBackoff() {
                            }

                            @Retryable(maxAttempts = 1, backoff = @Backoff(delay = 200, maxDelay = 5000, multiplier = 1.5))
                            void exponentialBackoff() {
                            }
                        }
                        """,
                        """
                        import org.springframework.resilience.annotation.Retryable;

                        import java.io.IOException;
                        import java.io.UncheckedIOException;

                        class Demo {
                            static final int MAX_ATTEMPTS = 4;

                            @Retryable(maxRetries = 2)
                            void defaults() {
                            }

                            @Retryable(includes = IOException.class, maxRetries = 2)
                            void includes() throws IOException {
                            }

                            @Retryable(excludes = {IllegalArgumentException.class, UncheckedIOException.class}, maxRetries = 2)
                            void excludes() {
                            }

                            @Retryable(maxRetries = MAX_ATTEMPTS - 1, delay = 500)
                            void fixedBackoff() {
                            }

                            @Retryable(maxRetries = 0, delay = 200, multiplier = 1.5, maxDelay = 5000)
                            void exponentialBackoff() {
                            }
                        }
                        """
                )
        );
    }

    @Test
    void recoveryLeavesProjectUnchanged() {
        rewriteRun(
                mavenProject("demo",
                        pomXml(POM_WITH_SPRING_RETRY),
                        //language=java
                        java(
                                """
                                import org.springframework.retry.annotation.Recover;
                                import org.springframework.retry.annotation.Retryable;

                                class Demo {
                                    @Retryable
                                    String call() {
                                        return "result";
                                    }

                                    @Recover
                                    String recover(RuntimeException ex) {
                                        return "fallback";
                                    }
                                }
                                """
                        ),
                        //language=java
                        java(
                                """
                                import org.springframework.retry.annotation.Retryable;

                                class Other {
                                    @Retryable
                                    void call() {
                                    }
                                }
                                """
                        )
                )
        );
    }

    @Test
    void retryApiLeavesProjectUnchanged() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.retry.RetryContext;
                        import org.springframework.retry.annotation.Retryable;
                        import org.springframework.retry.support.RetrySynchronizationManager;

                        class Demo {
                            @Retryable
                            void call() {
                                RetryContext context = RetrySynchronizationManager.getContext();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void backoffWithoutCounterpartLeavesProjectUnchanged() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.retry.annotation.Backoff;
                        import org.springframework.retry.annotation.Retryable;

                        class Demo {
                            @Retryable(backoff = @Backoff(delay = 100, maxDelay = 500))
                            void uniformRandom() {
                            }

                            @Retryable(backoff = @Backoff(delay = 100, multiplier = 2, random = true))
                            void exponentialRandom() {
                            }
                        }
                        """
                )
        );
    }

    @Test
    void reactiveMethodLeavesProjectUnchanged() {
        rewriteRun(
                spec -> spec.parser(JavaParser.fromJavaVersion()
                        .classpathFromResources(new InMemoryExecutionContext(), "spring-retry-2.0")
                        .dependsOn("package org.reactivestreams; public interface Publisher<T> {}")),
                //language=java
                java(
                        """
                        import org.reactivestreams.Publisher;
                        import org.springframework.retry.annotation.Retryable;

                        class Demo {
                            @Retryable
                            Publisher<String> call() {
                                return null;
                            }
                        }
                        """
                )
        );
    }

    @Test
    void springRetryKeptWithSpringCloud() {
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
                                            <groupId>org.springframework.cloud</groupId>
                                            <artifactId>spring-cloud-starter-config</artifactId>
                                            <version>5.0.6</version>
                                        </dependency>
                                        <dependency>
                                            <groupId>org.springframework.retry</groupId>
                                            <artifactId>spring-retry</artifactId>
                                            <version>2.0.13</version>
                                        </dependency>
                                    </dependencies>
                                </project>
                                """
                        ),
                        //language=java
                        java(
                                """
                                import org.springframework.retry.annotation.Retryable;

                                class Demo {
                                    @Retryable
                                    void call() {
                                    }
                                }
                                """,
                                """
                                import org.springframework.resilience.annotation.Retryable;

                                class Demo {
                                    @Retryable(maxRetries = 2)
                                    void call() {
                                    }
                                }
                                """
                        )
                )
        );
    }

    @Test
    void unusedSpringRetryRemoved() {
        rewriteRun(
                mavenProject("demo",
                        pomXml(POM_WITH_SPRING_RETRY, POM_WITHOUT_SPRING_RETRY),
                        //language=java
                        java(
                                """
                                class Demo {
                                }
                                """
                        )
                )
        );
    }

}
