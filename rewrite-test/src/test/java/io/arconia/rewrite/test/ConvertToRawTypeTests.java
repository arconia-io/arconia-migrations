package io.arconia.rewrite.test;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for {@link io.arconia.rewrite.test.ConvertToRawType}.
 */
class ConvertToRawTypeTests implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ConvertToRawType("java.util.ArrayList"));
    }

    @Test
    void convertToRawTypes() {
        rewriteRun(
                //language=java
                java(
                        """
                        import java.util.ArrayList;

                        class Demo {
                            ArrayList<?> items1;
                            ArrayList<String> items2;
                            ArrayList items3 = new ArrayList<>();
                            ArrayList items4 = new ArrayList<String>();
                            ArrayList<?> items5 = new ArrayList<>();
                            ArrayList items6;
                            ArrayList<ArrayList<String>> items7;

                            void process(ArrayList<?> items) {
                            }

                            ArrayList<?> getItems() {
                                return new ArrayList<>();
                            }

                            void moreProcess() {
                                ArrayList<?> items = new ArrayList<>();
                            }
                        }
                        """,
                        """
                        import java.util.ArrayList;

                        class Demo {
                            ArrayList items1;
                            ArrayList items2;
                            ArrayList items3 = new ArrayList();
                            ArrayList items4 = new ArrayList();
                            ArrayList items5 = new ArrayList();
                            ArrayList items6;
                            ArrayList items7;

                            void process(ArrayList items) {
                            }

                            ArrayList getItems() {
                                return new ArrayList();
                            }

                            void moreProcess() {
                                ArrayList items = new ArrayList();
                            }
                        }
                        """
                )
        );
    }

}
