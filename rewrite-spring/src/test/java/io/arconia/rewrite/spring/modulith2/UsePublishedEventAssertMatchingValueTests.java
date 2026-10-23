package io.arconia.rewrite.spring.modulith2;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UsePublishedEventAssertMatchingValueTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UsePublishedEventAssertMatchingValue())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-modulith-test-2.0", "assertj-core"));
    }

    @Test
    @DocumentExample
    void matchingValue() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.modulith.test.PublishedEventsAssert.PublishedEventAssert;

                        class Demo {
                            void verify(PublishedEventAssert<OrderCompleted> event) {
                                event.matching(OrderCompleted::orderId, "42");
                            }

                            record OrderCompleted(String orderId) {
                            }
                        }
                        """,
                        """
                        import org.springframework.modulith.test.PublishedEventsAssert.PublishedEventAssert;

                        class Demo {
                            void verify(PublishedEventAssert<OrderCompleted> event) {
                                event.matchingValue(OrderCompleted::orderId, "42");
                            }

                            record OrderCompleted(String orderId) {
                            }
                        }
                        """
                )
        );
    }

    @Test
    void predicateOverloadsUnchanged() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.function.Predicate;

                        import org.springframework.modulith.test.PublishedEventsAssert.PublishedEventAssert;

                        class Demo {
                            void verify(PublishedEventAssert<OrderCompleted> event, Predicate<String> startsWithFour) {
                                event.matching(OrderCompleted::orderId, id -> id.startsWith("4"))
                                        .matching(OrderCompleted::orderId, startsWithFour)
                                        .matching(order -> order.orderId() != null);
                            }

                            record OrderCompleted(String orderId) {
                            }
                        }
                        """
                )
        );
    }

}
