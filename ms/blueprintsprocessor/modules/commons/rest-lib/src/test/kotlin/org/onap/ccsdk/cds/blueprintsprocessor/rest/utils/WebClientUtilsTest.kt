/*
 * Copyright © 2026 Deutsche Telekom AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.rest.utils

import org.apache.http.HttpResponse
import org.apache.http.HttpVersion
import org.apache.http.client.methods.HttpGet
import org.apache.http.message.BasicHttpResponse
import org.junit.Test
import java.io.IOException
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class WebClientUtilsTest {

    @Test
    fun `loggableUrl drops user info and query string`() {
        assertEquals(
            "https://aai.onap:8443/aai/v24/network",
            WebClientUtils.loggableUrl(URI("https://user:secret@aai.onap:8443/aai/v24/network?token=abc"))
        )
    }

    @Test
    fun `logExecution returns the response`() {
        val response: HttpResponse = BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK")

        assertSame(response, WebClientUtils.logExecution(HttpGet("http://localhost/sample")) { response })
    }

    @Test
    fun `logExecution rethrows a failed call`() {
        assertFailsWith<IOException> {
            WebClientUtils.logExecution<HttpResponse>(HttpGet("http://localhost/sample")) {
                throw IOException("Connection refused")
            }
        }
    }
}
