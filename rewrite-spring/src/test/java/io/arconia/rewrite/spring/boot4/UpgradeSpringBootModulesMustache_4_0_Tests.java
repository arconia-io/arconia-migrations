package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesMustache_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateMustacheModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-3.5", "spring-boot-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.mustache.MustacheProperties;

                        class Demo {
                            MustacheProperties mustacheProperties = null;
                            org.springframework.boot.web.reactive.result.view.MustacheView reactiveView = null;
                            org.springframework.boot.web.reactive.result.view.MustacheViewResolver reactiveViewResolver = null;
                            org.springframework.boot.web.servlet.view.MustacheView servletView = null;
                            org.springframework.boot.web.servlet.view.MustacheViewResolver servletViewResolver = null;
                        }
                        """,
                        """
                        import org.springframework.boot.mustache.autoconfigure.MustacheProperties;
                        import org.springframework.boot.mustache.reactive.view.MustacheView;
                        import org.springframework.boot.mustache.reactive.view.MustacheViewResolver;

                        class Demo {
                            MustacheProperties mustacheProperties = null;
                            MustacheView reactiveView = null;
                            MustacheViewResolver reactiveViewResolver = null;
                            org.springframework.boot.mustache.servlet.view.MustacheView servletView = null;
                            org.springframework.boot.mustache.servlet.view.MustacheViewResolver servletViewResolver = null;
                        }
                        """
                )
        );
    }

}
