package io.arconia.rewrite.framework.bootstrap;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.PathUtils;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.marker.Markers;
import org.openrewrite.marker.SearchResult;
import org.openrewrite.properties.PropertiesParser;
import org.openrewrite.properties.tree.Properties;
import org.openrewrite.yaml.YamlIsoVisitor;
import org.openrewrite.yaml.tree.Yaml;

/**
 * Moves the Arconia bootstrap properties from the Spring application configuration files
 * ({@code application.properties}, {@code application.yml}, {@code application.yaml}) to
 * {@code META-INF/arconia-bootstrap.properties}, where Arconia resolves them before the
 * application configuration is loaded.
 *
 * <p>Properties the new file cannot express are left in place and marked for manual review:
 * values containing property placeholders, properties in profile-specific files or in
 * profile-activated YAML documents, and properties with conflicting values across the
 * configuration files of the same source set.
 */
public class MigrateArconiaBootstrapProperties extends ScanningRecipe<MigrateArconiaBootstrapProperties.Accumulator> {

    private static final List<String> BOOTSTRAP_PROPERTIES = List.of(
            "arconia.bootstrap.profiles.enabled",
            "arconia.dev.profiles",
            "arconia.test.profiles");

    private static final String ON_PROFILE_KEY = "spring.config.activate.on-profile";

    private static final Pattern APPLICATION_CONFIG_PATH =
            Pattern.compile("((?:.*/)?src/(?:main|test)/resources)/application\\.(?:properties|ya?ml)");

    private static final Pattern PROFILE_SPECIFIC_CONFIG_PATH =
            Pattern.compile("((?:.*/)?src/(?:main|test)/resources)/application-[^/]+\\.(?:properties|ya?ml)");

    private static final Pattern BOOTSTRAP_FILE_PATH =
            Pattern.compile("((?:.*/)?src/(?:main|test)/resources)/META-INF/arconia-bootstrap\\.properties");

    private static final String PLACEHOLDER_MESSAGE =
            "Move this property to META-INF/arconia-bootstrap.properties manually. The file does not resolve property placeholders.";

    private static final String PROFILE_MESSAGE =
            "Move this property to META-INF/arconia-bootstrap.properties manually. The file does not support profile-specific configuration.";

    private static final String CONFLICT_MESSAGE =
            "Move this property to META-INF/arconia-bootstrap.properties manually. Conflicting values were found in this source set.";

    @Override
    public String getDisplayName() {
        return "Migrate the Arconia bootstrap properties to `META-INF/arconia-bootstrap.properties`";
    }

    @Override
    public String getDescription() {
        return "The Arconia bootstrap properties are resolved before the application configuration is loaded, " +
                "so they are no longer supported in the Spring application configuration files. This recipe moves them to " +
                "`META-INF/arconia-bootstrap.properties`, creating or updating the file in the same source set and " +
                "keeping the values already present there. Properties the new file cannot express (values with property " +
                "placeholders, profile-specific configuration, conflicting values within a source set) are left in place " +
                "and marked for manual review.";
    }

