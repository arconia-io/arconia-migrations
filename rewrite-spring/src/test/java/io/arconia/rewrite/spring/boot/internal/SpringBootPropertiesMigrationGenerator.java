package io.arconia.rewrite.spring.boot.internal;

import java.io.IOException;
import java.nio.file.Path;

import io.arconia.rewrite.spring.internal.PropertiesMigrationGenerator;
import io.arconia.rewrite.spring.internal.PropertiesMigrationGenerator.Spec;

/**
 * Generates Spring Boot properties migration recipes from {@code spring-boot-dependencies}.
 * <p>
 * Usage: {@code java GenerateSpringBootPropertiesMigratorConfiguration 4.1.0}
 * <p>
 * The default output path matches the existing hand-curated layout
 * ({@code rewrite/boot/boot{major}/spring-boot-{major}-{minor}-properties.yml}); pass an
 * explicit recipe-path argument to avoid overwriting manually-maintained content.</p>
 */
public class SpringBootPropertiesMigrationGenerator {

    public static void main(String[] args) throws IOException, InterruptedException {
        PropertiesMigrationGenerator.run(args, SpringBootPropertiesMigrationGenerator.class, Spec.builder()
                .groupPath("org/springframework/boot")
                .groupId("org.springframework.boot")
                .bomArtifact("spring-boot-dependencies")
                .modulePrefix("spring-boot-")
                .cacheDirName(".generated/spring-boot-releases")
                .description("Migrate Spring Boot properties found in configuration data files (YAML and Properties).")
                .productName("Spring Boot")
                .tags("spring", "boot")
                .defaultRecipePath(v -> Path.of("rewrite-spring/src/main/resources/META-INF/rewrite/boot/boot%s/spring-boot-%s-%s-properties-generated.yml"
                        .formatted(v.major(), v.major(), v.minor())))
                .recipeName(v -> "io.arconia.rewrite.spring.boot%s.UpgradeSpringBootPropertiesGenerated_%s".formatted(v.major(), v.slug()))
                .displayName(v -> "Migrate Spring Boot properties to %s (generated)".formatted(v.display()))
                .build());
    }

}
