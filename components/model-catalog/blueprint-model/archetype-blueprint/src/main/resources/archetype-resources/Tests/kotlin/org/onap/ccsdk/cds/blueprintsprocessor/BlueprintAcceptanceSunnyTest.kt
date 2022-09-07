package org.onap.ccsdk.cds.blueprintsprocessor.uat

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.uat.base.BaseBlueprintsAcceptanceTest
import kotlin.test.Ignore

/**
 * This is a sample implementation of a test class using {@see BaseBlueprintsAcceptanceTest}
 * Please find "TODO" comments, where you need to make your changes
 */
class BlueprintAcceptanceSunnyTest : BaseBlueprintsAcceptanceTest() {

    // TODO: remove @Ignore to activate the test
    @Ignore
    @Test
    fun `Blueprint User Acceptance Tests sunny case`() {
        runBlocking {

            // TODO: replace the following parameters with yours, if needed.
            // As long as you have only one uat.yaml in the Tests folder of the cba,
            // you can leave the parameters as they are.

            callRunUat("../cba", "Tests/uat.yaml")
        }
    }
}
