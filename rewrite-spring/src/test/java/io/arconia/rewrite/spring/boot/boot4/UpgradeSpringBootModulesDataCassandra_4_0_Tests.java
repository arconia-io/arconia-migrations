package io.arconia.rewrite.spring.boot.boot4;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

class UpgradeSpringBootModulesDataCassandra_4_0_Tests implements RewriteTest {

    @Test
    void recipeConfigured() {
        assertRecipesConfigure("io.arconia.rewrite.spring.boot4.UpgradeSpringBootModulesDataCassandra_4_0");
    }

}
