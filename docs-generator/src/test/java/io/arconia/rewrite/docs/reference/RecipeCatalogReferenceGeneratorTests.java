package io.arconia.rewrite.docs.reference;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the pure, derivation helpers of {@link RecipeCatalogReferenceGenerator}.
 */
class RecipeCatalogReferenceGeneratorTests {

    @Nested
    class ModuleFromSource {

        @Test
        void takesModuleNameFromAVersionedJarWithoutSwallowingTheVersion() {
            String source = "jar:file:/home/user/.gradle/caches/rewrite-spring-0.17.0.jar!/META-INF/rewrite/spring.yml";
            assertThat(RecipeCatalogReferenceGenerator.moduleFromSource(source)).isEqualTo("rewrite-spring");
        }

        @Test
        void takesModuleNameFromABuildOutputDirectory() {
            String source = "file:/repo/rewrite-arconia/build/resources/main/META-INF/rewrite/arconia.yml";
            assertThat(RecipeCatalogReferenceGenerator.moduleFromSource(source)).isEqualTo("rewrite-arconia");
        }

        @Test
        void supportsMultiWordModuleNames() {
            assertThat(RecipeCatalogReferenceGenerator.moduleFromSource("file:/x/rewrite-java-dependencies-1.2.3.jar!/y"))
                    .isEqualTo("rewrite-java-dependencies");
        }

        @Test
        void fallsBackToUnattributedWhenNoModuleSegmentIsPresent() {
            assertThat(RecipeCatalogReferenceGenerator.moduleFromSource("file:/somewhere/unknown.yml"))
                    .isEqualTo("unattributed");
            assertThat(RecipeCatalogReferenceGenerator.moduleFromSource("")).isEqualTo("unattributed");
        }
    }

    @Nested
    class FamilyFromName {

        @Test
        void usesThePackageSegmentsBetweenThePrefixAndTheSimpleName() {
            assertThat(RecipeCatalogReferenceGenerator.familyFromName(
                    "io.arconia.rewrite.spring.boot4.UpgradeSpringBoot_4_0")).isEqualTo("spring.boot4");
            assertThat(RecipeCatalogReferenceGenerator.familyFromName(
                    "io.arconia.rewrite.framework.opentelemetry.MigrateOtlp")).isEqualTo("framework.opentelemetry");
        }

        @Test
        void fallsBackToGeneralForARootLevelRecipe() {
            assertThat(RecipeCatalogReferenceGenerator.familyFromName("io.arconia.rewrite.MigrateSomething"))
                    .isEqualTo("general");
        }
    }

    @Nested
    class FamilyPresentation {

        @Test
        void anchorReplacesDotsWithHyphensAndPrefixesFamily() {
            assertThat(RecipeCatalogReferenceGenerator.familyAnchor("spring.boot4")).isEqualTo("family-spring-boot4");
        }

        @Test
        void titleRendersDotsAsBreadcrumbs() {
            assertThat(RecipeCatalogReferenceGenerator.familyTitle("spring.ai2.mcp")).isEqualTo("spring › ai2 › mcp");
        }

        @Test
        void anchorTurnsRecipeIdIntoAValidAnchor() {
            assertThat(RecipeCatalogReferenceGenerator.anchor("io.arconia.rewrite.spring.boot4.UpgradeSpringBoot_4_0"))
                    .isEqualTo("io_arconia_rewrite_spring_boot4_UpgradeSpringBoot_4_0");
        }
    }

    @Nested
    class TextHelpers {

        @Test
        void cellEscapesTableSeparators() {
            assertThat(RecipeCatalogReferenceGenerator.cell("a | b | c")).isEqualTo("a \\| b \\| c");
        }

        @Test
        void oneLineCollapsesAllWhitespace() {
            assertThat(RecipeCatalogReferenceGenerator.oneLine("first line\n   second\tthird  ")).isEqualTo("first line second third");
        }
    }

    @Nested
    class NavRegion {

        private static final String BLOCK = """
                // tag::recipe-catalog[]
                * xref:recipe-catalog/index.adoc[Recipe Catalog]
                // end::recipe-catalog[]""";

        @Test
        void replacesContentBetweenExistingMarkers() {
            String existing = """
                    * xref:index.adoc[Introduction]
                    // tag::recipe-catalog[]
                    * stale content
                    // end::recipe-catalog[]
                    """;

            String result = RecipeCatalogReferenceGenerator.replaceNavRegion(existing, BLOCK);

            assertThat(result)
                    .contains("* xref:index.adoc[Introduction]")
                    .contains("* xref:recipe-catalog/index.adoc[Recipe Catalog]")
                    .doesNotContain("stale content")
                    .containsOnlyOnce(RecipeCatalogReferenceGeneratorTests.navStart());
        }

        @Test
        void appendsTheBlockWhenMarkersAreAbsent() {
            String existing = "* xref:index.adoc[Introduction]\n";

            String result = RecipeCatalogReferenceGenerator.replaceNavRegion(existing, BLOCK);

            assertThat(result)
                    .startsWith("* xref:index.adoc[Introduction]")
                    .endsWith(BLOCK + "\n")
                    .containsOnlyOnce(RecipeCatalogReferenceGeneratorTests.navStart());
        }
    }

    private static String navStart() {
        return "// tag::recipe-catalog[]";
    }
}
