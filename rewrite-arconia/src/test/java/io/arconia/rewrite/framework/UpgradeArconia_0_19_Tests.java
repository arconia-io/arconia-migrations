package io.arconia.rewrite.framework;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

class UpgradeArconia_0_19_Tests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.arconia.rewrite.UpgradeArconia_0_19")
                .parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(),
                        "arconia-docling-0.18.*"));
    }

    @Test
    void clientChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.docling.client.DoclingClient;

                        class Demo {
                            void call(DoclingClient doclingClient) {
                                doclingClient.health();
                                doclingClient.convertSource(null);
                            }
                        }
                        """,
                        """
                        import ai.docling.api.serve.DoclingServeApi;

                        class Demo {
                            void call(DoclingServeApi doclingClient) {
                                doclingClient.health();
                                doclingClient.convertSource(null);
                            }
                        }
                        """
                )
        );
    }

    @Test
    void convertDocumentRequestHttpChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.docling.client.convert.request.ConvertDocumentRequest;
                        import io.arconia.docling.client.convert.request.source.HttpSource;

                        import java.net.URI;
                        import java.util.List;

                        class Demo {
                            void call() {
                                ConvertDocumentRequest convertDocumentRequest = ConvertDocumentRequest.builder()
                                    .httpSources(List.of(HttpSource.builder().url(URI.create("https://example.net")).build()))
                                    .options(null)
                                    .target(null)
                                    .build();

                                convertDocumentRequest.options();
                                convertDocumentRequest.sources();
                                convertDocumentRequest.target();
                            }

                            void call2() {
                                ConvertDocumentRequest convertDocumentRequest = ConvertDocumentRequest.builder()
                                    .addHttpSources("https://example.net")
                                    .build();
                            }

                            void call3() {
                                ConvertDocumentRequest convertDocumentRequest = ConvertDocumentRequest.builder()
                                    .addHttpSources(URI.create("https://example.net"))
                                    .build();
                            }
                        }
                        """,
                        """
                        import ai.docling.api.serve.convert.request.ConvertDocumentRequest;
                        import ai.docling.api.serve.convert.request.source.HttpSource;

                        import java.net.URI;
                        import java.util.List;

                        class Demo {
                            void call() {
                                ConvertDocumentRequest convertDocumentRequest = ConvertDocumentRequest.builder()
                                    .sources(List.of(HttpSource.builder().url(URI.create("https://example.net")).build()))
                                    .options(null)
                                    .target(null)
                                    .build();

                                convertDocumentRequest.getOptions();
                                convertDocumentRequest.getSources();
                                convertDocumentRequest.getTarget();
                            }

                            void call2() {
                                ConvertDocumentRequest convertDocumentRequest = ConvertDocumentRequest.builder()
                                    .source(HttpSource.builder().url(URI.create("https://example.net")).build())
                                    .build();
                            }

                            void call3() {
                                ConvertDocumentRequest convertDocumentRequest = ConvertDocumentRequest.builder()
                                    .source(HttpSource.builder().url(URI.create("https://example.net")).build())
                                    .build();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void convertDocumentRequestFileChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.docling.client.convert.request.ConvertDocumentRequest;
                        import io.arconia.docling.client.convert.request.source.FileSource;

                        import java.util.List;

                        class Demo {
                            void call() {
                                ConvertDocumentRequest convertDocumentRequest = ConvertDocumentRequest.builder()
                                    .fileSources(List.of(FileSource.builder().filename("beep").base64String("boop").build()))
                                    .build();
                            }

                            void call2() {
                                ConvertDocumentRequest convertDocumentRequest = ConvertDocumentRequest.builder()
                                    .addFileSources("beep", "boop")
                                    .build();
                            }
                        }
                        """,
                        """
                        import ai.docling.api.serve.convert.request.ConvertDocumentRequest;
                        import ai.docling.api.serve.convert.request.source.FileSource;

                        import java.util.List;

                        class Demo {
                            void call() {
                                ConvertDocumentRequest convertDocumentRequest = ConvertDocumentRequest.builder()
                                    .sources(List.of(FileSource.builder().filename("beep").base64String("boop").build()))
                                    .build();
                            }

                            void call2() {
                                ConvertDocumentRequest convertDocumentRequest = ConvertDocumentRequest.builder()
                                    .source(FileSource.builder().filename("beep").base64String("boop").build())
                                    .build();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void convertDocumentOptionsChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.docling.client.convert.request.options.ConvertDocumentOptions;

                        class Demo {
                            void call() {
                                ConvertDocumentOptions options = ConvertDocumentOptions.builder().build();

                                options.fromFormats();
                                options.toFormats();
                                options.imageExportMode();
                                options.doOcr();
                                options.forceOcr();
                                options.ocrEngine();
                                options.ocrLang();
                                options.pdfBackend();
                                options.tableMode();
                                options.tableCellMatching();
                                options.pipeline();
                                options.pageRange();
                                options.documentTimeout();
                                options.abortOnError();
                                options.doTableStructure();
                                options.includeImages();
                                options.imagesScale();
                                options.mdPageBreakPlaceholder();
                                options.doCodeEnrichment();
                                options.doFormulaEnrichment();
                                options.doPictureClassification();
                                options.doPictureDescription();
                                options.pictureDescriptionAreaThreshold();
                                options.pictureDescriptionLocal();
                                options.pictureDescriptionApi();
                                options.vlmPipelineModel();
                                options.vlmPipelineModelLocal();
                                options.vlmPipelineModelApi();
                            }
                        }
                        """,
                        """
                        import ai.docling.api.serve.convert.request.options.ConvertDocumentOptions;

                        class Demo {
                            void call() {
                                ConvertDocumentOptions options = ConvertDocumentOptions.builder().build();

                                options.getFromFormats();
                                options.getToFormats();
                                options.getImageExportMode();
                                options.getDoOcr();
                                options.getForceOcr();
                                options.getOcrEngine();
                                options.getOcrLang();
                                options.getPdfBackend();
                                options.getTableMode();
                                options.getTableCellMatching();
                                options.getPipeline();
                                options.getPageRange();
                                options.getDocumentTimeout();
                                options.getAbortOnError();
                                options.getDoTableStructure();
                                options.getIncludeImages();
                                options.getImagesScale();
                                options.getPageBreakPlaceholder();
                                options.getDoCodeEnrichment();
                                options.getDoFormulaEnrichment();
                                options.getDoPictureClassification();
                                options.getDoPictureDescription();
                                options.getPictureDescriptionAreaThreshold();
                                options.getPictureDescriptionLocal();
                                options.getPictureDescriptionApi();
                                options.getVlmPipelineModel();
                                options.getVlmPipelineModelLocal();
                                options.getVlmPipelineModelApi();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void pictureDescriptionChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.docling.client.convert.request.options.PictureDescriptionApi;
                        import io.arconia.docling.client.convert.request.options.PictureDescriptionLocal;

                        class Demo {
                            void call(PictureDescriptionApi pictureDescriptionApi) {
                                pictureDescriptionApi.url();
                                pictureDescriptionApi.headers();
                                pictureDescriptionApi.params();
                                pictureDescriptionApi.timeout();
                                pictureDescriptionApi.concurrency();
                                pictureDescriptionApi.prompt();
                            }

                            void anotherCall(PictureDescriptionLocal pictureDescriptionLocal) {
                                pictureDescriptionLocal.repoId();
                                pictureDescriptionLocal.prompt();
                                pictureDescriptionLocal.generationConfig();
                            }
                        }
                        """,
                        """
                        import ai.docling.api.serve.convert.request.options.PictureDescriptionApi;
                        import ai.docling.api.serve.convert.request.options.PictureDescriptionLocal;

                        class Demo {
                            void call(PictureDescriptionApi pictureDescriptionApi) {
                                pictureDescriptionApi.getUrl();
                                pictureDescriptionApi.getHeaders();
                                pictureDescriptionApi.getParams();
                                pictureDescriptionApi.getTimeout();
                                pictureDescriptionApi.getConcurrency();
                                pictureDescriptionApi.getPrompt();
                            }

                            void anotherCall(PictureDescriptionLocal pictureDescriptionLocal) {
                                pictureDescriptionLocal.getRepoId();
                                pictureDescriptionLocal.getPrompt();
                                pictureDescriptionLocal.getGenerationConfig();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void vlmModelChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.docling.client.convert.request.options.VlmModelApi;
                        import io.arconia.docling.client.convert.request.options.VlmModelLocal;

                        class Demo {
                            void call(VlmModelApi vlmModelApi) {
                                vlmModelApi.url();
                                vlmModelApi.headers();
                                vlmModelApi.params();
                                vlmModelApi.timeout();
                                vlmModelApi.concurrency();
                                vlmModelApi.prompt();
                                vlmModelApi.scale();
                                vlmModelApi.responseFormat();
                            }

                            void anotherCall(VlmModelLocal vlmModelLocal) {
                                vlmModelLocal.repoId();
                                vlmModelLocal.prompt();
                                vlmModelLocal.scale();
                                vlmModelLocal.responseFormat();
                                vlmModelLocal.inferenceFramework();
                                vlmModelLocal.transformersModelType();
                                vlmModelLocal.extraGenerationConfig();
                            }
                        }
                        """,
                        """
                        import ai.docling.api.serve.convert.request.options.VlmModelApi;
                        import ai.docling.api.serve.convert.request.options.VlmModelLocal;

                        class Demo {
                            void call(VlmModelApi vlmModelApi) {
                                vlmModelApi.getUrl();
                                vlmModelApi.getHeaders();
                                vlmModelApi.getParams();
                                vlmModelApi.getTimeout();
                                vlmModelApi.getConcurrency();
                                vlmModelApi.getPrompt();
                                vlmModelApi.getScale();
                                vlmModelApi.getResponseFormat();
                            }

                            void anotherCall(VlmModelLocal vlmModelLocal) {
                                vlmModelLocal.getRepoId();
                                vlmModelLocal.getPrompt();
                                vlmModelLocal.getScale();
                                vlmModelLocal.getResponseFormat();
                                vlmModelLocal.getInferenceFramework();
                                vlmModelLocal.getTransformersModelType();
                                vlmModelLocal.getExtraGenerationConfig();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void sourceChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.docling.client.convert.request.source.FileSource;
                        import io.arconia.docling.client.convert.request.source.HttpSource;

                        class Demo {
                            void file() {
                                FileSource fileSource = FileSource.builder().build();
                                fileSource.base64String();
                                fileSource.filename();
                            }

                            void http() {
                                HttpSource httpSource = HttpSource.builder().build();
                                httpSource.url();
                                httpSource.headers();
                            }
                        }
                        """,
                        """
                        import ai.docling.api.serve.convert.request.source.FileSource;
                        import ai.docling.api.serve.convert.request.source.HttpSource;

                        class Demo {
                            void file() {
                                FileSource fileSource = FileSource.builder().build();
                                fileSource.getBase64String();
                                fileSource.getFilename();
                            }

                            void http() {
                                HttpSource httpSource = HttpSource.builder().build();
                                httpSource.getUrl();
                                httpSource.getHeaders();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void targetChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.docling.client.convert.request.target.PutTarget;

                        class Demo {
                            void call(PutTarget putTarget) {
                                putTarget.url();
                            }
                        }
                        """,
                        """
                        import ai.docling.api.serve.convert.request.target.PutTarget;

                        class Demo {
                            void call(PutTarget putTarget) {
                                putTarget.getUrl();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void convertDocumentResponseChanges() {
        rewriteRun(
                //language=java
                java(
                        """
                        import io.arconia.docling.client.convert.response.ConvertDocumentResponse;
                        import io.arconia.docling.client.convert.response.DocumentResponse;
                        import io.arconia.docling.client.convert.response.ErrorItem;

                        import java.util.List;

                        class Demo {
                            void call(ConvertDocumentResponse convertDocumentResponse) {
                                DocumentResponse document = convertDocumentResponse.document();
                                document.doctagsContent();
                                document.filename();
                                document.htmlContent();
                                document.jsonContent();
                                document.markdownContent();
                                document.textContent();

                                List<ErrorItem> errors = convertDocumentResponse.errors();
                                ErrorItem error = errors.get(0);
                                error.componentType();
                                error.errorMessage();
                                error.moduleName();

                                convertDocumentResponse.processingTime();
                                convertDocumentResponse.status();
                                convertDocumentResponse.timings();
                            }
                        }
                        """,
                        """
                        import ai.docling.api.serve.convert.response.ConvertDocumentResponse;
                        import ai.docling.api.serve.convert.response.DocumentResponse;
                        import ai.docling.api.serve.convert.response.ErrorItem;

                        import java.util.List;

                        class Demo {
                            void call(ConvertDocumentResponse convertDocumentResponse) {
                                DocumentResponse document = convertDocumentResponse.getDocument();
                                document.getDoctagsContent();
                                document.getFilename();
                                document.getHtmlContent();
                                document.getJsonContent();
                                document.getMarkdownContent();
                                document.getTextContent();

                                List<ErrorItem> errors = convertDocumentResponse.getErrors();
                                ErrorItem error = errors.get(0);
                                error.getComponentType();
                                error.getErrorMessage();
                                error.getModuleName();

                                convertDocumentResponse.getProcessingTime();
                                convertDocumentResponse.getStatus();
                                convertDocumentResponse.getTimings();
                            }
                        }
                        """
                )
        );
    }

    @Test
    void healthCheckResponseChanges() {
        rewriteRun(
                spec -> spec.typeValidationOptions(TypeValidation.builder().methodInvocations(false).build()),
                //language=java
                java(
                        """
                        import io.arconia.docling.client.health.HealthCheckResponse;

                        class Demo {
                            void health() {
                                HealthCheckResponse healthCheckResponse = new HealthCheckResponse("ok");
                                healthCheckResponse.status();
                            }
                        }
                        """,
                        """
                        import ai.docling.api.serve.health.HealthCheckResponse;

                        class Demo {
                            void health() {
                                HealthCheckResponse healthCheckResponse = HealthCheckResponse.builder().status("ok").build();
                                healthCheckResponse.getStatus();
                            }
                        }
                        """
                )
        );
    }

}