    @Override
    public Accumulator getInitialValue(ExecutionContext ctx) {
        return new Accumulator();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(Accumulator acc) {
        return new TreeVisitor<>() {
            @Override
            public @Nullable Tree visit(@Nullable Tree tree, ExecutionContext ctx) {
                if (!(tree instanceof SourceFile sourceFile)) {
                    return tree;
                }
                String path = PathUtils.separatorsToUnix(sourceFile.getSourcePath().toString());
                Matcher bootstrapFile = BOOTSTRAP_FILE_PATH.matcher(path);
                if (bootstrapFile.matches()) {
                    acc.sourceSetsWithBootstrapFile.add(bootstrapFile.group(1));
                    return tree;
                }
                Matcher applicationConfig = APPLICATION_CONFIG_PATH.matcher(path);
                if (!applicationConfig.matches()) {
                    return tree;
                }
                String sourceSet = applicationConfig.group(1);
                if (sourceFile instanceof Properties.File propertiesFile) {
                    for (Properties.Content content : propertiesFile.getContent()) {
                        if (content instanceof Properties.Entry entry && BOOTSTRAP_PROPERTIES.contains(entry.getKey())) {
                            recordEligibleValue(acc, sourceSet, entry.getKey(), entry.getValue().getText());
                        }
                    }
                }
                else if (sourceFile instanceof Yaml.Documents documents) {
                    for (Yaml.Document document : documents.getDocuments()) {
                        if (!isProfileActivated(document)) {
                            findBootstrapProperties(document).forEach((key, value) -> recordEligibleValue(acc, sourceSet, key, value));
                        }
                    }
                }
                return tree;
            }
        };
    }

    @Override
    public Collection<? extends SourceFile> generate(Accumulator acc, ExecutionContext ctx) {
        List<SourceFile> generated = new ArrayList<>();
        for (String sourceSet : new TreeSet<>(acc.eligibleValues.keySet())) {
            if (acc.sourceSetsWithBootstrapFile.contains(sourceSet)) {
                continue;
            }
            Map<String, String> migratable = migratableProperties(acc, sourceSet);
            if (migratable.isEmpty()) {
                continue;
            }
            // Prevents the file from being generated again in later cycles.
            acc.sourceSetsWithBootstrapFile.add(sourceSet);
            String contents = migratable.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("\n", "", "\n"));
            PropertiesParser.builder().build().parse(contents)
                    .map(file -> (SourceFile) file.withSourcePath(Paths.get(sourceSet, "META-INF", "arconia-bootstrap.properties")))
                    .forEach(generated::add);
        }
        return generated;
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor(Accumulator acc) {
        return new TreeVisitor<>() {
            @Override
            public @Nullable Tree visit(@Nullable Tree tree, ExecutionContext ctx) {
                if (!(tree instanceof SourceFile sourceFile)) {
                    return tree;
                }
                String path = PathUtils.separatorsToUnix(sourceFile.getSourcePath().toString());
                Matcher bootstrapFile = BOOTSTRAP_FILE_PATH.matcher(path);
                if (bootstrapFile.matches() && tree instanceof Properties.File file) {
                    return mergeIntoBootstrapFile(file, migratableProperties(acc, bootstrapFile.group(1)));
                }
                Matcher applicationConfig = APPLICATION_CONFIG_PATH.matcher(path);
                Matcher profileSpecificConfig = PROFILE_SPECIFIC_CONFIG_PATH.matcher(path);
                boolean application = applicationConfig.matches();
                boolean profileSpecific = !application && profileSpecificConfig.matches();
                if (!application && !profileSpecific) {
                    return tree;
                }
                String sourceSet = profileSpecific ? profileSpecificConfig.group(1) : applicationConfig.group(1);
                if (tree instanceof Properties.File file) {
                    return editProperties(file, acc, sourceSet, profileSpecific);
                }
                if (tree instanceof Yaml.Documents documents) {
                    return editYaml(documents, acc, sourceSet, profileSpecific, ctx);
                }
                return tree;
            }
        };
    }

    private static void recordEligibleValue(Accumulator acc, String sourceSet, String key, String value) {
        if (containsPlaceholder(value)) {
            return;
        }
        acc.eligibleValues
                .computeIfAbsent(sourceSet, set -> new HashMap<>())
                .computeIfAbsent(key, k -> new HashSet<>())
                .add(value);
    }

    private static Set<String> eligibleValues(Accumulator acc, String sourceSet, String key) {
        return acc.eligibleValues.getOrDefault(sourceSet, Map.of()).getOrDefault(key, Set.of());
    }

    /**
     * The properties that can be migrated automatically for the given source set: those with a single
     * eligible value, in the declaration order of {@link #BOOTSTRAP_PROPERTIES}.
     */
    private static Map<String, String> migratableProperties(Accumulator acc, String sourceSet) {
        Map<String, String> migratable = new LinkedHashMap<>();
        for (String key : BOOTSTRAP_PROPERTIES) {
            Set<String> values = eligibleValues(acc, sourceSet, key);
            if (values.size() == 1) {
                migratable.put(key, values.iterator().next());
            }
        }
        return migratable;
    }

    private static Properties.File mergeIntoBootstrapFile(Properties.File file, Map<String, String> migratable) {
        if (migratable.isEmpty()) {
            return file;
        }
        Set<String> existingKeys = new HashSet<>();
        for (Properties.Content content : file.getContent()) {
            if (content instanceof Properties.Entry entry) {
                existingKeys.add(entry.getKey());
            }
        }
        List<Properties.Content> additions = new ArrayList<>();
        migratable.forEach((key, value) -> {
            if (!existingKeys.contains(key)) {
                additions.add(new Properties.Entry(Tree.randomId(), "\n", Markers.EMPTY, key, "",
                        Properties.Entry.Delimiter.EQUALS, new Properties.Value(Tree.randomId(), "", Markers.EMPTY, value)));
            }
        });
        if (additions.isEmpty()) {
            return file;
        }
        List<Properties.Content> newContent = ListUtils.concatAll(file.getContent(), additions);
        if (newContent == null) {
            return file;
        }
        if (file.getContent().isEmpty()) {
            newContent = ListUtils.mapFirst(newContent, content -> (Properties.Content) content.withPrefix(""));
        }
        return newContent == null ? file : file.withContent(newContent);
    }

    private static Properties.File editProperties(Properties.File file, Accumulator acc, String sourceSet, boolean profileSpecific) {
        List<Properties.Content> before = file.getContent();
        boolean[] firstRemoved = new boolean[1];
        List<Properties.Content> after = ListUtils.map(before, (index, content) -> {
            if (!(content instanceof Properties.Entry entry) || !BOOTSTRAP_PROPERTIES.contains(entry.getKey())) {
                return content;
            }
            Properties.@Nullable Content result;
            String value = entry.getValue().getText();
            if (profileSpecific) {
                result = mark(entry, PROFILE_MESSAGE);
            }
            else if (containsPlaceholder(value)) {
                result = mark(entry, PLACEHOLDER_MESSAGE);
            }
            else {
                Set<String> values = eligibleValues(acc, sourceSet, entry.getKey());
                if (values.size() > 1) {
                    result = mark(entry, CONFLICT_MESSAGE);
                }
                else if (values.size() == 1) {
                    // Migrated to META-INF/arconia-bootstrap.properties.
                    result = null;
                }
                else {
                    result = content;
                }
            }
            if (index == 0 && result == null) {
                firstRemoved[0] = true;
            }
            return result;
        });
        if (firstRemoved[0]) {
            after = ListUtils.mapFirst(after, content -> (Properties.Content) content.withPrefix(""));
        }
        return after == null || after == before ? file : file.withContent(after);
    }

    private static Yaml.Documents editYaml(Yaml.Documents documents, Accumulator acc, String sourceSet,
                                           boolean profileSpecific, ExecutionContext ctx) {
        return (Yaml.Documents) new YamlIsoVisitor<ExecutionContext>() {
            @Override
            public Yaml.Document visitDocument(Yaml.Document document, ExecutionContext ctx) {
                Map<String, String> found = findBootstrapProperties(document);
                if (found.isEmpty()) {
                    return document;
                }
                boolean profileActivated = isProfileActivated(document);
                Map<String, String> messagesByKey = new LinkedHashMap<>();
                List<String> keysToDelete = new ArrayList<>();
                found.forEach((key, value) -> {
                    if (profileSpecific || profileActivated) {
                        messagesByKey.put(key, PROFILE_MESSAGE);
                    }
                    else if (containsPlaceholder(value)) {
                        messagesByKey.put(key, PLACEHOLDER_MESSAGE);
                    }
                    else if (eligibleValues(acc, sourceSet, key).size() > 1) {
                        messagesByKey.put(key, CONFLICT_MESSAGE);
                    }
                    else if (eligibleValues(acc, sourceSet, key).size() == 1) {
                        // Migrated to META-INF/arconia-bootstrap.properties.
                        keysToDelete.add(key);
                    }
                });
                Yaml.Document d = document;
                if (!messagesByKey.isEmpty()) {
                    d = (Yaml.Document) new MarkYamlProperties(messagesByKey).visitNonNull(d, ctx, getCursor().getParentTreeCursor());
                }
                // Scoped to the document so occurrences in other, profile-activated documents are preserved.
                for (String key : keysToDelete) {
                    d = (Yaml.Document) new org.openrewrite.yaml.DeleteProperty(key, null, false, null)
                            .getVisitor().visitNonNull(d, ctx, getCursor().getParentTreeCursor());
                }
                return d;
            }
        }.visitNonNull(documents, ctx);
    }

    private static boolean isProfileActivated(Yaml.Document document) {
        Map<String, String> found = new LinkedHashMap<>();
        collectProperties(document.getBlock(), "", List.of(ON_PROFILE_KEY), found);
        return !found.isEmpty();
    }

    private static Map<String, String> findBootstrapProperties(Yaml.Document document) {
        Map<String, String> found = new LinkedHashMap<>();
        collectProperties(document.getBlock(), "", BOOTSTRAP_PROPERTIES, found);
        return found;
    }

    /**
     * Collects the values of the given properties within a YAML block, interpreting nested mappings
     * as dot-separated property names the way Spring Boot does, and joining sequence values with commas.
     */
    private static void collectProperties(Yaml.Block block, String prefix, Collection<String> keys, Map<String, String> found) {
        if (!(block instanceof Yaml.Mapping mapping)) {
            return;
        }
        for (Yaml.Mapping.Entry entry : mapping.getEntries()) {
            String key = prefix.isEmpty() ? entry.getKey().getValue() : prefix + "." + entry.getKey().getValue();
            if (keys.contains(key)) {
                String value = scalarValue(entry.getValue());
                if (value != null) {
                    found.put(key, value);
                }
            }
            else {
                collectProperties(entry.getValue(), key, keys, found);
            }
        }
    }

    private static @Nullable String scalarValue(Yaml.Block block) {
        if (block instanceof Yaml.Scalar scalar) {
            return scalar.getValue();
        }
        if (block instanceof Yaml.Sequence sequence) {
            List<String> items = new ArrayList<>();
            for (Yaml.Sequence.Entry entry : sequence.getEntries()) {
                if (!(entry.getBlock() instanceof Yaml.Scalar scalar)) {
                    return null;
                }
                items.add(scalar.getValue());
            }
            return String.join(",", items);
        }
        return null;
    }

    private static boolean containsPlaceholder(String value) {
        return value.contains("${");
    }

    // Equality of SearchResult markers is based on the description, so this is idempotent across cycles.
    private static <T extends Tree> T mark(T tree, String message) {
        return tree.withMarkers(tree.getMarkers().addIfAbsent(new SearchResult(Tree.randomId(), message)));
    }

    /**
     * Marks the YAML mapping entries whose dot-separated property name has a message,
     * flagging them for manual review.
     */
    private static class MarkYamlProperties extends YamlIsoVisitor<ExecutionContext> {

        private final Map<String, String> messagesByKey;

        MarkYamlProperties(Map<String, String> messagesByKey) {
            this.messagesByKey = messagesByKey;
        }

        @Override
        public Yaml.Mapping.Entry visitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext ctx) {
            Yaml.Mapping.Entry e = super.visitMappingEntry(entry, ctx);
            List<String> segments = new ArrayList<>();
            getCursor().getPathAsStream()
                    .filter(Yaml.Mapping.Entry.class::isInstance)
                    .map(Yaml.Mapping.Entry.class::cast)
                    .forEach(pathEntry -> segments.add(0, pathEntry.getKey().getValue()));
            String message = messagesByKey.get(String.join(".", segments));
            if (message != null) {
                e = mark(e, message);
            }
            return e;
        }
    }

    public static class Accumulator {

        // Source set resources root (e.g. "src/main/resources") -> property key -> distinct values eligible for migration.
        final Map<String, Map<String, Set<String>>> eligibleValues = new HashMap<>();

        final Set<String> sourceSetsWithBootstrapFile = new HashSet<>();
    }

}
