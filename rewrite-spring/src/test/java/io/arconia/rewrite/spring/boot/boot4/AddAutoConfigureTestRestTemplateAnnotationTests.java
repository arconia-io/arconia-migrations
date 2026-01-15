package io.arconia.rewrite.spring.boot.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link AddAutoConfigureTestRestTemplateAnnotation}.
 */
class AddAutoConfigureTestRestTemplateAnnotationTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AddAutoConfigureTestRestTemplateAnnotation())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-beans-7.0", "spring-boot-test-4.0", "spring-boot-resttestclient-4.0",
                        "spring-boot-webmvc-test-4.0", "spring-context-7.0", "spring-test-7.0"));
    }

    @DocumentExample
    @Test
    void springBootTestWithTestRestTemplate() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.beans.factory.annotation.Autowired;
                import org.springframework.boot.resttestclient.TestRestTemplate;
                import org.springframework.boot.test.context.SpringBootTest;

                @SpringBootTest
                class MyControllerTest {
                  @Autowired
                  private TestRestTemplate testRestTemplate;
                }
                """,
                """
                import org.springframework.beans.factory.annotation.Autowired;
                import org.springframework.boot.resttestclient.TestRestTemplate;
                import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
                import org.springframework.boot.test.context.SpringBootTest;

                @AutoConfigureTestRestTemplate
                @SpringBootTest
                class MyControllerTest {
                  @Autowired
                  private TestRestTemplate testRestTemplate;
                }
                """
            )
        );
    }

    @Test
    void springBootTestAlreadyHasAutoConfigureTestRestTemplateAnnotation() {
        rewriteRun(
            //language=java
            java(
                """
                  import org.springframework.beans.factory.annotation.Autowired;
                  import org.springframework.boot.resttestclient.TestRestTemplate;
                  import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
                  import org.springframework.boot.test.context.SpringBootTest;

                  @AutoConfigureTestRestTemplate
                  @SpringBootTest
                  class MyControllerTest {
                    @Autowired
                    private TestRestTemplate testRestTemplate;
                  }
                  """
            )
        );
    }

    @Test
    void springBootTestWithoutTestRestTemplate() {
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
    void noTestAnnotationWithTestRestTemplate() {
        rewriteRun(
            //language=java
            java(
                """
                  import org.springframework.beans.factory.annotation.Autowired;
                  import org.springframework.boot.resttestclient.TestRestTemplate;

                  class MyControllerTest {
                    @Autowired
                    private TestRestTemplate testRestTemplate;
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
                  import org.springframework.boot.resttestclient.TestRestTemplate;
                  import org.springframework.boot.test.context.SpringBootTest;
                  import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

                  @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
                  class MyControllerTest {
                    @Autowired
                    private TestRestTemplate testRestTemplate;
                  }
                  """,
                """
                  import org.springframework.beans.factory.annotation.Autowired;
                  import org.springframework.boot.resttestclient.TestRestTemplate;
                  import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
                  import org.springframework.boot.test.context.SpringBootTest;
                  import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

                  @AutoConfigureTestRestTemplate
                  @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
                  class MyControllerTest {
                    @Autowired
                    private TestRestTemplate testRestTemplate;
                  }
                  """
            )
        );
    }

    @Test
    void webMvcTestWithTestRestTemplate() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.beans.factory.annotation.Autowired;
                import org.springframework.boot.resttestclient.TestRestTemplate;
                import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

                @WebMvcTest
                class MyControllerTest {
                  @Autowired
                  private TestRestTemplate testRestTemplate;
                }
                """,
                """
                import org.springframework.beans.factory.annotation.Autowired;
                import org.springframework.boot.resttestclient.TestRestTemplate;
                import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
                import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

                @AutoConfigureTestRestTemplate
                @WebMvcTest
                class MyControllerTest {
                  @Autowired
                  private TestRestTemplate testRestTemplate;
                }
                """
            )
        );
    }

    @Test
    void webMvcTestAlreadyHasAutoConfigureTestRestTemplateAnnotation() {
        rewriteRun(
            //language=java
            java(
                """
                import org.springframework.beans.factory.annotation.Autowired;
                import org.springframework.boot.resttestclient.TestRestTemplate;
                import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
                import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

                @AutoConfigureTestRestTemplate
                @WebMvcTest
                class MyControllerTest {
                  @Autowired
                  private TestRestTemplate testRestTemplate;
                }
                """
            )
        );
    }

    @Test
    void webMvcTestWithoutTestRestTemplate() {
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
                import org.springframework.boot.resttestclient.TestRestTemplate;
                import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

                @WebMvcTest(SomeController.class)
                class MyControllerTest {
                  @Autowired
                  private TestRestTemplate testRestTemplate;
                }

                class SomeController {}
                """,
                """
                import org.springframework.beans.factory.annotation.Autowired;
                import org.springframework.boot.resttestclient.TestRestTemplate;
                import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
                import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

                @AutoConfigureTestRestTemplate
                @WebMvcTest(SomeController.class)
                class MyControllerTest {
                  @Autowired
                  private TestRestTemplate testRestTemplate;
                }

                class SomeController {}
                """
            )
        );
    }

}
