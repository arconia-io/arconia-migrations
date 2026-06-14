package io.arconia.rewrite.test.testcontainers;

import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeUtils;

/**
 * Rewrites bare-tag string arguments passed to the legacy
 * {@code org.testcontainers.containers.localstack.LocalStackContainer} constructor into full
 * image-name strings. The 1.x constructor treated a single {@code String} as a tag against
 * {@code localstack/localstack}; the 2.x constructor expects a full image name. Without this
 * rewrite, {@code new LocalStackContainer("0.11.2")} compiles in both versions but in 2.x
 * fails at runtime trying to pull an image literally named {@code 0.11.2}.
 */
public class MigrateLocalStackImageArg extends Recipe {

    private static final String LEGACY_TYPE = "org.testcontainers.containers.localstack.LocalStackContainer";
    private static final String DEFAULT_IMAGE_NAME = "localstack/localstack";

    @Override
    public String getDisplayName() {
        return "Migrate LocalStackContainer bare-version constructor argument";
    }

    @Override
    public String getDescription() {
        return "Rewrites `new LocalStackContainer(\"<tag>\")` calls — where the string is a bare tag (no `/`) — " +
                "into `new LocalStackContainer(\"localstack/localstack:<tag>\")`. The 1.x constructor treated a " +
                "single string as a tag; the 2.x constructor expects a full image name.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesType<>(LEGACY_TYPE, false), new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.NewClass visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                J.NewClass nc = super.visitNewClass(newClass, ctx);
                if (!TypeUtils.isOfClassType(nc.getType(), LEGACY_TYPE)) {
                    return nc;
                }
                List<Expression> args = nc.getArguments();
                if (args == null || args.size() != 1 || !(args.get(0) instanceof J.Literal first)) {
                    return nc;
                }
                if (!(first.getValue() instanceof String s) || s.isEmpty() || s.contains("/")) {
                    return nc;
                }
                String newValue = DEFAULT_IMAGE_NAME + ":" + s;
                return nc.withArguments(List.of(first.withValue(newValue).withValueSource("\"" + newValue + "\"")));
            }
        });
    }

}
