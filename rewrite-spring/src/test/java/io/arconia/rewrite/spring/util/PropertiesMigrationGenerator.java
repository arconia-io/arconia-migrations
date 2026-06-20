package io.arconia.rewrite.spring.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;

/**
 * Generates properties migration recipes for a Spring ecosystem BOM by walking the BOM's
 * effective dependency management (following parent POMs and {@code <scope>import</scope>}
 * BOMs), downloading each matching module, and reading its
 * {@code spring-configuration-metadata.json} for deprecated and ignored properties.
 * <p>
 * Driven by per-ecosystem entry points that supply a {@link Spec}.
 * <p>
 * Inspired by <a href="https://github.com/openrewrite/rewrite-spring/blob/v5.25.1/src/test/java/org/openrewrite/java/spring/internal/GeneratePropertiesMigratorConfiguration.java">GeneratePropertiesMigratorConfiguration</a> (Apache-2.0)
 */
public final class PropertiesMigrationGenerator {

    private static final List<String> REPOSITORIES = List.of(
            "https://repo1.maven.org/maven2",
            "https://repo.spring.io/milestone",
            "https://repo.spring.io/snapshot"
    );

    private static final Pattern PROPERTY_REF = Pattern.compile("\\$\\{([^}]+)}");

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);

    /**
     * Cap on in-flight HTTP requests. Maven Central's HTTP/2 endpoint caps concurrent
     * streams per connection (~100), so an unbounded virtual-thread fan-out blows up
     * with {@code IOException: too many concurrent streams}. Sixteen is plenty of
     * parallelism for a one-shot CLI and stays well under any provider's limit.
     */
    private static final int DOWNLOAD_CONCURRENCY = 16;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(CONNECT_TIMEOUT)
            .build();

    private PropertiesMigrationGenerator() {}

    public record Spec(
            String groupPath,
            String groupId,
            String bomArtifact,
            String modulePrefix,
            String cacheDirName,
            String description,
            String productName,
            List<String> tags,
            Function<Version, Path> defaultRecipePath,
            Function<Version, String> recipeName,
            Function<Version, String> displayName
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private String groupPath;
            private String groupId;
            private String bomArtifact;
            private String modulePrefix;
            private String cacheDirName;
            private String description;
            private String productName;
            private List<String> tags = List.of();
            private Function<Version, Path> defaultRecipePath;
            private Function<Version, String> recipeName;
            private Function<Version, String> displayName;

            public Builder groupPath(String groupPath) { this.groupPath = groupPath; return this; }
            public Builder groupId(String groupId) { this.groupId = groupId; return this; }
            public Builder bomArtifact(String bomArtifact) { this.bomArtifact = bomArtifact; return this; }
            public Builder modulePrefix(String modulePrefix) { this.modulePrefix = modulePrefix; return this; }
            public Builder cacheDirName(String cacheDirName) { this.cacheDirName = cacheDirName; return this; }
            public Builder description(String description) { this.description = description; return this; }
            public Builder productName(String productName) { this.productName = productName; return this; }
            public Builder tags(String... tags) { this.tags = List.of(tags); return this; }
            public Builder defaultRecipePath(Function<Version, Path> defaultRecipePath) { this.defaultRecipePath = defaultRecipePath; return this; }
            public Builder recipeName(Function<Version, String> recipeName) { this.recipeName = recipeName; return this; }
            public Builder displayName(Function<Version, String> displayName) { this.displayName = displayName; return this; }

            public Spec build() {
                return new Spec(groupPath, groupId, bomArtifact, modulePrefix, cacheDirName, description, productName, tags,
                        defaultRecipePath, recipeName, displayName);
            }
        }
    }

    public record Version(String full, String major, String minor) {
        public static Version parse(String version) {
            String[] parts = version.split("[.\\-]");
            return new Version(version, parts[0], parts[1]);
        }

        public String slug() {
            return major + "_" + minor;
        }

        public String display() {
            return major + "." + minor;
        }
    }

    /** What the generator decided to do about a single property in a module. */
    sealed interface ModuleProperty permits ModuleProperty.Renamed, ModuleProperty.Commented {
        String name();
        record Renamed(String name, String replacement) implements ModuleProperty {}
        record Commented(String name, String comment) implements ModuleProperty {}
    }

    public static void run(String[] args, Class<?> entryPoint, Spec spec) throws IOException, InterruptedException {
        if (args.length < 1) {
            System.err.println("Usage: " + entryPoint.getSimpleName() + " <version> [recipe-path]");
            System.err.println("  e.g.: " + entryPoint.getSimpleName() + " 2.0.0");
            System.exit(1);
        }

        Version version = Version.parse(args[0]);
        Path recipePath = args.length > 1
                ? Path.of(args[1])
                : spec.defaultRecipePath().apply(version);

        Path cacheDir = Path.of(spec.cacheDirName(), version.full());
        Files.createDirectories(cacheDir);

        System.out.println("Resolving " + spec.bomArtifact() + " for " + version.full());
        Bom bom = resolveBom(spec.groupId(), spec.bomArtifact(), version.full(), spec.groupId(), cacheDir, new HashSet<>(), true);

        List<Module> modules = bom.modules().entrySet().stream()
                .filter(e -> e.getKey().startsWith(spec.modulePrefix()))
                .map(e -> new Module(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(Module::artifactId))
                .toList();

        System.out.println("Found " + modules.size() + " module(s) matching '" + spec.modulePrefix() + "*'");

        String productLabel = spec.productName() + " " + version.display();

        Map<String, List<ModuleProperty>> rawByModule = scanModulesInParallel(spec, modules, cacheDir, productLabel);

        Set<String> conflictingNames = findConflictingPropertyNames(rawByModule);
        if (!conflictingNames.isEmpty()) {
            System.out.println("Skipping " + conflictingNames.size() + " key(s) mapped to multiple targets across modules; handle these in the wrapper recipe:");
            conflictingNames.stream().sorted().forEach(n -> System.out.println("  - " + n));
        }

        Set<String> seenPropertyKeys = new HashSet<>();
        Map<String, List<ModuleProperty>> propertiesByModule = new LinkedHashMap<>();
        int total = 0;
        for (Map.Entry<String, List<ModuleProperty>> entry : rawByModule.entrySet()) {
            Set<String> redundantParents = findRedundantParentRenames(entry.getValue());
            List<ModuleProperty> unique = entry.getValue().stream()
                    .filter(p -> !conflictingNames.contains(p.name()))
                    .filter(p -> !redundantParents.contains(dedupKey(p)))
                    .filter(p -> seenPropertyKeys.add(dedupKey(p)))
                    .sorted(Comparator.comparing(ModuleProperty::name))
                    .toList();
            System.out.println("  " + entry.getKey() + ": " + entry.getValue().size() + " actionable");
            if (!unique.isEmpty()) {
                propertiesByModule.put(entry.getKey(), unique);
                total += unique.size();
            }
        }

        writeRecipeFile(recipePath, entryPoint, spec, version, propertiesByModule);
        System.out.println("Wrote " + total + " entries to " + recipePath);
    }

    private static Map<String, List<ModuleProperty>> scanModulesInParallel(
            Spec spec, List<Module> modules, Path cacheDir, String productLabel) throws IOException, InterruptedException {
        Map<Module, CompletableFuture<@Nullable List<ModuleProperty>>> futures = new LinkedHashMap<>();
        Semaphore inFlight = new Semaphore(DOWNLOAD_CONCURRENCY);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (Module module : modules) {
                futures.put(module, CompletableFuture.supplyAsync(() -> {
                    try {
                        inFlight.acquire();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new CompletionException(e);
                    }
                    try {
                        Path jar = downloadModuleJar(spec, module, cacheDir);
                        if (jar == null) {
                            return null;
                        }
                        return readModuleProperties(jar, productLabel);
                    } catch (IOException | InterruptedException e) {
                        throw new CompletionException(e);
                    } finally {
                        inFlight.release();
                    }
                }, executor));
            }
        }
        Map<String, List<ModuleProperty>> results = new LinkedHashMap<>();
        for (Map.Entry<Module, CompletableFuture<@Nullable List<ModuleProperty>>> entry : futures.entrySet()) {
            List<ModuleProperty> props;
            try {
                props = entry.getValue().join();
            } catch (CompletionException ce) {
                Throwable cause = ce.getCause();
                if (cause instanceof IOException io) throw io;
                if (cause instanceof InterruptedException ie) throw ie;
                if (cause instanceof RuntimeException re) throw re;
                throw new RuntimeException(cause);
            }
            if (props == null) {
                System.out.println("  skip " + entry.getKey().artifactId() + " (no JAR found)");
            } else {
                results.put(entry.getKey().artifactId(), props);
            }
        }
        return results;
    }

    // ---------- BOM resolution (with parent + <scope>import</scope> support) ----------

    /**
     * Resolve a BOM's effective module list by walking its parent chain and recursively
     * inlining {@code <scope>import</scope><type>pom</type>} BOM imports.
     * Only dependency entries whose groupId equals {@code filterGroupId} are collected.
     */
    private static Bom resolveBom(String groupId, String artifactId, String version,
                                  String filterGroupId, Path cacheDir, Set<String> visited, boolean required)
            throws IOException, InterruptedException {
        String coord = groupId + ":" + artifactId + ":" + version;
        if (!visited.add(coord)) {
            return new Bom(Map.of());
        }
        byte[] pomBytes = downloadPom(groupId, artifactId, version, cacheDir);
        if (pomBytes == null) {
            if (required) {
                throw new IOException("Could not find " + coord + " in any configured repo");
            }
            return new Bom(Map.of());
        }
        Element root = parseXml(pomBytes).getDocumentElement();

        Map<String, String> effectiveProps = collectEffectiveProperties(root, cacheDir, new HashSet<>(visited));
        String projectVersion = firstChildText(root, "version");
        if (projectVersion == null) {
            Element parent = firstChildElement(root, "parent");
            if (parent != null) {
                projectVersion = firstChildText(parent, "version");
            }
        }
        if (projectVersion != null) {
            effectiveProps.putIfAbsent("project.version", projectVersion);
        }

        Map<String, String> modules = new LinkedHashMap<>();
        for (Element dm : descendantElements(root, "dependencyManagement")) {
            for (Element deps : children(dm, "dependencies")) {
                for (Element dep : children(deps, "dependency")) {
                    String depGroupId = resolve(firstChildText(dep, "groupId"), effectiveProps);
                    String depArtifactId = resolve(firstChildText(dep, "artifactId"), effectiveProps);
                    String depVersion = resolve(firstChildText(dep, "version"), effectiveProps);
                    String depScope = firstChildText(dep, "scope");
                    String depType = firstChildText(dep, "type");

                    if ("import".equals(depScope) && "pom".equals(depType)) {
                        if (depGroupId != null && depArtifactId != null && depVersion != null) {
                            Bom imported = resolveBom(depGroupId, depArtifactId, depVersion, filterGroupId, cacheDir, visited, false);
                            // Imported BOM entries are lower priority than entries declared
                            // higher in the chain (Maven semantics).
                            imported.modules().forEach(modules::putIfAbsent);
                        }
                    } else if (filterGroupId.equals(depGroupId) && depArtifactId != null && depVersion != null) {
                        modules.put(depArtifactId, depVersion);
                    }
                }
            }
        }

        Element parentEl = firstChildElement(root, "parent");
        if (parentEl != null) {
            String pGroup = firstChildText(parentEl, "groupId");
            String pArtifact = firstChildText(parentEl, "artifactId");
            String pVersion = firstChildText(parentEl, "version");
            if (pGroup != null && pArtifact != null && pVersion != null) {
                Bom parentBom = resolveBom(pGroup, pArtifact, pVersion, filterGroupId, cacheDir, visited, false);
                parentBom.modules().forEach(modules::putIfAbsent);
            }
        }

        return new Bom(modules);
    }

    private static Map<String, String> collectEffectiveProperties(Element root, Path cacheDir, Set<String> visited)
            throws IOException, InterruptedException {
        // Parent's properties first (lower priority), then this POM's (overrides).
        Map<String, String> effective = new HashMap<>();
        Element parentEl = firstChildElement(root, "parent");
        if (parentEl != null) {
            String pGroup = firstChildText(parentEl, "groupId");
            String pArtifact = firstChildText(parentEl, "artifactId");
            String pVersion = firstChildText(parentEl, "version");
            if (pGroup != null && pArtifact != null && pVersion != null
                    && visited.add(pGroup + ":" + pArtifact + ":" + pVersion)) {
                byte[] parentPom = downloadPom(pGroup, pArtifact, pVersion, cacheDir);
                if (parentPom != null) {
                    Element parentRoot = parseXml(parentPom).getDocumentElement();
                    effective.putAll(collectEffectiveProperties(parentRoot, cacheDir, visited));
                    String parentVersion = firstChildText(parentRoot, "version");
                    if (parentVersion != null) {
                        effective.put("project.version", parentVersion);
                    }
                }
            }
        }
        effective.putAll(readPomProperties(root));
        return effective;
    }

    private static byte @Nullable [] downloadPom(String groupId, String artifactId, String version, Path cacheDir)
            throws IOException, InterruptedException {
        String groupPath = groupId.replace('.', '/');
        String pomName = artifactId + "-" + version + ".pom";
        return downloadFromAnyRepo(
                "/" + groupPath + "/" + artifactId + "/" + version + "/" + pomName,
                cacheDir.resolve(pomName));
    }

    private static Document parseXml(byte[] bytes) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse POM XML", e);
        }
    }

    private static Map<String, String> readPomProperties(Element root) {
        Map<String, String> props = new HashMap<>();
        for (Element propsEl : children(root, "properties")) {
            for (Node n = propsEl.getFirstChild(); n != null; n = n.getNextSibling()) {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    props.put(n.getNodeName(), n.getTextContent().trim());
                }
            }
        }
        return props;
    }

    private static @Nullable String resolve(@Nullable String value, Map<String, String> properties) {
        if (value == null) {
            return null;
        }
        Matcher m = PROPERTY_REF.matcher(value);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String resolved = properties.getOrDefault(m.group(1), m.group(0));
            m.appendReplacement(sb, Matcher.quoteReplacement(resolved));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    // ---------- Module download & scan ----------

    private static @Nullable Path downloadModuleJar(Spec spec, Module module, Path cacheDir) throws IOException, InterruptedException {
        String jarName = module.artifactId() + "-" + module.version() + ".jar";
        Path target = cacheDir.resolve(jarName);
        if (Files.exists(target)) {
            return target;
        }
        byte[] bytes = downloadFromAnyRepo(
                "/" + spec.groupPath() + "/" + module.artifactId() + "/" + module.version() + "/" + jarName,
                target);
        return bytes == null ? null : target;
    }

    private static List<ModuleProperty> readModuleProperties(Path jar, String productLabel) throws IOException {
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            JarEntry entry = jarFile.getJarEntry("META-INF/spring-configuration-metadata.json");
            if (entry == null) {
                return List.of();
            }
            try (InputStream is = jarFile.getInputStream(entry)) {
                SpringConfigurationMetadata metadata = objectMapper.readValue(is, SpringConfigurationMetadata.class);
                List<ModuleProperty> result = new ArrayList<>();
                if (metadata.properties() != null) {
                    for (ConfigurationProperty p : metadata.properties()) {
                        Deprecation d = p.deprecation();
                        if (d == null) {
                            continue;
                        }
                        if (d.replacement() != null) {
                            result.add(new ModuleProperty.Renamed(p.name(), d.replacement()));
                        } else {
                            String reason = d.reason();
                            String comment = "Deprecated property removed in " + productLabel + "."
                                    + (reason != null && !reason.isBlank() ? " " + reason.trim() : "");
                            result.add(new ModuleProperty.Commented(p.name(), comment));
                        }
                    }
                }
                if (metadata.ignored() != null && metadata.ignored().properties() != null) {
                    for (IgnoredProperty ip : metadata.ignored().properties()) {
                        result.add(new ModuleProperty.Commented(
                                ip.name(),
                                "Unbindable property in " + productLabel + ". The framework does not bind it."));
                    }
                }
                return result;
            }
        }
    }

    // ---------- HTTP helpers ----------

    private static byte @Nullable [] downloadFromAnyRepo(String relativePath, Path cachePath) throws IOException, InterruptedException {
        if (Files.exists(cachePath)) {
            return Files.readAllBytes(cachePath);
        }
        for (String repo : REPOSITORIES) {
            byte[] body = httpGet(repo + relativePath);
            if (body != null) {
                Files.write(cachePath, body);
                return body;
            }
        }
        return null;
    }

    private static byte @Nullable [] httpGet(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() == 200) {
            return response.body();
        }
        if (response.statusCode() == 404) {
            return null;
        }
        throw new IOException("Unexpected response " + response.statusCode() + " from " + url);
    }

    // ---------- Recipe writing ----------

    private static void writeRecipeFile(Path recipePath, Class<?> entryPoint, Spec spec, Version version,
                                        Map<String, List<ModuleProperty>> propertiesByModule) throws IOException {
        Files.createDirectories(recipePath.getParent());
        String tagsBlock = spec.tags().stream().map(t -> "  - " + t).collect(joining("\n"));
        StringBuilder out = new StringBuilder("""
                # This file is generated by %s.
                # Do not edit manually. Update the upstream configuration metadata.
                ---
                type: specs.openrewrite.org/v1beta/recipe
                name: %s
                displayName: %s
                description: %s
                tags:
                %s
                recipeList:
                """.formatted(
                entryPoint.getSimpleName(),
                spec.recipeName().apply(version),
                spec.displayName().apply(version),
                spec.description(),
                tagsBlock));

        propertiesByModule.forEach((moduleName, properties) -> {
            String entries = properties.stream()
                    .map(PropertiesMigrationGenerator::renderEntry)
                    .collect(joining());
            if (!entries.isEmpty()) {
                out.append("  # ").append(moduleName).append('\n').append(entries);
            }
        });

        Files.writeString(recipePath, out.toString());
    }

    private static String renderEntry(ModuleProperty p) {
        return switch (p) {
            case ModuleProperty.Renamed r -> """
                      - io.arconia.rewrite.spring.boot.properties.RenameSpringBootPropertyKey:
                          oldPropertyKey: "%s"
                          newPropertyKey: "%s"
                    """.formatted(r.name(), r.replacement());
            case ModuleProperty.Commented c -> """
                      - io.arconia.rewrite.spring.boot.properties.CommentSpringBootProperty:
                          propertyKey: "%s"
                          comment: "%s"
                    """.formatted(c.name(), yamlEscape(c.comment()));
        };
    }

    /**
     * Identify property names where the same old key is mapped to more than one distinct
     * replacement across modules (or to a mix of rename and comment-out). The upstream
     * configuration-metadata occasionally registers the same key against multiple modules
     * with diverging targets, which would silently let the first match win at recipe time
     * regardless of the user's actual runtime. These keys are dropped from the generated
     * file and must be handled in the wrapper YAML (e.g., behind a `FindDependency`
     * precondition that picks the right target).
     */
    private static Set<String> findConflictingPropertyNames(Map<String, List<ModuleProperty>> rawByModule) {
        Map<String, Set<String>> targetsByName = new LinkedHashMap<>();
        for (List<ModuleProperty> moduleProperties : rawByModule.values()) {
            for (ModuleProperty p : moduleProperties) {
                targetsByName.computeIfAbsent(p.name(), k -> new HashSet<>()).add(targetOf(p));
            }
        }
        Set<String> conflicts = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : targetsByName.entrySet()) {
            if (entry.getValue().size() > 1) {
                conflicts.add(entry.getKey());
            }
        }
        return conflicts;
    }

    private static String targetOf(ModuleProperty p) {
        return switch (p) {
            case ModuleProperty.Renamed r -> "rename:" + r.replacement();
            case ModuleProperty.Commented c -> "comment:" + c.comment();
        };
    }

    /**
     * Identify renames whose old key is a strict prefix of another rename in the same module
     * AND whose new key carries the same suffix shape. These parent renames are redundant —
     * the leaf renames already cover every documented sub-key, and keeping the parent would
     * sweep undocumented sub-keys that happen to share the namespace.
     */
    private static Set<String> findRedundantParentRenames(List<ModuleProperty> properties) {
        List<ModuleProperty.Renamed> renames = properties.stream()
                .filter(ModuleProperty.Renamed.class::isInstance)
                .map(ModuleProperty.Renamed.class::cast)
                .toList();
        Set<String> redundant = new HashSet<>();
        for (ModuleProperty.Renamed parent : renames) {
            String oldPrefix = parent.name() + ".";
            String newPrefix = parent.replacement() + ".";
            for (ModuleProperty.Renamed child : renames) {
                if (child == parent) continue;
                if (child.name().startsWith(oldPrefix)
                        && child.replacement().startsWith(newPrefix)
                        && child.name().substring(oldPrefix.length())
                            .equals(child.replacement().substring(newPrefix.length()))) {
                    redundant.add(dedupKey(parent));
                    break;
                }
            }
        }
        return redundant;
    }

    private static String dedupKey(ModuleProperty p) {
        return switch (p) {
            case ModuleProperty.Renamed r -> r.name() + "->" + r.replacement();
            case ModuleProperty.Commented c -> "comment:" + c.name();
        };
    }

    private static String yamlEscape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // ---------- XML helpers ----------

    private static List<Element> children(Element parent, String name) {
        List<Element> out = new ArrayList<>();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(name)) {
                out.add((Element) n);
            }
        }
        return out;
    }

    private static @Nullable Element firstChildElement(Element parent, String name) {
        List<Element> matches = children(parent, name);
        return matches.isEmpty() ? null : matches.get(0);
    }

    private static List<Element> descendantElements(Element root, String name) {
        List<Element> out = new ArrayList<>();
        NodeList nodes = root.getElementsByTagName(name);
        for (int i = 0; i < nodes.getLength(); i++) {
            out.add((Element) nodes.item(i));
        }
        return out;
    }

    private static @Nullable String firstChildText(Element parent, String name) {
        List<Element> matches = children(parent, name);
        return matches.isEmpty() ? null : matches.get(0).getTextContent().trim();
    }

    // ---------- Data records ----------

    record Module(String artifactId, String version) {}

    record Bom(Map<String, String> modules) {}

    record SpringConfigurationMetadata(@Nullable List<ConfigurationProperty> properties,
                                       @Nullable Ignored ignored) {}

    record Ignored(@Nullable List<IgnoredProperty> properties) {}

    record IgnoredProperty(String name) {}

    record ConfigurationProperty(String name, @Nullable Deprecation deprecation) {}

    record Deprecation(@Nullable String replacement, @Nullable String reason, @Nullable String level) {}
}
