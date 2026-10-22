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
                        "spring-core-7.0", "spring-expression-7.0", "spring-test-7.0", "spring-web-7.0", "jakarta.servlet-api", "reactive-streams"));
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

    @Test
    void spelParserConfigurationBuilder() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.expression.spel.SpelCompilerMode;
                        import org.springframework.expression.spel.SpelParserConfiguration;
                        import org.springframework.expression.spel.standard.SpelExpressionParser;

                        class Demo {
                            SpelParserConfiguration defaults = new SpelParserConfiguration();
                            SpelExpressionParser parser = new SpelExpressionParser(new SpelParserConfiguration(true, true));
                            SpelParserConfiguration limited = new SpelParserConfiguration(false, true, 100);
                            SpelParserConfiguration compiled = new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE, getClass().getClassLoader());
                            SpelParserConfiguration limits = new SpelParserConfiguration(SpelCompilerMode.OFF, null, false, false, Integer.MAX_VALUE, 5000, 1000, 64);
                        }
                        """,
                        """
                        import org.springframework.expression.spel.SpelCompilerMode;
                        import org.springframework.expression.spel.SpelParserConfiguration;
                        import org.springframework.expression.spel.standard.SpelExpressionParser;

                        class Demo {
                            SpelParserConfiguration defaults = SpelParserConfiguration.withDefaults();
                            SpelExpressionParser parser = new SpelExpressionParser(SpelParserConfiguration.builder().autoGrowNullReferences().autoGrowCollections().build());
                            SpelParserConfiguration limited = SpelParserConfiguration.builder().autoGrowCollections().maximumAutoGrowSize(100).build();
                            SpelParserConfiguration compiled = SpelParserConfiguration.builder().compilerMode(SpelCompilerMode.IMMEDIATE).compilerClassLoader(getClass().getClassLoader()).build();
                            SpelParserConfiguration limits = SpelParserConfiguration.builder().compilerMode(SpelCompilerMode.OFF).maximumAutoGrowSize(Integer.MAX_VALUE).maximumExpressionLength(5000).maximumOperations(1000).maximumBigPowerBits(64).build();
                        }
                        """
                ),
                //language=java
                java(
                        """
                        import org.springframework.expression.spel.SpelCompilerMode;
                        import org.springframework.expression.spel.SpelParserConfiguration;

                        class Unchanged {
                            SpelParserConfiguration autoGrow(boolean autoGrow) {
                                return new SpelParserConfiguration(autoGrow, autoGrow);
                            }

                            SpelParserConfiguration compiled(SpelCompilerMode mode) {
                                return new SpelParserConfiguration(mode, null);
                            }

                            SpelParserConfiguration negativeAutoGrowSize() {
                                return new SpelParserConfiguration(true, true, -1);
                            }
                        }
                        """
                )
        );
    }

    @Test
    void jsonConverterDelegate() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.List;

                        import org.springframework.http.converter.HttpMessageConverter;
                        import org.springframework.test.http.HttpMessageContentConverter;

                        class Demo {
                            HttpMessageContentConverter fromList(List<HttpMessageConverter<?>> converters) {
                                return HttpMessageContentConverter.of(converters);
                            }

                            HttpMessageContentConverter fromConverters(HttpMessageConverter<?> first, HttpMessageConverter<?> second) {
                                return HttpMessageContentConverter.of(first, second);
                            }
                        }
                        """,
                        """
                        import java.util.List;

                        import org.springframework.http.converter.HttpMessageConverter;
                        import org.springframework.test.json.JsonConverterDelegate;

                        class Demo {
                            JsonConverterDelegate fromList(List<HttpMessageConverter<?>> converters) {
                                return JsonConverterDelegate.of(converters);
                            }

                            JsonConverterDelegate fromConverters(HttpMessageConverter<?> first, HttpMessageConverter<?> second) {
                                return JsonConverterDelegate.of(List.of(first, second));
                            }
                        }
                        """
                )
        );
    }

}
