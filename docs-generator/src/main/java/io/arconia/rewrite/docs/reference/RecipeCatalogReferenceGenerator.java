package io.arconia.rewrite.docs.reference;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrewrite.config.Environment;
import org.openrewrite.config.OptionDescriptor;
import org.openrewrite.config.RecipeDescriptor;

/**
 * Generate Antora reference pages from the OpenRewrite {@link RecipeDescriptor} model.
 * <p>
 * Recipes are grouped by a category/family derived from the recipe id, and within each family
 * the composite recipes (those that compose others) are listed before the leaf recipes.
 * Chained version recipes are each listed individually; the chain is shown only inside
 * each recipe's composition tree. Grouping, composite/leaf, and deprecation are all derived.
 * No hand-maintained lists.
 * <p>
 * Run with {@code -Dreference.check=true} to verify the committed pages instead of writing them.
 */
public final class RecipeCatalogReferenceGenerator {

    private static final String RECIPE_PREFIX = "io.arconia.rewrite.";
    private static final Pattern MODULE = Pattern.compile("(rewrite-[a-z]+(?:-[a-z]+)*)");
    private static final String NAV = "docs/modules/ROOT/nav.adoc";
    private static final String NAV_START = "// tag::recipe-catalog[]";
    private static final String NAV_END = "// end::recipe-catalog[]";
    // The Antora page subdirectory / xref prefix, and the human-readable section name.
    private static final String CATALOG_DIR = "recipe-catalog";
    private static final String CATALOG_TITLE = "Recipe Catalog";
    private static final int MAX_DEPTH = 3;
    private static final int MAX_SIBLINGS = 20;

    // Order recipes by display name (so version siblings read 4.0 before 4.1, matching
    // docs.openrewrite.org), with the unique id as a stable tie-breaker.
    private static final Comparator<RecipeDescriptor> BY_DISPLAY_NAME =
            Comparator.comparing(RecipeDescriptor::getDisplayName)
                    .thenComparing(RecipeDescriptor::getName);

    // A family section's anchor id and human-readable title.
    record FamilyRef(String anchor, String title) {
    }

    // A rendered module page and the families it contains (for the navigation).
    record ModulePage(String content, List<FamilyRef> families) {
    }

    // Recipe id -> module page that hosts its anchor (rendered, non-deprecated recipes).
    private final Map<String, String> anchorIndex;

    // Recipe id -> canonical before/after example extracted from its @DocumentExample test.
    private final Map<String, RecipeExampleExtractor.Example> examples;

    RecipeCatalogReferenceGenerator(Map<String, String> anchorIndex,
                             Map<String, RecipeExampleExtractor.Example> examples) {
        this.anchorIndex = anchorIndex;
        this.examples = examples;
    }

    public static void main(String[] args) throws IOException {
        boolean check = Boolean.getBoolean("reference.check");
        Path repoRoot = Path.of(System.getProperty("user.dir"));
        Path pagesDir = repoRoot.resolve("docs/modules/ROOT/pages/" + CATALOG_DIR);
        Path navPath = repoRoot.resolve(NAV);

        Environment env = Environment.builder().scanRuntimeClasspath().build();
        Map<String, List<RecipeDescriptor>> byModule = new TreeMap<>();
        Map<String, String> anchorIndex = new HashMap<>();
        for (RecipeDescriptor r : env.listRecipeDescriptors()) {
            if (r.getName().startsWith(RECIPE_PREFIX)) {
                String module = moduleOf(r);
                byModule.computeIfAbsent(module, k -> new ArrayList<>()).add(r);
                if (!isDeprecated(r)) {
                    anchorIndex.put(r.getName(), module);
                }
            }
        }

        // Extract @DocumentExample examples from every recipe module's test sources.
        List<Path> testRoots = byModule.keySet().stream()
                .map(module -> repoRoot.resolve(module + "/src/test/java"))
                .toList();
        Map<String, RecipeExampleExtractor.Example> examples = RecipeExampleExtractor.extract(repoRoot, testRoots);
        RecipeCatalogReferenceGenerator generator = new RecipeCatalogReferenceGenerator(anchorIndex, examples);

        // Compute every output's intended content up front, then either write it all
        // (generate) or diff against what is committed (check). Output is deterministic
        // (sorted/TreeMap, total-order comparators), so the diff is stable across runs.
        Map<Path, String> outputs = new LinkedHashMap<>();
        Map<String, List<FamilyRef>> nav = new LinkedHashMap<>();
        for (Map.Entry<String, List<RecipeDescriptor>> e : byModule.entrySet()) {
            ModulePage page = generator.renderModule(e.getKey(), e.getValue());
            outputs.put(pagesDir.resolve(e.getKey() + ".adoc"), page.content());
            nav.put(e.getKey(), page.families());
        }
        outputs.put(pagesDir.resolve("index.adoc"), renderIndex(byModule));
        outputs.put(navPath, computeNav(navPath, nav));

        apply(outputs, pagesDir, check);
    }

