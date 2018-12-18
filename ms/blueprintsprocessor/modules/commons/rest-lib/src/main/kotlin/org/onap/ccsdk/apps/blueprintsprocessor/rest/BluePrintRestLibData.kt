/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.blueprintsprocessor.rest

open class RestClientProperties {
    lateinit var type: String
    lateinit var url: String
    lateinit var userId: String
}

open class BasicAuthRestClientProperties : RestClientProperties() {
    var token: String? = null
}

open class SSLBasicAuthRestClientProperties : RestClientProperties() {
    lateinit var keyStoreInstance: String // JKS, PKCS12
    lateinit var sslTrust: String
    lateinit var sslTrustPasswd: String
    lateinit var sslKey: String
    lateinit var sslKeyPasswd: String
}

open class DME2RestClientProperties : RestClientProperties() {
    lateinit var service: String
    lateinit var subContext: String
    lateinit var version: String
    lateinit var envContext: String
    lateinit var routeOffer: String
    var partner: String? = null
    lateinit var appId: String
}

open class PolicyManagerRestClientProperties : RestClientProperties() {
    lateinit var env: String
    lateinit var clientAuth: String
    lateinit var authorisation: String
}