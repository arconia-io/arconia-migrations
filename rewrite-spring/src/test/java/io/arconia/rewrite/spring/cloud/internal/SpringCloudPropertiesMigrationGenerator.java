package io.arconia.rewrite.spring.cloud.internal;

import java.io.IOException;
import java.nio.file.Path;

import io.arconia.rewrite.spring.internal.PropertiesMigrationGenerator;
import io.arconia.rewrite.spring.internal.PropertiesMigrationGenerator.Spec;

/**
 * Generates Spring Cloud properties migration recipes from {@code spring-cloud-dependencies},
 * which aggregates a number of per-project BOMs via {@code <scope>import</scope>}.
 * <p>
 * Usage: {@code java SpringCloudPropertiesMigrationGenerator 2025.0.0}
 */
public class SpringCloudPropertiesMigrationGenerator {

    public static void main(String[] args) throws IOException, InterruptedException {
        PropertiesMigrationGenerator.run(args, SpringCloudPropertiesMigrationGenerator.class, Spec.builder()
                .groupPath("org/springframework/cloud")
                .groupId("org.springframework.cloud")
                .bomArtifact("spring-cloud-dependencies")
                .modulePrefix("spring-cloud-")
                .cacheDirName(".generated/spring-cloud-releases")
                .description("Migrate Spring Cloud properties found in configuration data files (YAML and Properties).")
                .productName("Spring Cloud")
                .tags("spring", "cloud")
                .defaultRecipePath(v -> Path.of("rewrite-spring/src/main/resources/META-INF/rewrite/cloud/cloud%s/spring-cloud-%s-%s-properties-generated.yml"
                        .formatted(v.major(), v.major(), v.minor())))
                .recipeName(v -> "io.arconia.rewrite.spring.cloud%s.UpgradeSpringCloudPropertiesGenerated_%s".formatted(v.major(), v.slug()))
                .displayName(v -> "Migrate Spring Cloud properties to %s (generated)".formatted(v.display()))
                .build());
    }

}
