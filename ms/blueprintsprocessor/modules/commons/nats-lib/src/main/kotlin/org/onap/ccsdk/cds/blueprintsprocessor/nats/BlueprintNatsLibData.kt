/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.nats

import org.onap.ccsdk.cds.blueprintsprocessor.nats.utils.NatsClusterUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.ClusterUtils

open class NatsConnectionProperties {

    lateinit var type: String
    var clusterId: String = NatsClusterUtils.clusterId()
    var clientId: String = ClusterUtils.clusterNodeId()
    lateinit var host: String

    /** Rest endpoint selector to access Monitoring API */
    var monitoringSelector: String? = null
}

open class TokenAuthNatsConnectionProperties : NatsConnectionProperties() {

    lateinit var token: String
}

open class TLSAuthNatsConnectionProperties : NatsConnectionProperties() {

    var trustCertCollection: String? = null

    /** Below Used only for Mutual TLS */
    var clientCertChain: String? = null
    var clientPrivateKey: String? = null
}
