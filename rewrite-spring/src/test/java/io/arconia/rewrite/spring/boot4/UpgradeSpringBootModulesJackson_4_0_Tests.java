package io.arconia.rewrite.spring.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;
import static org.openrewrite.java.Assertions.java;

class UpgradeSpringBootModulesJackson_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateJacksonModule")
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
                        import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
                        import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
                        import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
                        import org.springframework.boot.jackson.JsonComponent;
                        import org.springframework.boot.jackson.JsonComponentModule;
                        import org.springframework.boot.jackson.JsonMixin;
                        import org.springframework.boot.jackson.JsonMixinModule;
                        import org.springframework.boot.jackson.JsonMixinModuleEntries;
                        import org.springframework.boot.jackson.JsonObjectDeserializer;
                        import org.springframework.boot.jackson.JsonObjectSerializer;

                        class Demo {
                            Jackson2ObjectMapperBuilderCustomizer customizer = null;
                            JacksonAutoConfiguration autoConfiguration = null;
                            JacksonProperties properties = null;
                            JsonComponent component = null;
                            JsonComponentModule componentModule = null;
                            JsonMixin mixin = null;
                            JsonMixinModule mixinModule = null;
                            JsonMixinModuleEntries mixinModuleEntries = null;
                            JsonObjectDeserializer objectDeserializer = null;
                            JsonObjectSerializer objectSerializer = null;
                        }
                        """,
                        """
                        import org.springframework.boot.jackson.*;
                        import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
                        import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
                        import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;

                        class Demo {
                            JsonMapperBuilderCustomizer customizer = null;
                            JacksonAutoConfiguration autoConfiguration = null;
                            JacksonProperties properties = null;
                            JacksonComponent component = null;
                            JacksonComponentModule componentModule = null;
                            JacksonMixin mixin = null;
                            JacksonMixinModule mixinModule = null;
                            JacksonMixinModuleEntries mixinModuleEntries = null;
                            ObjectValueDeserializer objectDeserializer = null;
                            ObjectValueSerializer objectSerializer = null;
                        }
                        """
                )
        );
    }

    @Test
    void dependencies() {
        rewriteRun(
                spec -> spec.beforeRecipe(withToolingApi()),
                //language=groovy
                buildGradle(
                        """
                        plugins {
                            id 'java-library'
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation "org.springframework.boot:spring-boot-starter-json"
                        }
                        """,
                        """
                        plugins {
                            id 'java-library'
                        }

                        repositories {
                            mavenCentral()
                        }

                        dependencies {
                            implementation "org.springframework.boot:spring-boot-starter-jackson"

                            testImplementation "org.springframework.boot:spring-boot-starter-jackson-test"
                        }
                        """
                )
        );
    }

}
