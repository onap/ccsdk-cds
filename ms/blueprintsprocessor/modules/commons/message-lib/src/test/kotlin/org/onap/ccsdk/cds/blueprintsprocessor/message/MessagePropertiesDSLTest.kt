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

package org.onap.ccsdk.cds.blueprintsprocessor.message

import org.apache.kafka.streams.StreamsConfig
import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.relationshipTypeConnectsTo
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.serviceTemplate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MessagePropertiesDSLTest {

    @Test
    fun testMessageProducerDSL() {
        val serviceTemplate = serviceTemplate("message-properties-test", "1.0.0", "xxx.@xx.com", "message") {
            topologyTemplate {
                relationshipTemplateMessageProducer("sample-basic-auth", "Message Producer") {
                    kafkaBasicAuth {
                        bootstrapServers("sample-bootstrapServers")
                        clientId("sample-client-id")
                        acks("all")
                        retries(3)
                        enableIdempotence(true)
                        topic("sample-topic")
                    }
                }
            }
            relationshipTypeConnectsToMessageProducer()
            relationshipTypeConnectsTo()
        }

        // println(serviceTemplate.asJsonString(true))
        assertNotNull(serviceTemplate, "failed to create service template")
        val relationshipTemplates = serviceTemplate.topologyTemplate?.relationshipTemplates
        assertNotNull(relationshipTemplates, "failed to get relationship templates")
        assertEquals(1, relationshipTemplates.size, "relationshipTemplates doesn't match")
        assertNotNull(relationshipTemplates["sample-basic-auth"], "failed to get sample-basic-auth")

        val relationshipTypes = serviceTemplate.relationshipTypes
        assertNotNull(relationshipTypes, "failed to get relationship types")
        assertEquals(2, relationshipTypes.size, "relationshipTypes doesn't match")
        assertNotNull(
            relationshipTypes[BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO],
            "failed to get ${BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO}"
        )
        assertNotNull(
            relationshipTypes[BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_MESSAGE_PRODUCER],
            "failed to get ${BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_MESSAGE_PRODUCER}"
        )
    }

    @Test
    fun testMessageConsumerDSL() {
        val serviceTemplate = serviceTemplate("message-properties-test", "1.0.0", "xxx.@xx.com", "message") {
            topologyTemplate {
                relationshipTemplateMessageConsumer("sample-basic-auth", "Message Consumer") {
                    kafkaBasicAuth {
                        bootstrapServers("sample-bootstrapServers")
                        clientId("sample-client-id")
                        groupId("sample-group-id")
                        topic("sample-topic")
                        autoCommit(false)
                        autoOffsetReset("latest")
                        pollMillSec(5000)
                        pollRecords(20)
                    }
                }
                relationshipTemplateMessageConsumer("sample-stream-basic-auth", "Message Consumer") {
                    kafkaStreamsBasicAuth {
                        bootstrapServers("sample-bootstrapServers")
                        applicationId("sample-application-id")
                        autoOffsetReset("latest")
                        processingGuarantee(StreamsConfig.EXACTLY_ONCE)
                        topic("sample-streaming-topic")
                    }
                }
            }
            relationshipTypeConnectsToMessageConsumer()
            relationshipTypeConnectsTo()
        }

        // println(serviceTemplate.asJsonString(true))
        assertNotNull(serviceTemplate, "failed to create service template")
        val relationshipTemplates = serviceTemplate.topologyTemplate?.relationshipTemplates
        assertNotNull(relationshipTemplates, "failed to get relationship templates")
        assertEquals(2, relationshipTemplates.size, "relationshipTemplates doesn't match")
        assertNotNull(relationshipTemplates["sample-basic-auth"], "failed to get sample-basic-auth")
        assertNotNull(relationshipTemplates["sample-stream-basic-auth"], "failed to get sample-stream-basic-auth")

        val relationshipTypes = serviceTemplate.relationshipTypes
        assertNotNull(relationshipTypes, "failed to get relationship types")
        assertEquals(2, relationshipTypes.size, "relationshipTypes doesn't match")
        assertNotNull(
            relationshipTypes[BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO],
            "failed to get ${BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO}"
        )
        assertNotNull(
            relationshipTypes[BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_MESSAGE_CONSUMER],
            "failed to get ${BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_MESSAGE_CONSUMER}"
        )
    }
}
