package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesDataRedis_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateDataRedisModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-autoconfigure-3.5", "spring-boot-actuator-autoconfigure-3.5",
                        "spring-boot-actuator-3.5", "spring-boot-test-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.autoconfigure.metrics.redis.LettuceMetricsAutoConfiguration;
                        import org.springframework.boot.actuate.data.redis.RedisHealthIndicator;
                        import org.springframework.boot.actuate.data.redis.RedisReactiveHealthIndicator;
                        import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
                        import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
                        import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
                        import org.springframework.boot.autoconfigure.data.redis.LettuceClientOptionsBuilderCustomizer;
                        import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
                        import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
                        import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
                        import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
                        import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
                        import org.springframework.boot.test.autoconfigure.data.redis.AutoConfigureDataRedis;
                        import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;

                        class Demo {
                            LettuceMetricsAutoConfiguration lettuceObservationAutoConfiguration = null;
                            RedisHealthIndicator dataRedisHealthIndicator = null;
                            RedisReactiveHealthIndicator dataRedisReactiveHealthIndicator = null;
                            ClientResourcesBuilderCustomizer clientResourcesBuilderCustomizer = null;
                            JedisClientConfigurationBuilderCustomizer jedisClientConfigurationBuilderCustomizer = null;
                            LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationBuilderCustomizer = null;
                            LettuceClientOptionsBuilderCustomizer lettuceClientOptionsBuilderCustomizer = null;
                            RedisAutoConfiguration dataRedisAutoConfiguration = null;
                            RedisConnectionDetails dataRedisConnectionDetails = null;
                            RedisProperties dataRedisProperties = null;
                            RedisReactiveAutoConfiguration dataRedisReactiveAutoConfiguration = null;
                            RedisRepositoriesAutoConfiguration dataRedisRepositoriesAutoConfiguration = null;
                            AutoConfigureDataRedis autoConfigureDataRedis = null;
                            DataRedisTest dataRedisTest = null;
                        }
                        """,
                        """
                        import org.springframework.boot.data.redis.autoconfigure.*;
                        import org.springframework.boot.data.redis.autoconfigure.observation.LettuceObservationAutoConfiguration;
                        import org.springframework.boot.data.redis.health.DataRedisHealthIndicator;
                        import org.springframework.boot.data.redis.health.DataRedisReactiveHealthIndicator;
                        import org.springframework.boot.data.redis.test.autoconfigure.AutoConfigureDataRedis;
                        import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;

                        class Demo {
                            LettuceObservationAutoConfiguration lettuceObservationAutoConfiguration = null;
                            DataRedisHealthIndicator dataRedisHealthIndicator = null;
                            DataRedisReactiveHealthIndicator dataRedisReactiveHealthIndicator = null;
                            ClientResourcesBuilderCustomizer clientResourcesBuilderCustomizer = null;
                            JedisClientConfigurationBuilderCustomizer jedisClientConfigurationBuilderCustomizer = null;
                            LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationBuilderCustomizer = null;
                            LettuceClientOptionsBuilderCustomizer lettuceClientOptionsBuilderCustomizer = null;
                            DataRedisAutoConfiguration dataRedisAutoConfiguration = null;
                            DataRedisConnectionDetails dataRedisConnectionDetails = null;
                            DataRedisProperties dataRedisProperties = null;
                            DataRedisReactiveAutoConfiguration dataRedisReactiveAutoConfiguration = null;
                            DataRedisRepositoriesAutoConfiguration dataRedisRepositoriesAutoConfiguration = null;
                            AutoConfigureDataRedis autoConfigureDataRedis = null;
                            DataRedisTest dataRedisTest = null;
                        }
                        """
                )
        );
    }

}
