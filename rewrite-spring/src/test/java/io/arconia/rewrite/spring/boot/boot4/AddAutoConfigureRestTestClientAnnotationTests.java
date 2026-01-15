package io.arconia.rewrite.spring.boot.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link AddAutoConfigureRestTestClientAnnotation}.
 */
class AddAutoConfigureRestTestClientAnnotationTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AddAutoConfigureRestTestClientAnnotation())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-beans-7.0", "spring-boot-test-4.0", "spring-boot-resttestclient-4.0",
                        "spring-boot-webmvc-test-4.0", "spring-context-7.0", "spring-test-7.0"));
    }

    @DocumentExample
    @Test
    void springBootTestWithRestTestClient() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.beans.factory.annotation.Autowired;
                import org.springframework.boot.test.context.SpringBootTest;
                import org.springframework.test.web.servlet.client.RestTestClient;

                @SpringBootTest
                class MyControllerTest {
                  @Autowired
                  private RestTestClient restTestClient;
                }
                """,
                """
                import org.springframework.beans.factory.annotation.Autowired;
                import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
                import org.springframework.boot.test.context.SpringBootTest;
                import org.springframework.test.web.servlet.client.RestTestClient;

                @AutoConfigureRestTestClient
                @SpringBootTest
                class MyControllerTest {
                  @Autowired
                  private RestTestClient restTestClient;
                }
                """
            )
        );
    }

    @Test
    void springBootTestAlreadyHasAutoConfigureRestTestClientAnnotation() {
        rewriteRun(
            //language=java
            java(
                """
                  import org.springframework.beans.factory.annotation.Autowired;
                  import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
                  import org.springframework.boot.test.context.SpringBootTest;
                  import org.springframework.test.web.servlet.client.RestTestClient;

                  @AutoConfigureRestTestClient
                  @SpringBootTest
                  class MyControllerTest {
                    @Autowired
                    private RestTestClient restTestClient;
                  }
                  """
            )
        );
    }

    @Test
    void springBootTestWithoutRestTestClient() {
        rewriteRun(
            //language=java
            java(
                """
                 import org.springframework.boot.test.context.SpringBootTest;

                 @SpringBootTest
                 class MyControllerTest {
                   void testSomething() {
                     // test code
                   }
                 }
                 """
            )
        );
    }

    @Test
    void noTestAnnotationWithRestTestClient() {
        rewriteRun(
            //language=java
            java(
                """
                  import org.springframework.beans.factory.annotation.Autowired;
                  import org.springframework.test.web.servlet.client.RestTestClient;

                  class MyControllerTest {
                    @Autowired
                    private RestTestClient restTestClient;
                  }
                  """
            )
        );
    }

    @Test
    void springBootTestWithWebEnvironment() {
        rewriteRun(
            //language=java
            java(
                """
                  import org.springframework.beans.factory.annotation.Autowired;
                  import org.springframework.boot.test.context.SpringBootTest;
                  import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
                  import org.springframework.test.web.servlet.client.RestTestClient;

                  @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
                  class MyControllerTest {
                    @Autowired
                    private RestTestClient restTestClient;
                  }
                  """,
                """
                  import org.springframework.beans.factory.annotation.Autowired;
                  import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
                  import org.springframework.boot.test.context.SpringBootTest;
                  import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
                  import org.springframework.test.web.servlet.client.RestTestClient;

                  @AutoConfigureRestTestClient
                  @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
                  class MyControllerTest {
                    @Autowired
                    private RestTestClient restTestClient;
                  }
                  """
            )
        );
    }

    @Test
    void webMvcTestWithRestTestClient() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.beans.factory.annotation.Autowired;
                import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
                import org.springframework.test.web.servlet.client.RestTestClient;

                @WebMvcTest
                class MyControllerTest {
                  @Autowired
                  private RestTestClient restTestClient;
                }
                """,
                """
                import org.springframework.beans.factory.annotation.Autowired;
                import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
                import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
                import org.springframework.test.web.servlet.client.RestTestClient;

                @AutoConfigureRestTestClient
                @WebMvcTest
                class MyControllerTest {
                  @Autowired
                  private RestTestClient restTestClient;
                }
                """
            )
        );
    }

    @Test
    void webMvcTestAlreadyHasAutoConfigureRestTestClientAnnotation() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.beans.factory.annotation.Autowired;
                import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
                import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
                import org.springframework.test.web.servlet.client.RestTestClient;

                @AutoConfigureRestTestClient
                @WebMvcTest
                class MyControllerTest {
                  @Autowired
                  private RestTestClient restTestClient;
                }
                """
            )
        );
    }

    @Test
    void webMvcTestWithoutRestTestClient() {
        rewriteRun(
            //language=java
            java(
                """
                 import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

                 @WebMvcTest
                 class MyControllerTest {
                   void testSomething() {
                     // test code
                   }
                 }
                 """
            )
        );
    }

    @Test
    void webMvcTestWithController() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.beans.factory.annotation.Autowired;
                import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
                import org.springframework.test.web.servlet.client.RestTestClient;

                @WebMvcTest(SomeController.class)
                class MyControllerTest {
                  @Autowired
                  private RestTestClient restTestClient;
                }

                class SomeController {}
                """,
                """
                import org.springframework.beans.factory.annotation.Autowired;
                import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
                import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
                import org.springframework.test.web.servlet.client.RestTestClient;

                @AutoConfigureRestTestClient
                @WebMvcTest(SomeController.class)
                class MyControllerTest {
                  @Autowired
                  private RestTestClient restTestClient;
                }

                class SomeController {}
                """
            )
        );
    }

}
