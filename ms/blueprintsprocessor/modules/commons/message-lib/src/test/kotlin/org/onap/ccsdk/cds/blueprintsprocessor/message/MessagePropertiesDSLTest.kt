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
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.relationshipTypeConnectsTo
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.serviceTemplate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MessagePropertiesDSLTest {

    @Test
    fun testScramSslMessageProducerDSL() {
        val serviceTemplate = serviceTemplate("message-properties-test", "1.0.0", "xxx.@xx.com", "message") {
            topologyTemplate {
                relationshipTemplateMessageProducer("sample-scram-ssl-auth", "Message Producer") {
                    kafkaScramSslAuth {
                        bootstrapServers("sample-bootstrapServers")
                        clientId("sample-client-id")
                        acks("all")
                        maxBlockMs(0)
                        reconnectBackOffMs(60 * 60 * 1000)
                        enableIdempotence(true)
                        topic("sample-topic")
                        truststore("/path/to/truststore.jks")
                        truststorePassword("secretpassword")
                        truststoreType("JKS")
                        keystore("/path/to/keystore.jks")
                        keystorePassword("secretpassword")
                        keystoreType("JKS")
                        sslEndpointIdentificationAlgorithm("")
                        saslMechanism("SCRAM-SHA-512")
                        scramUsername("sample-user")
                        scramPassword("secretpassword")
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
        assertNotNull(relationshipTemplates["sample-scram-ssl-auth"], "failed to get sample-scram-ssl-auth")

        val relationshipTypes = serviceTemplate.relationshipTypes
        assertNotNull(relationshipTypes, "failed to get relationship types")
        assertEquals(2, relationshipTypes.size, "relationshipTypes doesn't match")
        assertNotNull(
            relationshipTypes[BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO],
            "failed to get ${BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO}"
        )
        assertNotNull(
            relationshipTypes[BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_MESSAGE_PRODUCER],
            "failed to get ${BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_MESSAGE_PRODUCER}"
        )
    }

    @Test
    fun testScramSslAuthMessageConsumerDSL() {
        val serviceTemplate = serviceTemplate("message-properties-test", "1.0.0", "xxx.@xx.com", "message") {
            topologyTemplate {
                relationshipTemplateMessageConsumer("sample-scram-ssl-auth", "Message Consumer") {
                    kafkaScramSslAuth {
                        bootstrapServers("sample-bootstrapServers")
                        clientId("sample-client-id")
                        groupId("sample-group-id")
                        topic("sample-topic")
                        autoCommit(false)
                        autoOffsetReset("latest")
                        pollMillSec(5000)
                        pollRecords(20)
                        truststore("/path/to/truststore.jks")
                        truststorePassword("secretpassword")
                        truststoreType("JKS")
                        keystore("/path/to/keystore.jks")
                        keystorePassword("secretpassword")
                        keystoreType("JKS")
                        sslEndpointIdentificationAlgorithm("")
                        saslMechanism("SCRAM-SHA-512")
                        scramUsername("sample-user")
                        scramPassword("secretpassword")
                    }
                }
                relationshipTemplateMessageConsumer("sample-stream-scram-ssl-auth", "Message Consumer") {
                    kafkaStreamsScramSslAuth {
                        bootstrapServers("sample-bootstrapServers")
                        applicationId("sample-application-id")
                        autoOffsetReset("latest")
                        processingGuarantee(StreamsConfig.EXACTLY_ONCE)
                        topic("sample-streaming-topic")
                        truststore("/path/to/truststore.jks")
                        truststorePassword("secretpassword")
                        truststoreType("JKS")
                        keystore("/path/to/keystore.jks")
                        keystorePassword("secretpassword")
                        keystoreType("JKS")
                        sslEndpointIdentificationAlgorithm("")
                        saslMechanism("SCRAM-SHA-512")
                        scramUsername("sample-user")
                        scramPassword("secretpassword")
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
        assertNotNull(relationshipTemplates["sample-scram-ssl-auth"], "failed to get sample-scram-ssl-auth")
        assertNotNull(relationshipTemplates["sample-stream-scram-ssl-auth"], "failed to get sample-stream-scram-ssl-auth")

        val relationshipTypes = serviceTemplate.relationshipTypes
        assertNotNull(relationshipTypes, "failed to get relationship types")
        assertEquals(2, relationshipTypes.size, "relationshipTypes doesn't match")
        assertNotNull(
            relationshipTypes[BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO],
            "failed to get ${BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO}"
        )
        assertNotNull(
            relationshipTypes[BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_MESSAGE_CONSUMER],
            "failed to get ${BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_MESSAGE_CONSUMER}"
        )
    }
}
