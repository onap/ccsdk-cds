/*
 *  Copyright © 2019 IBM.
 *  Modifications Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.grpc

/** GRPC Server Properties */
open class GrpcServerProperties {

    lateinit var type: String
    var port: Int = -1
}

open class TokenAuthGrpcServerProperties : GrpcServerProperties() {

    lateinit var token: String
}

open class TLSAuthGrpcServerProperties : GrpcServerProperties() {

    lateinit var certChain: String
    lateinit var privateKey: String

    /** Below Used only for Mutual TLS */
    var trustCertCollection: String? = null
}

/** GRPC Client Properties */
open class GrpcClientProperties {

    lateinit var type: String
    lateinit var host: String
    var port: Int = -1
}

open class TokenAuthGrpcClientProperties : GrpcClientProperties() {

    lateinit var token: String
}

open class TLSAuthGrpcClientProperties : GrpcClientProperties() {

    var trustCertCollection: String? = null

    /** Below Used only for Mutual TLS */
    var clientCertChain: String? = null
    var clientPrivateKey: String? = null
}

open class BasicAuthGrpcClientProperties : GrpcClientProperties() {

    lateinit var username: String
    lateinit var password: String
}
