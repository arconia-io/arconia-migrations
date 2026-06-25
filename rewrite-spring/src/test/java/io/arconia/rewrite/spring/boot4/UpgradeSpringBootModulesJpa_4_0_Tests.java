package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesJpa_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateJpaModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-3.5", "spring-boot-autoconfigure-3.5", "spring-boot-test-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryBuilderCustomizer;
                        import org.springframework.boot.autoconfigure.orm.jpa.EntityManagerFactoryDependsOnPostProcessor;
                        import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
                        import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
                        import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
                        import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
                        import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
                        import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManagerAutoConfiguration;

                        class Demo {
                            EntityManagerFactoryBuilder entityManagerFactoryBuilder = null;
                            EntityManagerFactoryBuilderCustomizer entityManagerFactoryBuilderCustomizer = null;
                            EntityManagerFactoryDependsOnPostProcessor entityManagerFactoryDependsOnPostProcessor = null;
                            JpaBaseConfiguration jpaBaseConfiguration = null;
                            JpaProperties jpaProperties = null;
                            AutoConfigureTestEntityManager autoConfigureTestEntityManager = null;
                            TestEntityManager testEntityManager = null;
                            TestEntityManagerAutoConfiguration testEntityManagerAutoConfiguration = null;
                        }
                        """,
                        """
                        import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
                        import org.springframework.boot.jpa.autoconfigure.EntityManagerFactoryBuilderCustomizer;
                        import org.springframework.boot.jpa.autoconfigure.EntityManagerFactoryDependsOnPostProcessor;
                        import org.springframework.boot.jpa.autoconfigure.JpaBaseConfiguration;
                        import org.springframework.boot.jpa.autoconfigure.JpaProperties;
                        import org.springframework.boot.jpa.test.autoconfigure.AutoConfigureTestEntityManager;
                        import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
                        import org.springframework.boot.jpa.test.autoconfigure.TestEntityManagerAutoConfiguration;

                        class Demo {
                            EntityManagerFactoryBuilder entityManagerFactoryBuilder = null;
                            EntityManagerFactoryBuilderCustomizer entityManagerFactoryBuilderCustomizer = null;
                            EntityManagerFactoryDependsOnPostProcessor entityManagerFactoryDependsOnPostProcessor = null;
                            JpaBaseConfiguration jpaBaseConfiguration = null;
                            JpaProperties jpaProperties = null;
                            AutoConfigureTestEntityManager autoConfigureTestEntityManager = null;
                            TestEntityManager testEntityManager = null;
                            TestEntityManagerAutoConfiguration testEntityManagerAutoConfiguration = null;
                        }
                        """
                )
        );
    }

}
