package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
class UtilsTest {

    @Test
    fun `valid Http status codes should be produced for valid parameters`() {
        val httpStatusCode200 = determineHttpStatusCode(200)
        assertEquals(HttpStatus.OK, httpStatusCode200)

        val httpStatusCode500 = determineHttpStatusCode(500)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, httpStatusCode500)
    }

    @Test
    fun `Http status code 500 should be produced for any invalid parameter`() {
        val nonExistentHttpStatusCode = determineHttpStatusCode(999999)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, nonExistentHttpStatusCode)
    }

}