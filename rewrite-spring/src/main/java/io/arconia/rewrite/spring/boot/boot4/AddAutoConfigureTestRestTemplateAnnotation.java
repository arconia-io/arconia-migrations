package io.arconia.rewrite.spring.boot.boot4;

import java.util.Comparator;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

/**
 * In Spring Boot 3, many projects using Spring Web MVC rely on TestRestTemplate
 * in @SpringBootTest or @WebMvcTest integration tests, which it's automatically configured
 * when Spring Web is on the classpath. In Spring Boot 4, this is no longer
 * the case, so we need to add the @AutoConfigureTestRestTemplate annotation
 * explicitly. This recipe should run after the recipes addressing package and
 * dependency changes for Spring Boot 4.
 */
public class AddAutoConfigureTestRestTemplateAnnotation extends Recipe {

    private static final String AUTO_CONFIGURE_TEST_REST_TEMPLATE = "AutoConfigureTestRestTemplate";
    private static final String FQN_AUTO_CONFIGURE_TEST_REST_TEMPLATE = "org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate";
    private static final String FQN_SPRING_BOOT_TEST = "org.springframework.boot.test.context.SpringBootTest";
    private static final String FQN_WEB_MVC_TEST = "org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest";
    private static final String FQN_TEST_REST_TEMPLATE = "org.springframework.boot.resttestclient.TestRestTemplate";

    private static final AnnotationMatcher AUTO_CONFIGURE_TEST_REST_TEMPLATE_ANNOTATION_MATCHER = new AnnotationMatcher("@" + FQN_AUTO_CONFIGURE_TEST_REST_TEMPLATE, true);
    private static final AnnotationMatcher SPRING_BOOT_TEST_ANNOTATION_MATCHER = new AnnotationMatcher("@" + FQN_SPRING_BOOT_TEST, true);
    private static final AnnotationMatcher WEB_MVC_TEST_ANNOTATION_MATCHER = new AnnotationMatcher("@" + FQN_WEB_MVC_TEST, true);

    @Override
    public String getDisplayName() {
        return "Add missing `@AutoConfigureTestRestTemplate` annotation";
    }

    @Override
    public String getDescription() {
        return "Add missing `@AutoConfigureTestRestTemplate` annotation to classes annotated with " +
                "`@SpringBootTest` or `@WebMvcTest` that use `TestRestTemplate`.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new UsesType<>(FQN_TEST_REST_TEMPLATE, false),
                new SpringBootTestVisitor()
        );
    }

    private static class SpringBootTestVisitor extends JavaIsoVisitor<ExecutionContext> {

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
            J.ClassDeclaration c = super.visitClassDeclaration(classDecl, ctx);
            if (isApplicableClass(c)) {
                c = addMissingAnnotation(c, ctx);
            }
            return c;
        }

        private J.ClassDeclaration addMissingAnnotation(J.ClassDeclaration c, ExecutionContext ctx) {
            maybeAddImport(FQN_AUTO_CONFIGURE_TEST_REST_TEMPLATE);
            return JavaTemplate.builder("@" + AUTO_CONFIGURE_TEST_REST_TEMPLATE)
                    .imports(FQN_AUTO_CONFIGURE_TEST_REST_TEMPLATE)
                    .javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "spring-boot-resttestclient-4.0.*"))
                    .build()
                    .apply(
                            getCursor(),
                            c.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName))
                    );
        }

        private boolean isApplicableClass(J.ClassDeclaration classDeclaration) {
            // Check if the class is already annotated with @AutoConfigureTestRestTemplate
            for (J.Annotation annotation : classDeclaration.getLeadingAnnotations()) {
                JavaType.FullyQualified annotationType = TypeUtils.asFullyQualified(annotation.getType());
                if (annotationType != null && AUTO_CONFIGURE_TEST_REST_TEMPLATE_ANNOTATION_MATCHER.matchesAnnotationOrMetaAnnotation(annotationType)) {
                    // Already annotated with @AutoConfigureTestRestTemplate. No changes are needed.
                    return false;
                }
            }

            // Check if the class is annotated with @SpringBootTest
            for (J.Annotation annotation : classDeclaration.getLeadingAnnotations()) {
                JavaType.FullyQualified aType = TypeUtils.asFullyQualified(annotation.getType());
                if (aType != null) {
                    if (SPRING_BOOT_TEST_ANNOTATION_MATCHER.matchesAnnotationOrMetaAnnotation(aType)) {
                        // Annotated with @SpringBootTest, so we need to add @AutoConfigureTestRestTemplate.
                        return true;
                    } else if (WEB_MVC_TEST_ANNOTATION_MATCHER.matchesAnnotationOrMetaAnnotation(aType)) {
                        // Annotated with @WebMvcTest, so we need to add @AutoConfigureTestRestTemplate.
                        return true;
                    }
                }
            }

            // Not annotated with @SpringBootTest or @WebMvcTest. No changes are needed.
            return false;
        }
    }

}
