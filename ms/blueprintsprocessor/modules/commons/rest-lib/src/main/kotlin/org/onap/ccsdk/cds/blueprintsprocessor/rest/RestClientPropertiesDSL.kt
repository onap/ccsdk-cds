/*
 *  Copyright © 2019 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.rest

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.PropertiesAssignmentBuilder

fun BluePrintTypes.dslBasicAuthRestClientProperties(block: BasicAuthRestClientPropertiesBuilder.() -> Unit): JsonNode {
    val assignments = BasicAuthRestClientPropertiesBuilder().apply(block).build()
    assignments[RestLibConstants.PROPERTY_TYPE] = RestLibConstants.TYPE_BASIC_AUTH.asJsonPrimitive()
    return assignments.asJsonType()
}

fun BluePrintTypes.dslTokenAuthRestClientProperties(block: TokenAuthRestClientPropertiesBuilder.() -> Unit): JsonNode {
    val assignments = TokenAuthRestClientPropertiesBuilder().apply(block).build()
    assignments[RestLibConstants.PROPERTY_TYPE] = RestLibConstants.TYPE_TOKEN_AUTH.asJsonPrimitive()
    return assignments.asJsonType()
}

fun BluePrintTypes.dslSSLRestClientProperties(block: SSLRestClientPropertiesBuilder.() -> Unit): JsonNode {
    val assignments = SSLRestClientPropertiesBuilder().apply(block).build()
    assignments[RestLibConstants.PROPERTY_TYPE] = RestLibConstants.TYPE_SSL_NO_AUTH.asJsonPrimitive()
    return assignments.asJsonType()
}

open class RestClientPropertiesBuilder : PropertiesAssignmentBuilder() {
    fun type(type: String) {
        type(type.asJsonPrimitive())
    }

    fun type(type: JsonNode) {
        property(RestLibConstants.PROPERTY_TYPE, type)
    }

    open fun url(url: String) {
        url(url.asJsonPrimitive())
    }

    open fun url(url: JsonNode) {
        property("url", url)
    }
}

open class BasicAuthRestClientPropertiesBuilder : RestClientPropertiesBuilder() {
    open fun password(password: String) {
        password(password.asJsonPrimitive())
    }

    open fun password(password: JsonNode) {
        property("password", password)
    }

    open fun username(username: String) {
        username(username.asJsonPrimitive())
    }

    open fun username(username: JsonNode) {
        property("username", username)
    }
}

open class TokenAuthRestClientPropertiesBuilder : RestClientPropertiesBuilder() {
    open fun token(token: String) {
        token(token.asJsonPrimitive())
    }

    open fun token(token: JsonNode) {
        property("token", token)
    }
}

open class SSLRestClientPropertiesBuilder : RestClientPropertiesBuilder() {
    open fun keyStoreInstance(keyStoreInstance: String) {
        keyStoreInstance(keyStoreInstance.asJsonPrimitive())
    }

    open fun keyStoreInstance(keyStoreInstance: JsonNode) {
        property("keyStoreInstance", keyStoreInstance)
    }

    open fun sslTrust(sslTrust: String) {
        sslTrust(sslTrust.asJsonPrimitive())
    }

    open fun sslTrust(sslTrust: JsonNode) {
        property("sslTrust", sslTrust)
    }

    open fun sslTrustPassword(sslTrustPassword: String) {
        sslTrustPassword(sslTrustPassword.asJsonPrimitive())
    }

    open fun sslTrustPassword(sslTrustPassword: JsonNode) {
        property("sslTrustPassword", sslTrustPassword)
    }

    open fun sslKey(sslKey: String) {
        sslKey(sslKey.asJsonPrimitive())
    }

    open fun sslKey(sslKey: JsonNode) {
        property("sslKey", sslKey)
    }

    open fun sslKeyPassword(sslKeyPassword: String) {
        sslKeyPassword(sslKeyPassword.asJsonPrimitive())
    }

    open fun sslKeyPassword(sslKeyPassword: JsonNode) {
        property("sslKeyPassword", sslKeyPassword)
    }
}

open class SSLBasicAuthRestClientPropertiesBuilder : SSLRestClientPropertiesBuilder() {
    // TODO()
}

open class SSLTokenAuthRestClientPropertiesBuilder : SSLRestClientPropertiesBuilder() {
    // TODO()
}
