package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesPersistence_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigratePersistenceModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration;
                        import org.springframework.boot.autoconfigure.domain.EntityScan;
                        import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
                        import org.springframework.boot.autoconfigure.domain.EntityScanner;

                        class Demo {
                            PersistenceExceptionTranslationAutoConfiguration autoConfiguration = null;
                            EntityScan entityScan = null;
                            EntityScanPackages entityScanPackages = null;
                            EntityScanner entityScanner = null;
                        }
                        """,
                        """
                        import org.springframework.boot.persistence.autoconfigure.EntityScan;
                        import org.springframework.boot.persistence.autoconfigure.EntityScanPackages;
                        import org.springframework.boot.persistence.autoconfigure.EntityScanner;
                        import org.springframework.boot.persistence.autoconfigure.PersistenceExceptionTranslationAutoConfiguration;

                        class Demo {
                            PersistenceExceptionTranslationAutoConfiguration autoConfiguration = null;
                            EntityScan entityScan = null;
                            EntityScanPackages entityScanPackages = null;
                            EntityScanner entityScanner = null;
                        }
                        """
                )
        );
    }

}
