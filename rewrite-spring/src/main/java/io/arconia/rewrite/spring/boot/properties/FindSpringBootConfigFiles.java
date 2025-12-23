package io.arconia.rewrite.spring.boot.properties;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.FindSourceFiles;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;

/**
 * Finds Spring Boot configuration files based on the provided path matchers.
 * <p>
 * This recipe is used to identify configuration files that may need to be modified or processed in some way by other recipes.
 *
 * @link <a href="https://docs.spring.io/spring-boot/reference/features/external-config.html">Spring Boot Externalized Configuration</a>
 */
public class FindSpringBootConfigFiles extends Recipe {

    private static final List<String> DEFAULT_PATH_EXPRESSIONS = List.of(
            "**/application.yml",
            "**/application.yaml",
            "**/application.properties",
            "**/application-*.yml",
            "**/application-*.yaml",
            "**/application-*.properties"
    );

    @Option(displayName = "Configuration files path matchers",
            description = """
                    A list of glob expressions used to match which files contain Spring Boot configuration properties
                    and therefore will be modified. If no value is provided, default expressions will be used to match
                    the main YAML and Properties files supported by Spring Boot for configuration.
                    If multiple patterns are supplied, any of the patterns matching will be interpreted as a match.
                    """,
            required = false)
    @Nullable
    private final List<String> pathExpressions;

    @JsonCreator
    public FindSpringBootConfigFiles(@Nullable List<String> pathExpressions) {
        this.pathExpressions = pathExpressions;
    }

    @Override
    public String getDisplayName() {
        return "Find Spring Boot configuration files";
    }

    @Override
    public String getDescription() {
        return "Finds Spring Boot configuration files based on the provided path matchers.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        String filePattern = pathExpressions != null && !pathExpressions.isEmpty() ?
                String.join(";", pathExpressions) :
                String.join(";", DEFAULT_PATH_EXPRESSIONS);
        return new FindSourceFiles(filePattern).getVisitor();
    }

}
