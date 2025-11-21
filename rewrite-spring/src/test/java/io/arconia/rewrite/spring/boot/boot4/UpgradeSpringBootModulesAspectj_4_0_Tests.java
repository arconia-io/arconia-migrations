package io.arconia.rewrite.spring.boot.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.toolingapi.Assertions.withToolingApi;

class UpgradeSpringBootModulesAspectj_4_0_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.spring.boot4.MigrateAspectjModule");
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
                              testImplementation "org.springframework.boot:spring-boot-starter-aop"
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
                              testImplementation "org.springframework.boot:spring-boot-starter-aspectj"
                          }
                          """
                )
        );
    }

}
