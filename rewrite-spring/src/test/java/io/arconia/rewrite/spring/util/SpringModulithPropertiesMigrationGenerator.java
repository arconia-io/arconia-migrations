package io.arconia.rewrite.spring.util;

import java.io.IOException;
import java.nio.file.Path;

import io.arconia.rewrite.spring.util.PropertiesMigrationGenerator.Spec;

/**
 * Generates Spring Modulith properties migration recipes from the {@code spring-modulith-bom}.
 * <p>
 * Usage: {@code java SpringModulithPropertiesMigrationGenerator 2.2.0}
 */
public class SpringModulithPropertiesMigrationGenerator {

    public static void main(String[] args) throws IOException, InterruptedException {
        PropertiesMigrationGenerator.run(args, SpringModulithPropertiesMigrationGenerator.class, Spec.builder()
                .groupPath("org/springframework/modulith")
                .groupId("org.springframework.modulith")
                .bomArtifact("spring-modulith-bom")
                .modulePrefix("spring-modulith-")
                .cacheDirName(".generated/spring-modulith-releases")
                .description(v -> "Migrate Spring Modulith configuration properties that were renamed or deprecated in the %s release, across YAML and Properties configuration files.".formatted(v.display()))
                .productName("Spring Modulith")
                .tags("spring", "modulith")
                .defaultRecipePath(v -> Path.of("rewrite-spring/src/main/resources/META-INF/rewrite/spring/modulith%s/spring-modulith-%s-%s-properties-generated.yml"
                        .formatted(v.major(), v.major(), v.minor())))
                .recipeName(v -> "io.arconia.rewrite.spring.modulith%s.UpgradeSpringModulithPropertiesGenerated_%s".formatted(v.major(), v.slug()))
                .displayName(v -> "Migrate Spring Modulith properties to %s (generated)".formatted(v.display()))
                .build());
    }

}
