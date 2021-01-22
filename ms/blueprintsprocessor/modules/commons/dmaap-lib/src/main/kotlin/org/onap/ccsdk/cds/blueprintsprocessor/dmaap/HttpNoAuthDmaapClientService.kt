/*-
 * ============LICENSE_START=======================================================
 * ONAP - CDS
 * ================================================================================
 * Copyright (C) 2019 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.cds.blueprintsprocessor.dmaap

import com.att.nsa.mr.client.MRBatchingPublisher
import com.att.nsa.mr.client.MRClientFactory.createSimplePublisher
import com.att.nsa.mr.client.impl.MRSimplerBatchPublisher

/**
 * Representation of DMAAP client service for HTTP no auth type.
 */
class HttpNoAuthDmaapClientService(
    private val clientProps:
        HttpNoAuthDmaapClientProperties
) :
    BlueprintDmaapClientService {

    /**
     * The constructed DMAAP client.
     */
    var clients: MutableList<MRBatchingPublisher> = mutableListOf()

    /**
     * Returns the DMAAP client after constructing it properly with the data
     * that is required for HTTP no auth connection.
     */
    override fun getDmaapClient(): MutableList<MRBatchingPublisher> {
        if (!clients.isEmpty()) {
            return clients
        }
        val topics = mutableListOf<String>()
        topics.addAll(clientProps.topic.split(","))

        for (t in topics) {
            val client = createSimplePublisher(clientProps.host, t)
            val batchPublisher = client as MRSimplerBatchPublisher
            batchPublisher.setProtocolFlag(clientProps.type)
            batchPublisher.props = clientProps.props
            clients.add(client)
        }

        return clients
    }
}
