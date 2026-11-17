package io.arconia.rewrite.framework.arconia0;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeArconia_0_33_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.framework.UpgradeArconia_0_33")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "arconia-ai-docling-document-reader-0.32"));
    }

    @Test
    @DocumentExample
    void doclingDocumentMetadataRenamed() {
        rewriteRun(
                //language=java
                java(
                        """
                        package com.yourorg;

                        import io.arconia.ai.document.docling.DocumentMetadata;

                        class DocumentKeys {

                            String fileNameKey() {
                                return DocumentMetadata.FILE_NAME.value();
                            }

                        }
                        """,
                        """
                        package com.yourorg;

                        import io.arconia.ai.document.docling.DoclingDocumentMetadata;

                        class DocumentKeys {

                            String fileNameKey() {
                                return DoclingDocumentMetadata.FILE_NAME.value();
                            }

                        }
                        """)
        );
    }

}