    private static void apply(Map<Path, String> outputs, Path pagesDir, boolean check) throws IOException {
        // Reference pages that are committed but no longer produced (e.g. a removed module).
        List<Path> orphans = new ArrayList<>();
        if (Files.isDirectory(pagesDir)) {
            try (var stream = Files.newDirectoryStream(pagesDir, "*.adoc")) {
                for (Path p : stream) {
                    if (!outputs.containsKey(p)) {
                        orphans.add(p);
                    }
                }
            }
        }

        List<Path> stale = new ArrayList<>();
        for (Map.Entry<Path, String> e : outputs.entrySet()) {
            String current = Files.exists(e.getKey()) ? Files.readString(e.getKey()) : null;
            if (!e.getValue().equals(current)) {
                stale.add(e.getKey());
            }
        }

        if (check) {
            if (stale.isEmpty() && orphans.isEmpty()) {
                System.out.println("Recipe catalog docs are up to date (" + outputs.size() + " files).");
                return;
            }
            stale.forEach(p -> System.err.println("  out of date: " + p));
            orphans.forEach(p -> System.err.println("  orphaned:    " + p));
            System.err.println("Recipe catalog docs are stale. "
                    + "Run: ./gradlew :docs-generator:generateRecipeCatalogReference");
            System.exit(1);
        }

        Files.createDirectories(pagesDir);
        for (Map.Entry<Path, String> e : outputs.entrySet()) {
            Files.writeString(e.getKey(), e.getValue());
        }
        for (Path orphan : orphans) {
            Files.delete(orphan);
        }
        System.out.println("Generated " + outputs.size() + " recipe catalog files"
                + (orphans.isEmpty() ? "." : ", removed " + orphans.size() + " orphaned."));
    }

    // ---- Grouping helpers ---------------------------------------------------

    private static String moduleOf(RecipeDescriptor r) {
        return moduleFromSource(r.getSource().toString());
    }

    // Derive the owning Gradle module from a recipe source URI (jar name or build dir).
    static String moduleFromSource(String source) {
        Matcher m = MODULE.matcher(source);
        String found = null;
        while (m.find()) {
            found = m.group(1); // last match wins (the jar/dir segment, not a transitive path)
        }
        return found != null ? found : "unattributed";
    }

    private static String familyOf(RecipeDescriptor r) {
        return familyFromName(r.getName());
    }

    // Derive the category/family slug (the recipe id minus its prefix and simple name).
    static String familyFromName(String recipeName) {
        String rel = recipeName.substring(RECIPE_PREFIX.length());
        int lastDot = rel.lastIndexOf('.');
        return lastDot < 0 ? "general" : rel.substring(0, lastDot);
    }

    private static boolean isComposite(RecipeDescriptor r) {
        return !r.getRecipeList().isEmpty();
    }

    private static boolean isDeprecated(RecipeDescriptor r) {
        return r.getDescription().startsWith("Deprecated");
    }

    static String familyTitle(String slug) {
        return slug.replace(".", " › ");
    }

    static String familyAnchor(String slug) {
        return "family-" + slug.replace('.', '-');
    }

    // ---- Page rendering -----------------------------------------------------

