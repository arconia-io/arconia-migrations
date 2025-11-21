package io.arconia.rewrite.spring.boot.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesCache_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateCacheModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-actuator-3.5.*", "spring-boot-autoconfigure-3.5.*",
                        "spring-boot-actuator-autoconfigure-3.5.*", "spring-boot-test-autoconfigure-3.5.*"));
    }

    @Test
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.cache.CachesEndpoint;
                        import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.cache.CachesEndpointAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.metrics.cache.CacheMetricsAutoConfiguration;
                        import org.springframework.boot.actuate.metrics.cache.CacheMeterBinderProvider;
                        import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;

                        class Demo {
                            CachesEndpoint cachesEndpoint = null;
                            CacheAutoConfiguration cacheAutoConfiguration = null;
                            CachesEndpointAutoConfiguration cachesEndpointAutoConfiguration = null;
                            CacheMetricsAutoConfiguration cacheMetricsAutoConfiguration = null;
                            CacheMeterBinderProvider cacheMeterBinderProvider = null;
                            AutoConfigureCache autoConfigureCache = null;
                        }
                        """,
                        """
                        import org.springframework.boot.cache.actuate.endpoint.CachesEndpoint;
                        import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
                        import org.springframework.boot.cache.autoconfigure.CachesEndpointAutoConfiguration;
                        import org.springframework.boot.cache.autoconfigure.metrics.CacheMetricsAutoConfiguration;
                        import org.springframework.boot.cache.metrics.CacheMeterBinderProvider;
                        import org.springframework.boot.cache.test.autoconfigure.AutoConfigureCache;

                        class Demo {
                            CachesEndpoint cachesEndpoint = null;
                            CacheAutoConfiguration cacheAutoConfiguration = null;
                            CachesEndpointAutoConfiguration cachesEndpointAutoConfiguration = null;
                            CacheMetricsAutoConfiguration cacheMetricsAutoConfiguration = null;
                            CacheMeterBinderProvider cacheMeterBinderProvider = null;
                            AutoConfigureCache autoConfigureCache = null;
                        }
                        """
                )
        );
    }

}
