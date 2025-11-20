package io.arconia.rewrite.spring.boot.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link AddAutoConfigureWebTestClientAnnotation}.
 */
class AddAutoConfigureWebTestClientAnnotationTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AddAutoConfigureWebTestClientAnnotation())
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-beans-7.0.*", "spring-boot-test-4.0.*", "spring-boot-webtestclient-4.0.*", "spring-test-7.0.*"));
    }

    @DocumentExample
    @Test
    void springBootTestWithWebTestClient() {
        rewriteRun(
            //language=java
            java(
                  """
                  import org.springframework.beans.factory.annotation.Autowired;
                  import org.springframework.boot.test.context.SpringBootTest;
                  import org.springframework.test.web.reactive.server.WebTestClient;

                  @SpringBootTest
                  class MyControllerTest {
                    @Autowired
                    private WebTestClient webTestClient;
                  }
                  """,
                """
                  import org.springframework.beans.factory.annotation.Autowired;
                  import org.springframework.boot.test.context.SpringBootTest;
                  import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
                  import org.springframework.test.web.reactive.server.WebTestClient;

                  @AutoConfigureWebTestClient
                  @SpringBootTest
                  class MyControllerTest {
                    @Autowired
                    private WebTestClient webTestClient;
                  }
                  """
            )
        );
    }

    @Test
    void alreadyHasAutoConfigureWebTestClientAnnotation() {
        rewriteRun(
            //language=java
            java(
                """
                  import org.springframework.beans.factory.annotation.Autowired;
                  import org.springframework.boot.test.context.SpringBootTest;
                  import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
                  import org.springframework.test.web.reactive.server.WebTestClient;

                  @AutoConfigureWebTestClient
                  @SpringBootTest
                  class MyControllerTest {
                    @Autowired
                    private WebTestClient webTestClient;
                  }
                  """
            )
        );
    }

    @Test
    void springBootTestWithoutWebTestClient() {
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
    void noTestAnnotationWithWebTestClient() {
        rewriteRun(
            //language=java
            java(
                """
                  import org.springframework.beans.factory.annotation.Autowired;
                  import org.springframework.test.web.reactive.server.WebTestClient;

                  class MyControllerTest {
                    @Autowired
                    private WebTestClient webTestClient;
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
                  import org.springframework.test.web.reactive.server.WebTestClient;

                  @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
                  class MyControllerTest {
                    @Autowired
                    private WebTestClient webTestClient;
                  }
                  """,
                """
                  import org.springframework.beans.factory.annotation.Autowired;
                  import org.springframework.boot.test.context.SpringBootTest;
                  import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
                  import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
                  import org.springframework.test.web.reactive.server.WebTestClient;

                  @AutoConfigureWebTestClient
                  @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
                  class MyControllerTest {
                    @Autowired
                    private WebTestClient webTestClient;
                  }
                  """
            )
        );
    }

}
