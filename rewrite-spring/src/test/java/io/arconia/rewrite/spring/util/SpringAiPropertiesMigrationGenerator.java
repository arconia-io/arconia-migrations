package io.arconia.rewrite.spring.util;

import java.io.IOException;
import java.nio.file.Path;

import io.arconia.rewrite.spring.util.PropertiesMigrationGenerator.Spec;

/**
 * Generates Spring AI properties migration recipes from the {@code spring-ai-bom}.
 * <p>
 * Usage: {@code java GenerateSpringAiPropertiesMigratorConfiguration 2.0.0}
 */
public class SpringAiPropertiesMigrationGenerator {

    public static void main(String[] args) throws IOException, InterruptedException {
        PropertiesMigrationGenerator.run(args, SpringAiPropertiesMigrationGenerator.class, Spec.builder()
                .groupPath("org/springframework/ai")
                .groupId("org.springframework.ai")
                .bomArtifact("spring-ai-bom")
                .modulePrefix("spring-ai-autoconfigure-")
                .cacheDirName(".generated/spring-ai-releases")
                .description("Migrate Spring AI properties found in configuration data files (YAML and Properties).")
                .productName("Spring AI")
                .tags("spring", "ai")
                .defaultRecipePath(v -> Path.of("rewrite-spring/src/main/resources/META-INF/rewrite/spring/ai%s/spring-ai-%s-%s-properties-generated.yml"
                        .formatted(v.major(), v.major(), v.minor())))
                .recipeName(v -> "io.arconia.rewrite.spring.ai%s.UpgradeSpringAiPropertiesGenerated_%s".formatted(v.major(), v.slug()))
                .displayName(v -> "Migrate Spring AI properties to %s (generated)".formatted(v.display()))
                .build());
    }

}
