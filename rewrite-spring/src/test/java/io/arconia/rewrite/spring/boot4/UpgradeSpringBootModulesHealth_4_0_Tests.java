package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesHealth_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateHealthModule")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "spring-boot-actuator-3.5", "spring-boot-actuator-autoconfigure-3.5"));
    }

    @Test
    @DocumentExample
    void typeChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import org.springframework.boot.actuate.autoconfigure.availability.AvailabilityHealthContributorAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.availability.AvailabilityProbesAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthContributorConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.health.CompositeReactiveHealthContributorConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
                        import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointProperties;
                        import org.springframework.boot.actuate.autoconfigure.health.HealthProperties;
                        import org.springframework.boot.actuate.autoconfigure.ssl.SslHealthContributorAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.ssl.SslHealthIndicatorProperties;
                        import org.springframework.boot.actuate.autoconfigure.system.DiskSpaceHealthContributorAutoConfiguration;
                        import org.springframework.boot.actuate.autoconfigure.system.DiskSpaceHealthIndicatorProperties;
                        import org.springframework.boot.actuate.availability.AvailabilityStateHealthIndicator;
                        import org.springframework.boot.actuate.health.AbstractHealthIndicator;
                        import org.springframework.boot.actuate.health.AbstractReactiveHealthIndicator;
                        import org.springframework.boot.actuate.health.AdditionalHealthEndpointPath;
                        import org.springframework.boot.actuate.health.CompositeHealthContributor;
                        import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
                        import org.springframework.boot.actuate.health.DefaultHealthContributorRegistry;
                        import org.springframework.boot.actuate.health.DefaultReactiveHealthContributorRegistry;
                        import org.springframework.boot.actuate.health.Health;
                        import org.springframework.boot.actuate.health.HealthContributor;
                        import org.springframework.boot.actuate.health.HealthContributorRegistry;
                        import org.springframework.boot.actuate.health.HealthEndpoint;
                        import org.springframework.boot.actuate.health.HealthEndpointGroup;
                        import org.springframework.boot.actuate.health.HealthEndpointGroups;
                        import org.springframework.boot.actuate.health.HealthEndpointGroupsPostProcessor;
                        import org.springframework.boot.actuate.health.HealthEndpointWebExtension;
                        import org.springframework.boot.actuate.health.HealthIndicator;
                        import org.springframework.boot.actuate.health.HttpCodeStatusMapper;
                        import org.springframework.boot.actuate.health.PingHealthIndicator;
                        import org.springframework.boot.actuate.health.ReactiveHealthContributor;
                        import org.springframework.boot.actuate.health.ReactiveHealthContributorRegistry;
                        import org.springframework.boot.actuate.health.ReactiveHealthEndpointWebExtension;
                        import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
                        import org.springframework.boot.actuate.health.SimpleHttpCodeStatusMapper;
                        import org.springframework.boot.actuate.health.SimpleStatusAggregator;
                        import org.springframework.boot.actuate.health.Status;
                        import org.springframework.boot.actuate.health.StatusAggregator;
                        import org.springframework.boot.actuate.ssl.SslHealthIndicator;
                        import org.springframework.boot.actuate.system.DiskSpaceHealthIndicator;

                        class Demo {
                            AdditionalHealthEndpointPath additionalHealthEndpointPath = null;
                            HealthEndpoint healthEndpoint = null;
                            HealthEndpointGroup healthEndpointGroup = null;
                            HealthEndpointGroups healthEndpointGroups = null;
                            HealthEndpointGroupsPostProcessor healthEndpointGroupsPostProcessor = null;
                            HealthEndpointWebExtension healthEndpointWebExtension = null;
                            HttpCodeStatusMapper httpCodeStatusMapper = null;
                            ReactiveHealthEndpointWebExtension reactiveHealthEndpointWebExtension = null;
                            SimpleHttpCodeStatusMapper simpleHttpCodeStatusMapper = null;
                            SimpleStatusAggregator simpleStatusAggregator = null;
                            StatusAggregator statusAggregator = null;
                            AvailabilityStateHealthIndicator availabilityStateHealthIndicator = null;
                            SslHealthIndicator sslHealthIndicator = null;
                            DiskSpaceHealthIndicator diskSpaceHealthIndicator = null;
                            AvailabilityProbesAutoConfiguration availabilityProbesAutoConfiguration = null;
                            HealthEndpointAutoConfiguration healthEndpointAutoConfiguration = null;
                            HealthEndpointProperties healthEndpointProperties = null;
                            HealthProperties healthProperties = null;
                            AvailabilityHealthContributorAutoConfiguration availabilityHealthContributorAutoConfiguration = null;
                            DiskSpaceHealthContributorAutoConfiguration diskSpaceHealthContributorAutoConfiguration = null;
                            DiskSpaceHealthIndicatorProperties diskSpaceHealthIndicatorProperties = null;
                            SslHealthContributorAutoConfiguration sslHealthContributorAutoConfiguration = null;
                            SslHealthIndicatorProperties sslHealthIndicatorProperties = null;
                            CompositeHealthContributorConfiguration compositeHealthContributorConfiguration = null;
                            CompositeReactiveHealthContributorConfiguration compositeReactiveHealthContributorConfiguration = null;
                            ConditionalOnEnabledHealthIndicator conditionalOnEnabledHealthIndicator = null;
                            HealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            AbstractHealthIndicator abstractHealthIndicator = null;
                            AbstractReactiveHealthIndicator abstractReactiveHealthIndicator = null;
                            CompositeHealthContributor compositeHealthContributor = null;
                            CompositeReactiveHealthContributor compositeReactiveHealthContributor = null;
                            Health health = null;
                            HealthContributor healthContributor = null;
                            HealthIndicator healthIndicator = null;
                            PingHealthIndicator pingHealthIndicator = null;
                            ReactiveHealthContributor reactiveHealthContributor = null;
                            ReactiveHealthIndicator reactiveHealthIndicator = null;
                            Status status = null;
                            DefaultHealthContributorRegistry defaultHealthContributorRegistry = null;
                            DefaultReactiveHealthContributorRegistry defaultReactiveHealthContributorRegistry = null;
                            HealthContributorRegistry healthContributorRegistry = null;
                            ReactiveHealthContributorRegistry reactiveHealthContributorRegistry = null;
                        }
                        """,
                        """
                        import org.springframework.boot.health.application.AvailabilityStateHealthIndicator;
                        import org.springframework.boot.health.application.SslHealthIndicator;
                        import org.springframework.boot.health.autoconfigure.actuate.endpoint.AvailabilityProbesAutoConfiguration;
                        import org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointAutoConfiguration;
                        import org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointProperties;
                        import org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthProperties;
                        import org.springframework.boot.health.autoconfigure.application.*;
                        import org.springframework.boot.health.autoconfigure.contributor.CompositeHealthContributorConfiguration;
                        import org.springframework.boot.health.autoconfigure.contributor.CompositeReactiveHealthContributorConfiguration;
                        import org.springframework.boot.health.autoconfigure.contributor.ConditionalOnEnabledHealthIndicator;
                        import org.springframework.boot.health.autoconfigure.contributor.HealthContributorAutoConfiguration;
                        import org.springframework.boot.health.contributor.*;
                        import org.springframework.boot.health.registry.DefaultHealthContributorRegistry;
                        import org.springframework.boot.health.registry.DefaultReactiveHealthContributorRegistry;
                        import org.springframework.boot.health.registry.HealthContributorRegistry;
                        import org.springframework.boot.health.registry.ReactiveHealthContributorRegistry;
                        import org.springframework.boot.health.application.DiskSpaceHealthIndicator;
                        import org.springframework.boot.health.actuate.endpoint.*;

                        class Demo {
                            AdditionalHealthEndpointPath additionalHealthEndpointPath = null;
                            HealthEndpoint healthEndpoint = null;
                            HealthEndpointGroup healthEndpointGroup = null;
                            HealthEndpointGroups healthEndpointGroups = null;
                            HealthEndpointGroupsPostProcessor healthEndpointGroupsPostProcessor = null;
                            HealthEndpointWebExtension healthEndpointWebExtension = null;
                            HttpCodeStatusMapper httpCodeStatusMapper = null;
                            ReactiveHealthEndpointWebExtension reactiveHealthEndpointWebExtension = null;
                            SimpleHttpCodeStatusMapper simpleHttpCodeStatusMapper = null;
                            SimpleStatusAggregator simpleStatusAggregator = null;
                            StatusAggregator statusAggregator = null;
                            AvailabilityStateHealthIndicator availabilityStateHealthIndicator = null;
                            SslHealthIndicator sslHealthIndicator = null;
                            DiskSpaceHealthIndicator diskSpaceHealthIndicator = null;
                            AvailabilityProbesAutoConfiguration availabilityProbesAutoConfiguration = null;
                            HealthEndpointAutoConfiguration healthEndpointAutoConfiguration = null;
                            HealthEndpointProperties healthEndpointProperties = null;
                            HealthProperties healthProperties = null;
                            AvailabilityHealthContributorAutoConfiguration availabilityHealthContributorAutoConfiguration = null;
                            DiskSpaceHealthContributorAutoConfiguration diskSpaceHealthContributorAutoConfiguration = null;
                            DiskSpaceHealthIndicatorProperties diskSpaceHealthIndicatorProperties = null;
                            SslHealthContributorAutoConfiguration sslHealthContributorAutoConfiguration = null;
                            SslHealthIndicatorProperties sslHealthIndicatorProperties = null;
                            CompositeHealthContributorConfiguration compositeHealthContributorConfiguration = null;
                            CompositeReactiveHealthContributorConfiguration compositeReactiveHealthContributorConfiguration = null;
                            ConditionalOnEnabledHealthIndicator conditionalOnEnabledHealthIndicator = null;
                            HealthContributorAutoConfiguration healthContributorAutoConfiguration = null;
                            AbstractHealthIndicator abstractHealthIndicator = null;
                            AbstractReactiveHealthIndicator abstractReactiveHealthIndicator = null;
                            CompositeHealthContributor compositeHealthContributor = null;
                            CompositeReactiveHealthContributor compositeReactiveHealthContributor = null;
                            Health health = null;
                            HealthContributor healthContributor = null;
                            HealthIndicator healthIndicator = null;
                            PingHealthIndicator pingHealthIndicator = null;
                            ReactiveHealthContributor reactiveHealthContributor = null;
                            ReactiveHealthIndicator reactiveHealthIndicator = null;
                            Status status = null;
                            DefaultHealthContributorRegistry defaultHealthContributorRegistry = null;
                            DefaultReactiveHealthContributorRegistry defaultReactiveHealthContributorRegistry = null;
                            HealthContributorRegistry healthContributorRegistry = null;
                            ReactiveHealthContributorRegistry reactiveHealthContributorRegistry = null;
                        }
                        """
                )
        );
    }

}
