package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesReactorNetty_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateReactorNettyModule")
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
                        import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
                        import org.springframework.boot.autoconfigure.web.embedded.NettyWebServerFactoryCustomizer;
                        import org.springframework.boot.autoconfigure.reactor.netty.ReactorNettyConfigurations;
                        import org.springframework.boot.autoconfigure.reactor.netty.ReactorNettyProperties;

                        class Demo {
                            NettyReactiveWebServerFactory webServerFactory = null;
                            NettyWebServerFactoryCustomizer customizer = null;
                            ReactorNettyConfigurations configurations = null;
                            ReactorNettyProperties properties = null;
                        }
                        """,
                        """
                        import org.springframework.boot.reactor.netty.NettyReactiveWebServerFactory;
                        import org.springframework.boot.reactor.netty.autoconfigure.NettyReactiveWebServerFactoryCustomizer;
                        import org.springframework.boot.reactor.netty.autoconfigure.ReactorNettyConfigurations;
                        import org.springframework.boot.reactor.netty.autoconfigure.ReactorNettyProperties;

                        class Demo {
                            NettyReactiveWebServerFactory webServerFactory = null;
                            NettyReactiveWebServerFactoryCustomizer customizer = null;
                            ReactorNettyConfigurations configurations = null;
                            ReactorNettyProperties properties = null;
                        }
                        """
                )
        );
    }

}
