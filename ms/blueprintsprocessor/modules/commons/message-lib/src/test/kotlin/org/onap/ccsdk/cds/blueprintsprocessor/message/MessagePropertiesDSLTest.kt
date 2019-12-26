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
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonString
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.relationshipTypeConnectsTo
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.serviceTemplate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MessagePropertiesDSLTest {

    @Test
    fun testMessageProducerDSL() {
        val serviceTemplate = serviceTemplate("message-properties-test", "1.0.0", "xxx.@xx.com", "message") {
            topologyTemplate {
                messageProducerRelationshipTemplate("sample-basic-auth", "Message Producer") {
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
            relationshipTypes(
                arrayListOf(
                    BluePrintTypes.relationshipTypeConnectsToMessageProducer(),
                    BluePrintTypes.relationshipTypeConnectsTo()
                )
            )
        }
        assertNotNull(serviceTemplate, "failed to create service template")
        val relationshipTemplates = serviceTemplate.topologyTemplate?.relationshipTemplates
        assertNotNull(relationshipTemplates, "failed to get relationship templates")
        assertEquals(1, relationshipTemplates.size, "relationshipTemplates doesn't match")
        assertNotNull(relationshipTemplates["sample-basic-auth"], "failed to get sample-basic-auth")
        println(serviceTemplate.asJsonString(true))
    }

    @Test
    fun testMessageConsumerDSL() {
        val serviceTemplate = serviceTemplate("message-properties-test", "1.0.0", "xxx.@xx.com", "message") {
            topologyTemplate {
                messageConsumerRelationshipTemplate("sample-basic-auth", "Message Consumer") {
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
                messageConsumerRelationshipTemplate("sample-stream-basic-auth", "Message Consumer") {
                    kafkaStreamsBasicAuth {
                        bootstrapServers("sample-bootstrapServers")
                        applicationId("sample-application-id")
                        autoOffsetReset("latest")
                        processingGuarantee(StreamsConfig.EXACTLY_ONCE)
                        topic("sample-streaming-topic")
                    }
                }
            }
            relationshipTypes(
                arrayListOf(
                    BluePrintTypes.relationshipTypeConnectsToMessageConsumer(),
                    BluePrintTypes.relationshipTypeConnectsTo()
                )
            )
        }

        assertNotNull(serviceTemplate, "failed to create service template")
        val relationshipTemplates = serviceTemplate.topologyTemplate?.relationshipTemplates
        assertNotNull(relationshipTemplates, "failed to get relationship templates")
        assertEquals(2, relationshipTemplates.size, "relationshipTemplates doesn't match")
        assertNotNull(relationshipTemplates["sample-basic-auth"], "failed to get sample-basic-auth")
        assertNotNull(relationshipTemplates["sample-stream-basic-auth"], "failed to get sample-stream-basic-auth")
        println(serviceTemplate.asJsonString(true))
    }
}
