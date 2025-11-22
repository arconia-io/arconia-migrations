package io.arconia.rewrite.framework.docling;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

/**
 * Replaces {@code addHttpSources(url)} with {@code source(HttpSource.builder().url(URI.create(url)).build())},
 * and {@code addHttpSources(uri)} with {@code source(HttpSource.builder().url(uri).build())},
 * and {@code addFileSources(filename, base64String)} with {@code source(FileSource.builder().filename(filename).base64String(base64String).build())}.
 */
public class UseConvertDocumentRequestSourcesMethod extends Recipe {

    private static final String FQN_FILE_SOURCE = "ai.docling.api.serve.convert.request.source.FileSource";
    private static final String FQN_HTTP_SOURCE = "ai.docling.api.serve.convert.request.source.HttpSource";
    private static final String FQN_URI = "java.net.URI";

    private static final MethodMatcher ADD_HTTP_SOURCES_STRING_MATCHER
            = new MethodMatcher("ai.docling.api.serve.convert.request.ConvertDocumentRequest$Builder addHttpSources(java.lang.String...)");

    private static final MethodMatcher ADD_HTTP_SOURCES_URI_MATCHER
            = new MethodMatcher("ai.docling.api.serve.convert.request.ConvertDocumentRequest$Builder addHttpSources(java.net.URI...)");

    private static final MethodMatcher ADD_FILE_SOURCES_MATCHER
            = new MethodMatcher("ai.docling.api.serve.convert.request.ConvertDocumentRequest$Builder addFileSources(java.lang.String, java.lang.String)");

    @Override
    public String getDisplayName() {
        return "Use `source()` methods instead of `addHttpSources()` or `addFileSources()`";
    }

    @Override
    public String getDescription() {
        return "Replace `.addHttpSources()` or `addFileSources()` with `.source()`, " +
                "wrapping arguments appropriately with `HttpSource.builder()` or `FileSource.builder()`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                Preconditions.or(
                        new UsesMethod<>(ADD_HTTP_SOURCES_STRING_MATCHER),
                        new UsesMethod<>(ADD_HTTP_SOURCES_URI_MATCHER),
                        new UsesMethod<>(ADD_FILE_SOURCES_MATCHER)
                ),
                new ConvertDocumentRequestVisitor()
        );
    }

    private static class ConvertDocumentRequestVisitor extends JavaVisitor<ExecutionContext> {

        @Override
        public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
            J.MethodInvocation mi = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);

            if (ADD_HTTP_SOURCES_STRING_MATCHER.matches(mi)) {
                if (mi.getArguments().size() != 1) {
                    // Only support migrating single-argument `addHttpSources` calls
                    return mi;
                }

                Expression arg = mi.getArguments().getFirst();

                maybeAddImport(FQN_HTTP_SOURCE);
                maybeAddImport(FQN_URI);

                return JavaTemplate.builder("source(HttpSource.builder().url(URI.create(#{any(String)})).build())")
                        .imports(FQN_HTTP_SOURCE, FQN_URI)
                        .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "docling-serve-api"))
                        .build()
                        .apply(getCursor(), mi.getCoordinates().replaceMethod(), arg);
            } else if (ADD_HTTP_SOURCES_URI_MATCHER.matches(mi)) {
                Expression arg = mi.getArguments().getFirst();

                if (mi.getArguments().size() != 1) {
                    // Only support migrating single-argument `addHttpSources` calls
                    return mi;
                }

                maybeAddImport(FQN_HTTP_SOURCE);

                return JavaTemplate.builder("source(HttpSource.builder().url(#{any(java.net.URI)}).build())")
                        .imports(FQN_HTTP_SOURCE)
                        .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "docling-serve-api"))
                        .build()
                        .apply(getCursor(), mi.getCoordinates().replaceMethod(), arg);
            } else if (ADD_FILE_SOURCES_MATCHER.matches(mi)) {
                Expression filename = mi.getArguments().get(0);
                Expression base64String = mi.getArguments().get(1);

                maybeAddImport(FQN_FILE_SOURCE);

                return JavaTemplate.builder("source(FileSource.builder().filename(#{any(String)}).base64String(#{any(String)}).build())")
                        .imports(FQN_FILE_SOURCE)
                        .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "docling-serve-api"))
                        .build()
                        .apply(getCursor(), mi.getCoordinates().replaceMethod(), filename, base64String);
            }

            return mi;
        }

    }

}
