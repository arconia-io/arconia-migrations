package io.arconia.rewrite.spring.framework.framework7;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for "io.arconia.rewrite.spring.framework7.UpgradeSpringFramework_7_0".
 */
class UpgradeSpringFramework_7_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.framework7.UpgradeSpringFramework_7_0")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-core-6.2.*", "spring-test-6.2.*", "spring-web-6.2.*", "spring-webflux-6.2.*",
                        "spring-webmvc-6.2.*", "jakarta.servlet-api", "reactive-streams"));
    }

    @Test
    void springCoreModule() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.aot.hint.support.FilePatternResourceHintsRegistrar;
                import org.springframework.aot.hint.MemberCategory;

                class Demo {
                    void test(FilePatternResourceHintsRegistrar.Builder registrarBuilder) {
                        registrarBuilder.withClasspathLocations("location");
                        System.out.println(MemberCategory.DECLARED_FIELDS);
                        System.out.println(MemberCategory.PUBLIC_FIELDS);
                    }
                }
                """,
                """
                import org.springframework.aot.hint.support.FilePatternResourceHintsRegistrar;
                import org.springframework.aot.hint.MemberCategory;

                class Demo {
                    void test(FilePatternResourceHintsRegistrar.Builder registrarBuilder) {
                        registrarBuilder.withClassPathLocations("location");
                        System.out.println(MemberCategory.INVOKE_DECLARED_FIELDS);
                        System.out.println(MemberCategory.INVOKE_PUBLIC_FIELDS);
                    }
                }
                """
            )
        );
    }

    @Test
    void springTestModule() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.test.web.servlet.result.StatusResultMatchers;

                class Demo {
                    void test(StatusResultMatchers matchers) {
                        matchers.isPayloadTooLarge();
                        matchers.isUnprocessableEntity();
                    }
                }
                """,
                """
                import org.springframework.test.web.servlet.result.StatusResultMatchers;

                class Demo {
                    void test(StatusResultMatchers matchers) {
                        matchers.isContentTooLarge();
                        matchers.isUnprocessableContent();
                    }
                }
                """
            )
        );
    }

    @Test
    void springWebModule() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
                import org.springframework.http.HttpStatus;
                import org.springframework.http.ResponseEntity;
                import org.springframework.web.client.HttpClientErrorException;
                import org.springframework.web.server.PayloadTooLargeException;

                class Demo {
                    void test(Jackson2ObjectMapperBuilder builder) {
                        var a = HttpStatus.PAYLOAD_TOO_LARGE;
                        var b = HttpStatus.UNPROCESSABLE_ENTITY;
                    }
                    void other() throws PayloadTooLargeException {
                        ResponseEntity.unprocessableEntity();
                    }
                }
                """,
                """
                import org.springframework.http.HttpStatus;
                import org.springframework.http.ResponseEntity;
                import org.springframework.web.client.HttpClientErrorException;
                import org.springframework.web.server.ContentTooLargeException;
                import tools.jackson.databind.json.JsonMapper;

                class Demo {
                    void test(JsonMapper.Builder builder) {
                        var a = HttpStatus.CONTENT_TOO_LARGE;
                        var b = HttpStatus.UNPROCESSABLE_CONTENT;
                    }
                    void other() throws ContentTooLargeException {
                        ResponseEntity.unprocessableContent();
                    }
                }
                """
            )
        );
    }

    @Test
    void springWebFluxModule() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.web.reactive.function.server.ServerResponse;
                import org.springframework.web.reactive.result.view.FragmentsRendering;

                class Demo {
                    void other() {
                        ServerResponse.unprocessableEntity();
                    }
                    void fragments() {
                        FragmentsRendering.with("fragment");
                        FragmentsRendering.withCollection(null);
                        FragmentsRendering.withPublisher(null);
                        FragmentsRendering.withProducer(null);
                    }
                }
                """,
                """
                import org.springframework.web.reactive.function.server.ServerResponse;
                import org.springframework.web.reactive.result.view.FragmentsRendering;

                class Demo {
                    void other() {
                        ServerResponse.unprocessableContent();
                    }
                    void fragments() {
                        FragmentsRendering.fragment("fragment");
                        FragmentsRendering.fragments(null);
                        FragmentsRendering.fragmentsPublisher(null);
                        FragmentsRendering.fragmentsProducer(null);
                    }
                }
                """
            )
        );
    }

    @Test
    void springWebMvcModule() {
        rewriteRun(
            //language=java
            java(
                """
                import jakarta.servlet.http.HttpServletRequest;
                import org.springframework.web.servlet.function.ServerResponse;
                import org.springframework.web.servlet.support.RequestContext;
                import org.springframework.web.servlet.view.FragmentsRendering;

                import java.util.List;
                import java.util.Map;

                class Demo {
                    void other() {
                        ServerResponse.unprocessableEntity();
                    }
                    void fragments() {
                        FragmentsRendering.with("fragment");
                        FragmentsRendering.with("fragment", Map.of());
                        FragmentsRendering.with(List.of());
                    }
                    static class MyRequestContext extends RequestContext {
                        public MyRequestContext(HttpServletRequest request) {
                            super(request);
                            System.out.println(RequestContext.jstlPresent);
                        }
                    }
                }
                """,
                """
                import jakarta.servlet.http.HttpServletRequest;
                import org.springframework.web.servlet.function.ServerResponse;
                import org.springframework.web.servlet.support.RequestContext;
                import org.springframework.web.servlet.view.FragmentsRendering;

                import java.util.List;
                import java.util.Map;

                class Demo {
                    void other() {
                        ServerResponse.unprocessableContent();
                    }
                    void fragments() {
                        FragmentsRendering.fragment("fragment");
                        FragmentsRendering.fragment("fragment", Map.of());
                        FragmentsRendering.fragments(List.of());
                    }
                    static class MyRequestContext extends RequestContext {
                        public MyRequestContext(HttpServletRequest request) {
                            super(request);
                            System.out.println(RequestContext.JSTL_PRESENT);
                        }
                    }
                }
                """
            )
        );
    }

}