    private ModulePage renderModule(String module, List<RecipeDescriptor> recipes) {
        Map<String, List<RecipeDescriptor>> families = new TreeMap<>();
        List<RecipeDescriptor> deprecated = new ArrayList<>();
        for (RecipeDescriptor r : recipes) {
            if (isDeprecated(r)) {
                deprecated.add(r);
            } else {
                families.computeIfAbsent(familyOf(r), k -> new ArrayList<>()).add(r);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("= ").append(module).append('\n');
        sb.append(":description: OpenRewrite recipes published in the ").append(module).append(" module.\n\n");
        sb.append("// AUTO-GENERATED from the OpenRewrite recipe model. Do not edit by hand.\n\n");
        sb.append("The `").append(module).append("` module publishes ").append(recipes.size())
                .append(recipes.size() == 1 ? " recipe across " : " recipes across ").append(families.size())
                .append(families.size() == 1 ? " category.\n\n" : " categories.\n\n");

        List<FamilyRef> navFamilies = new ArrayList<>();
        for (Map.Entry<String, List<RecipeDescriptor>> e : families.entrySet()) {
            String slug = e.getKey();
            navFamilies.add(new FamilyRef(familyAnchor(slug), familyTitle(slug)));

            List<RecipeDescriptor> composite = new ArrayList<>();
            List<RecipeDescriptor> leaf = new ArrayList<>();
            for (RecipeDescriptor r : e.getValue()) {
                (isComposite(r) ? composite : leaf).add(r);
            }
            // Composite recipes still render before the leaf recipes (the grouping below);
            // within each group, order by display name like docs.openrewrite.org.
            composite.sort(BY_DISPLAY_NAME);
            leaf.sort(BY_DISPLAY_NAME);

            sb.append("[#").append(familyAnchor(slug)).append("]\n");
            sb.append("== ").append(familyTitle(slug)).append("\n\n");
            if (!composite.isEmpty()) {
                sb.append("*Composite recipes*\n\n");
                composite.forEach(r -> renderRecipe(sb, r));
            }
            if (!leaf.isEmpty()) {
                sb.append("*Recipes*\n\n");
                leaf.forEach(r -> renderRecipe(sb, r));
            }
        }

        if (!deprecated.isEmpty()) {
            deprecated.sort(Comparator.comparing(RecipeDescriptor::getName));
            sb.append("== Deprecated recipes\n\n");
            sb.append("These identifiers remain as aliases that delegate to their replacement ")
                    .append("and will be removed in a future release.\n\n");
            for (RecipeDescriptor r : deprecated) {
                sb.append("* `").append(r.getName()).append("` — ")
                        .append(oneLine(r.getDescription())).append('\n');
            }
            sb.append('\n');
        }

        return new ModulePage(sb.toString(), navFamilies);
    }

    private void renderRecipe(StringBuilder sb, RecipeDescriptor r) {
        sb.append("[#").append(anchor(r.getName())).append("]\n");
        sb.append("=== ").append(r.getDisplayName()).append('\n');
        sb.append("`").append(r.getName()).append("`\n\n");
        if (!r.getDescription().isBlank()) {
            sb.append(r.getDescription().strip()).append("\n\n");
        }
        if (!r.getTags().isEmpty()) {
            sb.append("_Tags: ").append(String.join(", ", r.getTags())).append("_\n\n");
        }

        List<OptionDescriptor> options = r.getOptions();
        if (!options.isEmpty()) {
            sb.append(".Options\n");
            sb.append("[cols=\"2,1,1,4\",options=\"header\"]\n|===\n");
            sb.append("| Name | Type | Required | Description\n\n");
            for (OptionDescriptor o : options) {
                sb.append("| `").append(o.getName()).append("`\n");
                sb.append("| ").append(cell(o.getType())).append('\n');
                sb.append("| ").append(o.isRequired() ? "yes" : "no").append('\n');
                sb.append("| ").append(cell(o.getDescription()));
                String example = o.getExample();
                if (example != null && !example.isBlank()) {
                    sb.append(" +\n_Example:_ `").append(cell(example)).append('`');
                }
                sb.append('\n');
            }
            sb.append("|===\n\n");
        }

        if (isComposite(r)) {
            sb.append(".Recipes applied\n");
            sb.append("[%collapsible]\n====\n");
            renderTree(sb, r.getRecipeList(), 1);
            sb.append("====\n\n");
        }

        RecipeExampleExtractor.Example example = examples.get(r.getName());
        if (example != null) {
            renderExample(sb, example);
        }
    }

    private void renderExample(StringBuilder sb, RecipeExampleExtractor.Example example) {
        sb.append("*Example*\n\n");
        for (RecipeExampleExtractor.Source s : example.sources()) {
            sb.append(".Before\n[source,").append(s.language()).append("]\n----\n")
                    .append(s.before().stripTrailing()).append("\n----\n\n");
            sb.append(".After\n[source,").append(s.language()).append("]\n----\n")
                    .append(s.after().stripTrailing()).append("\n----\n\n");
        }
    }

    private void renderTree(StringBuilder sb, List<RecipeDescriptor> children, int depth) {
        String bullet = "*".repeat(depth);
        int shown = 0;
        int i = 0;
        int n = children.size();
        while (i < n) {
            RecipeDescriptor c = children.get(i);
            // Collapse a run of consecutive children that share the same recipe id
            // (e.g. one RenameSpringBootPropertyKey per migrated property).
            int run = 1;
            while (i + run < n && sameName(children.get(i + run), c)) {
                run++;
            }
            if (shown >= MAX_SIBLINGS) {
                sb.append(bullet).append(" … and ").append(n - i).append(" more\n");
                break;
            }
            sb.append(bullet).append(' ').append(linked(c));
            sb.append(run > 1 ? " (`" + c.getName() + "` ×" + run + ")" : " (`" + c.getName() + "`)");
            sb.append('\n');
            if (run == 1 && depth < MAX_DEPTH && !c.getRecipeList().isEmpty()) {
                renderTree(sb, c.getRecipeList(), depth + 1);
            }
            shown++;
            i += run;
        }
    }

    // Render a child's display name, linking to its own anchor when it is a rendered Arconia recipe.
    private String linked(RecipeDescriptor r) {
        String module = anchorIndex.get(r.getName());
        String text = r.getDisplayName().replace("]", "\\]");
        if (module == null) {
            return text;
        }
        return "xref:" + CATALOG_DIR + "/" + module + ".adoc#" + anchor(r.getName()) + "[" + text + "]";
    }

    private static boolean sameName(RecipeDescriptor a, RecipeDescriptor b) {
        return a.getName().equals(b.getName());
    }

    private static String renderIndex(Map<String, List<RecipeDescriptor>> byModule) {
        StringBuilder sb = new StringBuilder();
        sb.append("= ").append(CATALOG_TITLE).append('\n');
        sb.append(":description: Every Arconia Migrations recipe, grouped by module.\n\n");
        sb.append("This catalog lists every OpenRewrite recipe published by Arconia Migrations, ")
                .append("grouped by the module that ships it and, within each module, by category.\n\n");
        sb.append("For curated, step-by-step upgrade guides, see the topic sections in the navigation.\n\n");
        byModule.forEach((module, recipes) ->
                sb.append("* xref:").append(CATALOG_DIR).append('/').append(module).append(".adoc[")
                        .append(module).append("] — ").append(recipes.size()).append(" recipes\n"));
        return sb.toString();
    }

    // ---- Nav ----------------------------------------------------------------

    private static String computeNav(Path navPath, Map<String, List<FamilyRef>> nav) {
        StringBuilder block = new StringBuilder();
        block.append(NAV_START).append('\n');
        block.append("* xref:").append(CATALOG_DIR).append("/index.adoc[").append(CATALOG_TITLE).append("]\n");
        nav.forEach((module, families) -> {
            block.append("** xref:").append(CATALOG_DIR).append('/').append(module)
                    .append(".adoc[").append(module).append("]\n");
            for (FamilyRef f : families) {
                block.append("*** xref:").append(CATALOG_DIR).append('/').append(module).append(".adoc#")
                        .append(f.anchor()).append('[').append(f.title()).append("]\n");
            }
        });
        block.append(NAV_END);

        try {
            return replaceNavRegion(Files.readString(navPath), block.toString());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    // Replace the reference region between the markers, or append it if the markers are absent.
    static String replaceNavRegion(String existing, String block) {
        int start = existing.indexOf(NAV_START);
        int end = existing.indexOf(NAV_END);
        if (start >= 0 && end > start) {
            return existing.substring(0, start) + block + existing.substring(end + NAV_END.length());
        }
        return existing.strip() + "\n" + block + "\n";
    }

    // ---- Low-level ----------------------------------------------------------

    static String anchor(String name) {
        return name.replace('.', '_');
    }

    static String oneLine(String text) {
        return text.replaceAll("\\s+", " ").strip();
    }

    // Escape AsciiDoc table cell separators.
    static String cell(String text) {
        return text.replace("|", "\\|");
    }

}
