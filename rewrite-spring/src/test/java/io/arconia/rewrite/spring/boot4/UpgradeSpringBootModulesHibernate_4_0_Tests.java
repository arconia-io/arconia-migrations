package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesHibernate_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateHibernateModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-3.5", "spring-boot-autoconfigure-3.5",
                        "spring-boot-actuator-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.autoconfigure.metrics.orm.jpa.HibernateMetricsAutoConfiguration;
                        import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
                        import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
                        import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
                        import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
                        import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;

                        class Demo {
                            SpringImplicitNamingStrategy implicitNamingStrategy = null;
                            HibernateJpaAutoConfiguration autoConfiguration = null;
                            HibernateProperties properties = null;
                            HibernatePropertiesCustomizer propertiesCustomizer = null;
                            HibernateSettings settings = null;
                            HibernateMetricsAutoConfiguration metricsAutoConfiguration = null;
                        }
                        """,
                        """
                        import org.springframework.boot.hibernate.SpringImplicitNamingStrategy;
                        import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
                        import org.springframework.boot.hibernate.autoconfigure.HibernateProperties;
                        import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
                        import org.springframework.boot.hibernate.autoconfigure.HibernateSettings;
                        import org.springframework.boot.hibernate.autoconfigure.metrics.HibernateMetricsAutoConfiguration;

                        class Demo {
                            SpringImplicitNamingStrategy implicitNamingStrategy = null;
                            HibernateJpaAutoConfiguration autoConfiguration = null;
                            HibernateProperties properties = null;
                            HibernatePropertiesCustomizer propertiesCustomizer = null;
                            HibernateSettings settings = null;
                            HibernateMetricsAutoConfiguration metricsAutoConfiguration = null;
                        }
                        """
                )
        );
    }

}
