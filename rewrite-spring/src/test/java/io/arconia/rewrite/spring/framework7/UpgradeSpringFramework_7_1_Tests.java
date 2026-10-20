package io.arconia.rewrite.spring.framework7;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for "io.arconia.rewrite.spring.framework7.UpgradeSpringFramework_7_1".
 */
class UpgradeSpringFramework_7_1_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.framework7.UpgradeSpringFramework_7_1")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-core-7.0", "spring-web-7.0", "jakarta.servlet-api", "reactive-streams"));
    }

    @DocumentExample
    @Test
    void forwardedHeaders() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.web.filter.ForwardedHeaderFilter;
                        import org.springframework.web.server.adapter.ForwardedHeaderTransformer;

                        class Demo {
                            ForwardedHeaderFilter filter() {
                                return new ForwardedHeaderFilter();
                            }

                            ForwardedHeaderTransformer transformer() {
                                return new ForwardedHeaderTransformer();
                            }
                        }
                        """,
                        """
                        import org.springframework.web.filter.ForwardedHeaderFilter;
                        import org.springframework.web.server.adapter.ForwardedHeaderTransformer;

                        class Demo {
                            ForwardedHeaderFilter filter() {
                                return new ForwardedHeaderFilter(false);
                            }

                            ForwardedHeaderTransformer transformer() {
                                return new ForwardedHeaderTransformer(false);
                            }
                        }
                        """
                )
        );
    }

    @Test
    void multipartConverter() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;

                        class Demo {
                            AllEncompassingFormHttpMessageConverter converter = new AllEncompassingFormHttpMessageConverter();
                        }
                        """,
                        """
                        import org.springframework.http.converter.multipart.MultipartHttpMessageConverter;

                        class Demo {
                            MultipartHttpMessageConverter converter = new MultipartHttpMessageConverter();
                        }
                        """
                )
        );
    }

}
