/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 Huawei.
 * Modifications Copyright © 2019 Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.rest

open class RestClientProperties {

    lateinit var type: String
    lateinit var url: String
    var additionalHeaders: Map<String, String>? = null
}

open class SSLRestClientProperties : RestClientProperties() {

    lateinit var keyStoreInstance: String // JKS, PKCS12
    lateinit var sslTrust: String
    lateinit var sslTrustPassword: String
    var sslTrustIgnoreHostname: Boolean = false
    var sslKey: String? = null
    var sslKeyPassword: String? = null
}

// FIXME("Define basic auth userName and password properties")
open class SSLBasicAuthRestClientProperties : SSLRestClientProperties() {

    var basicAuth: BasicAuthRestClientProperties? = null
}

// FIXME("Define token properties")
open class SSLTokenAuthRestClientProperties : SSLRestClientProperties() {

    var tokenAuth: TokenAuthRestClientProperties? = null
}

open class BasicAuthRestClientProperties : RestClientProperties() {

    lateinit var password: String
    lateinit var username: String
}

open class TokenAuthRestClientProperties : RestClientProperties() {

    var token: String? = null
}

open class PolicyManagerRestClientProperties : RestClientProperties() {

    lateinit var env: String
    lateinit var clientAuth: String
    lateinit var authorisation: String
}
