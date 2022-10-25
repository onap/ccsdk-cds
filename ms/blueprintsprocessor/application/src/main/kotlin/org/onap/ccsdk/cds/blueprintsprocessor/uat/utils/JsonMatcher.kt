package org.onap.ccsdk.cds.blueprintsprocessor.uat.utils

import org.mockito.ArgumentMatcher
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class JsonMatcher(val expectedJson: String?) : ArgumentMatcher<String> {

    override fun matches(actualJson: String?): Boolean {
        if (expectedJson == null) {
            return actualJson == null
        } else if (actualJson.isNullOrEmpty() && (expectedJson.isEmpty() || expectedJson.equals("null"))) {
            // null, "" and "null" means the same here
            return true
        } else if (!actualJson.isNullOrEmpty() && expectedJson.isNotEmpty()) {
            return try {
                JSONAssert.assertEquals("", expectedJson, actualJson, JSONCompareMode.LENIENT)
                true
            } catch (e: AssertionError) {
                false
            }
        } else {
            return false
        }
    }
}
