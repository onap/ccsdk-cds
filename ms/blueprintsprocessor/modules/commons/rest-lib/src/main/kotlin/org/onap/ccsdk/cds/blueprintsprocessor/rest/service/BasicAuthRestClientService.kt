/*
 * Copyright © 2017-2019 AT&T, Bell Canada, Nordix Foundation
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

package org.onap.ccsdk.cds.blueprintsprocessor.rest.service

import org.apache.http.message.BasicHeader
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BasicAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.rest.utils.WebClientUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.nio.charset.Charset
import java.util.*

class BasicAuthRestClientService(private val restClientProperties:
                                 BasicAuthRestClientProperties) :
    BlueprintWebClientService {
    //TODO: maybe fail-fast in constructor is preferable?
    /* init {
         if(restClientProperties.additionalHeaders!!.containsKey(HttpHeaders.AUTHORIZATION))
             throw BluePrintProcessorException("additionalHeaders cannot contain Authorization header")
     }*/


    override fun defaultHeaders(): Map<String, String> {

        val encodedCredentials = setBasicAuth(restClientProperties.username,
            restClientProperties.password)
        return mapOf(
            HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON_VALUE,
            HttpHeaders.ACCEPT to MediaType.APPLICATION_JSON_VALUE,
            HttpHeaders.AUTHORIZATION to "Basic $encodedCredentials")
    }

    override fun host(uri: String): String {
        return restClientProperties.url + uri
    }

    override fun convertToBasicHeaders(headers: Map<String, String>):
        Array<BasicHeader> {
        val customHeaders: MutableMap<String, String> = headers.toMutableMap()
        //Extract additionalHeaders from the requestProperties and
        //throw an error if HttpHeaders.AUTHORIZATION key (headers are case-insensitive)
        restClientProperties.additionalHeaders?.let {
            if (it.keys.map { k -> k.toLowerCase().trim() }.contains(HttpHeaders.AUTHORIZATION.toLowerCase())) {
                val errMsg = "Error in definition of endpoint ${restClientProperties.url}." +
                    " User-supplied \"additionalHeaders\" cannot contain AUTHORIZATION header with" +
                    " auth-type \"${RestLibConstants.TYPE_BASIC_AUTH}\""
                WebClientUtils.log.error(errMsg)
                throw BluePrintProcessorException(errMsg)
            } else {
                customHeaders.putAll(it)
            }
        }

        if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            val encodedCredentials = setBasicAuth(
                restClientProperties.username,
                restClientProperties.password)
            customHeaders[HttpHeaders.AUTHORIZATION] =
                "Basic $encodedCredentials"
        }
        return super.convertToBasicHeaders(customHeaders)
    }

    private fun setBasicAuth(username: String, password: String): String {

        val credentialsString = "$username:$password"
        return Base64.getEncoder().encodeToString(
            credentialsString.toByteArray(Charset.defaultCharset()))
    }


}
