package io.arconia.rewrite.spring.modulith2;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.mavenProject;

class MigrateSpringModulithIncompleteEventPublications_2_2_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.modulith2.MigrateSpringModulithIncompleteEventPublications_2_2")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-modulith-events-api-2.1"));
    }

    @Test
    @DocumentExample
    void resubmissionOptions() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.modulith.events.IncompleteEventPublications;
                        import org.springframework.modulith.events.ResubmissionOptions;

                        class Resubmitter {
                            private final IncompleteEventPublications publications;

                            Resubmitter(IncompleteEventPublications publications) {
                                this.publications = publications;
                            }

                            void resubmit() {
                                publications.resubmitIncompletePublications(ResubmissionOptions.defaults());
                            }
                        }
                        """,
                        """
                        import org.springframework.modulith.events.FailedEventPublications;
                        import org.springframework.modulith.events.ResubmissionOptions;

                        class Resubmitter {
                            private final FailedEventPublications publications;

                            Resubmitter(FailedEventPublications publications) {
                                this.publications = publications;
                            }

                            void resubmit() {
                                publications.resubmit(ResubmissionOptions.defaults());
                            }
                        }
                        """
                )
        );
    }

    @Test
    void overloadsWithoutEquivalentLeaveProjectUnchanged() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.modulith.events.IncompleteEventPublications;
                        import org.springframework.modulith.events.ResubmissionOptions;

                        class Resubmitter {
                            void resubmit(IncompleteEventPublications publications) {
                                publications.resubmitIncompletePublications(ResubmissionOptions.defaults());
                            }
                        }
                        """
                ),
                //language=java
                java(
                        """
                        import java.time.Duration;

                        import org.springframework.modulith.events.IncompleteEventPublications;

                        class Housekeeping {
                            void resubmit(IncompleteEventPublications publications) {
                                publications.resubmitIncompletePublications(publication -> true);
                                publications.resubmitIncompletePublicationsOlderThan(Duration.ofMinutes(5));
                            }
                        }
                        """
                )
        );
    }

    @Test
    void implementationLeavesProjectUnchanged() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.time.Duration;
                        import java.util.function.Predicate;

                        import org.springframework.modulith.events.EventPublication;
                        import org.springframework.modulith.events.IncompleteEventPublications;
                        import org.springframework.modulith.events.ResubmissionOptions;

                        class CustomIncompleteEventPublications implements IncompleteEventPublications {
                            @Override
                            public void resubmitIncompletePublications(Predicate<EventPublication> filter) {
                            }

                            @Override
                            public void resubmitIncompletePublicationsOlderThan(Duration duration) {
                            }

                            @Override
                            public void resubmitIncompletePublications(ResubmissionOptions options) {
                            }
                        }
                        """
                )
        );
    }

    @Test
    void eachProjectQualifiesOnItsOwn() {
        rewriteRun(
                mavenProject("migratable",
                        //language=java
                        java(
                                """
                                import org.springframework.modulith.events.IncompleteEventPublications;
                                import org.springframework.modulith.events.ResubmissionOptions;

                                class Resubmitter {
                                    void resubmit(IncompleteEventPublications publications) {
                                        publications.resubmitIncompletePublications(ResubmissionOptions.defaults());
                                    }
                                }
                                """,
                                """
                                import org.springframework.modulith.events.FailedEventPublications;
                                import org.springframework.modulith.events.ResubmissionOptions;

                                class Resubmitter {
                                    void resubmit(FailedEventPublications publications) {
                                        publications.resubmit(ResubmissionOptions.defaults());
                                    }
                                }
                                """
                        )
                ),
                mavenProject("filtered",
                        //language=java
                        java(
                                """
                                import org.springframework.modulith.events.IncompleteEventPublications;

                                class Housekeeping {
                                    void resubmit(IncompleteEventPublications publications) {
                                        publications.resubmitIncompletePublications(publication -> true);
                                    }
                                }
                                """
                        )
                )
        );
    }

}
