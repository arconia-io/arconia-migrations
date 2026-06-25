package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesRestDocs_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateRestDocsModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-test-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
                        import org.springframework.boot.test.autoconfigure.restdocs.RestDocsAutoConfiguration;
                        import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcBuilderCustomizer;
                        import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
                        import org.springframework.boot.test.autoconfigure.restdocs.RestDocsProperties;
                        import org.springframework.boot.test.autoconfigure.restdocs.RestDocsTestExecutionListener;
                        import org.springframework.boot.test.autoconfigure.restdocs.RestDocsWebTestClientConfigurationCustomizer;

                        class Demo {
                            AutoConfigureRestDocs autoConfigureRestDocs = null;
                            RestDocsAutoConfiguration restDocsAutoConfiguration = null;
                            RestDocsMockMvcBuilderCustomizer restDocsMockMvcBuilderCustomizer = null;
                            RestDocsMockMvcConfigurationCustomizer restDocsMockMvcConfigurationCustomizer = null;
                            RestDocsProperties restDocsProperties = null;
                            RestDocsTestExecutionListener restDocsTestExecutionListener = null;
                            RestDocsWebTestClientConfigurationCustomizer restDocsWebTestClientConfigurationCustomizer = null;
                        }
                        """,
                        """
                        import org.springframework.boot.restdocs.test.autoconfigure.*;

                        class Demo {
                            AutoConfigureRestDocs autoConfigureRestDocs = null;
                            RestDocsAutoConfiguration restDocsAutoConfiguration = null;
                            RestDocsMockMvcBuilderCustomizer restDocsMockMvcBuilderCustomizer = null;
                            RestDocsMockMvcConfigurationCustomizer restDocsMockMvcConfigurationCustomizer = null;
                            RestDocsProperties restDocsProperties = null;
                            RestDocsTestExecutionListener restDocsTestExecutionListener = null;
                            RestDocsWebTestClientConfigurationCustomizer restDocsWebTestClientConfigurationCustomizer = null;
                        }
                        """
                )
        );
    }

}
