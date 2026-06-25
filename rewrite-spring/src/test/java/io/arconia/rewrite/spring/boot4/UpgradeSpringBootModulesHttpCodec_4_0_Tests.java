package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesHttpCodec_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateHttpCodecModule")
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
                        import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration;
                        import org.springframework.boot.web.codec.CodecCustomizer;

                        class Demo {
                            CodecsAutoConfiguration codecsAutoConfiguration = null;
                            CodecCustomizer codecCustomizer = null;
                        }
                        """,
                        """
                        import org.springframework.boot.http.codec.autoconfigure.CodecsAutoConfiguration;
                        import org.springframework.boot.http.codec.CodecCustomizer;

                        class Demo {
                            CodecsAutoConfiguration codecsAutoConfiguration = null;
                            CodecCustomizer codecCustomizer = null;
                        }
                        """
                )
        );
    }

}
