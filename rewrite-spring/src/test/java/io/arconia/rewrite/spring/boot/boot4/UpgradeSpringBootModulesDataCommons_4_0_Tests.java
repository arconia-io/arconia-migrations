package io.arconia.rewrite.spring.boot.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesDataCommons_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateDataCommonsModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-actuator-3.5", "spring-boot-actuator-autoconfigure-3.5",
                        "spring-boot-autoconfigure-3.5"));
    }

    @Test
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.autoconfigure.metrics.data.RepositoryMetricsAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.PropertiesAutoTimer;
                        import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
                        import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
                        import org.springframework.boot.actuate.metrics.AutoTimer;
                        import org.springframework.boot.actuate.metrics.data.DefaultRepositoryTagsProvider;
                        import org.springframework.boot.actuate.metrics.data.MetricsRepositoryMethodInvocationListener;
                        import org.springframework.boot.actuate.metrics.data.RepositoryTagsProvider;
                        import org.springframework.boot.actuate.metrics.annotation.TimedAnnotations;

                        class Demo {
                            RepositoryMetricsAutoConfiguration autoConfiguration = null;
                            PropertiesAutoTimer propertiesAutoTimer = null;
                            SpringDataWebAutoConfiguration springDataWebAutoConfiguration = null;
                            SpringDataWebProperties springDataWebProperties = null;
                            AutoTimer autoTimer = null;
                            DefaultRepositoryTagsProvider defaultRepositoryTagsProvider = null;
                            MetricsRepositoryMethodInvocationListener metricsRepositoryMethodInvocationListener = null;
                            RepositoryTagsProvider repositoryTagsProvider = null;
                            TimedAnnotations timedAnnotations = null;
                        }
                        """,
                        """
                        import org.springframework.boot.data.autoconfigure.metrics.DataRepositoryMetricsAutoConfiguration;
                        import org.springframework.boot.data.autoconfigure.metrics.PropertiesAutoTimer;
                        import org.springframework.boot.data.autoconfigure.web.DataWebAutoConfiguration;
                        import org.springframework.boot.data.autoconfigure.web.DataWebProperties;
                        import org.springframework.boot.data.metrics.*;

                        class Demo {
                            DataRepositoryMetricsAutoConfiguration autoConfiguration = null;
                            PropertiesAutoTimer propertiesAutoTimer = null;
                            DataWebAutoConfiguration springDataWebAutoConfiguration = null;
                            DataWebProperties springDataWebProperties = null;
                            AutoTimer autoTimer = null;
                            DefaultRepositoryTagsProvider defaultRepositoryTagsProvider = null;
                            MetricsRepositoryMethodInvocationListener metricsRepositoryMethodInvocationListener = null;
                            RepositoryTagsProvider repositoryTagsProvider = null;
                            TimedAnnotations timedAnnotations = null;
                        }
                        """
                )
        );
    }

}
