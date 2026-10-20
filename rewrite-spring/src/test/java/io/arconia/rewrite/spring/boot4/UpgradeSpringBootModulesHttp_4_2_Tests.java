package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesHttp_4_2_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateSpringBootHttp_4_2")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-http-converter-4.1", "spring-web-7.0"));
    }

    @DocumentExample
    @Test
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.http.converter.autoconfigure.HttpMessageConverters;
                        import org.springframework.http.converter.HttpMessageConverter;

                        class Demo {
                            void test(HttpMessageConverters converters) {
                                for (HttpMessageConverter<?> converter : converters) {
                                    System.out.println(converter);
                                }
                            }
                        }
                        """,
                        """
                        import org.springframework.http.converter.HttpMessageConverter;
                        import org.springframework.http.converter.HttpMessageConverters;

                        class Demo {
                            void test(HttpMessageConverters converters) {
                                for (HttpMessageConverter<?> converter : converters) {
                                    System.out.println(converter);
                                }
                            }
                        }
                        """
                )
        );
    }

}
