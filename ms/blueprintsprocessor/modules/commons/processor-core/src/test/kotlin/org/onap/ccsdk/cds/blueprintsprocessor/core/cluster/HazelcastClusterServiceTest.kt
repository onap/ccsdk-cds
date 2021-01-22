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

package org.onap.ccsdk.cds.blueprintsprocessor.core.cluster

import com.fasterxml.jackson.databind.JsonNode
import com.hazelcast.client.config.YamlClientConfigBuilder
import com.hazelcast.cluster.Member
import com.hazelcast.config.FileSystemYamlConfig
import com.hazelcast.instance.impl.HazelcastInstanceFactory
import com.hazelcast.map.IMap
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BlueprintClusterMessage
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BlueprintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BlueprintClusterMessageListener
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterInfo
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import java.io.Serializable
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HazelcastClusterServiceTest {

    private val log = logger(HazelcastClusterServiceTest::class)
    private val clusterSize = 3

    @Before
    @After
    fun killAllHazelcastInstances() {
        HazelcastInstanceFactory.terminateAll()
    }

    @Test
    fun testClientFileSystemYamlConfig() {
        System.setProperty(BlueprintConstants.PROPERTY_CLUSTER_ID, "test-cluster")
        System.setProperty(BlueprintConstants.PROPERTY_CLUSTER_NODE_ID, "node-1234")
        System.setProperty(
            "hazelcast.client.config",
            normalizedFile("./src/test/resources/hazelcast/hazelcast-client.yaml").absolutePath
        )
        val config = YamlClientConfigBuilder().build()
        assertNotNull(config)
        assertEquals("test-cluster", config.clusterName)
        assertEquals("node-1234", config.instanceName)
    }

    @Test
    fun testServerFileSystemYamlConfig() {
        System.setProperty(BlueprintConstants.PROPERTY_CLUSTER_ID, "test-cluster")
        System.setProperty(BlueprintConstants.PROPERTY_CLUSTER_NODE_ID, "node-1234")
        val configFile = normalizedFile("./src/test/resources/hazelcast/hazelcast.yaml")
        val config = FileSystemYamlConfig(configFile)
        assertNotNull(config)
        assertEquals("test-cluster", config.clusterName)
        assertEquals("node-1234", config.instanceName)
    }

    @Test
    fun testClusterJoin() {
        runBlocking {
            val bluePrintClusterServiceOne =
                createCluster(arrayListOf(1, 2, 3)).toMutableList()
            printReachableMembers(bluePrintClusterServiceOne)
            testDistributedStore(bluePrintClusterServiceOne)
            testDistributedLock(bluePrintClusterServiceOne)
        }
    }

    @Test
    fun testClusterMessaging() {
        runBlocking {
            val bluePrintClusterServiceOne =
                createCluster(arrayListOf(1, 2, 3)).toMutableList()
            printReachableMembers(bluePrintClusterServiceOne)
            testMessageReceived(bluePrintClusterServiceOne)
        }
    }

    private suspend fun testMessageReceived(bluePrintClusterServices: List<BlueprintClusterService>) {
        val sender = bluePrintClusterServices[0] as HazelcastClusterService
        val receiver = bluePrintClusterServices[1] as HazelcastClusterService
        val messageSent = "hello world"
        var isMessageReceived = false
        val uuid = receiver.addBlueprintClusterMessageListener(
            BlueprintClusterTopic.BLUEPRINT_CLEAN_COMPILER_CACHE,
            object : BlueprintClusterMessageListener<String> {
                override fun onMessage(message: BlueprintClusterMessage<String>?) {
                    log.info("Message received - ${message?.payload}")
                    isMessageReceived = messageSent == message?.payload
                }
            }
        )

        assertNotNull(uuid)
        sender.sendMessage(BlueprintClusterTopic.BLUEPRINT_CLEAN_COMPILER_CACHE, messageSent)
        delay(1000)
        assertTrue(isMessageReceived)

        assertTrue(receiver.removeBlueprintClusterMessageListener(BlueprintClusterTopic.BLUEPRINT_CLEAN_COMPILER_CACHE, uuid))
        assertFalse(receiver.removeBlueprintClusterMessageListener(BlueprintClusterTopic.BLUEPRINT_CLEAN_COMPILER_CACHE, uuid))
    }

    private suspend fun createCluster(
        ids: List<Int>,
        joinAsClient: Boolean? = false
    ): List<BlueprintClusterService> {

        return withContext(Dispatchers.Default) {
            val deferred = ids.map { id ->
                async(Dispatchers.IO) {
                    val nodeId = "node-$id"
                    log.info("********** Starting ($nodeId)")
                    val properties = Properties()
                    properties["hazelcast.logging.type"] = "slf4j"
                    val clusterInfo =
                        if (joinAsClient!!) {
                            ClusterInfo(
                                id = "test-cluster", nodeId = nodeId, joinAsClient = true,
                                configFile = "./src/test/resources/hazelcast/hazelcast-client.yaml",
                                properties = properties
                            )
                        } else {
                            ClusterInfo(
                                id = "test-cluster", nodeId = nodeId, joinAsClient = false,
                                configFile = "./src/test/resources/hazelcast/hazelcast-cluster.yaml",
                                properties = properties
                            )
                        }
                    val hazelcastClusterService = HazelcastClusterService(mockk(relaxed = true))
                    hazelcastClusterService.startCluster(clusterInfo)
                    hazelcastClusterService
                }
            }
            deferred.awaitAll()
        }
    }

    private suspend fun testDistributedStore(bluePrintClusterServices: List<BlueprintClusterService>) {
        /** Test Distributed store creation */
        repeat(2) { storeId ->
            val store = bluePrintClusterServices[0].clusterMapStore<JsonNode>(
                "blueprint-runtime-$storeId"
            ) as IMap
            assertNotNull(store, "failed to get store")
            repeat(5) {
                store["key-$storeId-$it"] = "value-$it".asJsonPrimitive()
            }

            val store1 = bluePrintClusterServices[1].clusterMapStore<JsonNode>(
                "blueprint-runtime-$storeId"
            ) as IMap

            store1.values.map {
                log.trace("Received map event : $it")
            }
            delay(5)
            store.clear()
        }
    }

    private suspend fun testDistributedLock(bluePrintClusterServices: List<BlueprintClusterService>) {
        val lockName = "sample-lock"
        withContext(Dispatchers.IO) {
            val deferred = async {
                newSingleThreadContext("first").use {
                    withContext(it) {
                        executeLock(bluePrintClusterServices[0], "first", lockName)
                    }
                }
            }
            val deferred2 = async {
                newSingleThreadContext("second").use {
                    withContext(it) {
                        executeLock(bluePrintClusterServices[1], "second", lockName)
                    }
                }
            }
            val deferred3 = async {
                newSingleThreadContext("third").use {
                    withContext(it) {
                        executeLock(bluePrintClusterServices[2], "third", lockName)
                    }
                }
            }
            deferred.start()
            deferred2.start()
            deferred3.start()
        }
    }

    private suspend fun executeLock(
        bluePrintClusterService: BlueprintClusterService,
        lockId: String,
        lockName: String
    ) {
        log.info("initialising $lockId lock...")
        val distributedLock = bluePrintClusterService.clusterLock(lockName)
        assertNotNull(distributedLock, "failed to create distributed $lockId lock")
        distributedLock.lock()
        assertTrue(distributedLock.isLocked(), "failed to lock $lockId")
        try {
            log.info("locked $lockId process for 5mSec")
            delay(5)
        } finally {
            distributedLock.unLock()
            log.info("$lockId lock released")
        }
        distributedLock.close()
    }

    private suspend fun executeScheduler(bluePrintClusterService: BlueprintClusterService) {
        log.info("initialising ...")
        val hazelcastClusterService = bluePrintClusterService as HazelcastClusterService

        val memberNameMap = bluePrintClusterService.clusterMapStore<Member>("member-name-map") as IMap
        assertEquals(3, memberNameMap.size, "failed to match member size")
        memberNameMap.forEach { (key, value) -> log.info("nodeId($key), Member($value)") }
        val scheduler = hazelcastClusterService.clusterScheduler("cleanup")
        // scheduler.scheduleOnAllMembers(SampleSchedulerTask(), 0, TimeUnit.SECONDS)
        // scheduler.scheduleOnKeyOwnerAtFixedRate(SampleSchedulerTask(), "node-5680",0, 1, TimeUnit.SECONDS)
        // scheduler.scheduleAtFixedRate(SampleSchedulerTask(), 0, 1, TimeUnit.SECONDS)
        // scheduler.scheduleOnAllMembersAtFixedRate(SampleSchedulerTask(), 0, 5, TimeUnit.SECONDS)
    }

    private suspend fun printReachableMembers(bluePrintClusterServices: List<BlueprintClusterService>) {
        bluePrintClusterServices.forEach { bluePrintClusterService ->
            val hazelcastClusterService = bluePrintClusterService as HazelcastClusterService
            val hazelcast = hazelcastClusterService.hazelcast
            val self = if (!bluePrintClusterService.isClient()) hazelcast.cluster.localMember else null
            val master = hazelcastClusterService.masterMember("system").memberAddress
            val members = hazelcastClusterService.allMembers().map { it.memberAddress }
            log.info("Cluster Members for($self): master($master) Members($members)")
        }

        val applicationMembers = bluePrintClusterServices[0].applicationMembers("node-")
        assertEquals(clusterSize, applicationMembers.size, "failed to match applications member size")
        log.info("Cluster applicationMembers ($applicationMembers)")
    }
}

open class SampleSchedulerTask : Runnable, Serializable {

    private val log = logger(SampleSchedulerTask::class)
    override fun run() {
        log.info("I am scheduler action")
    }
}
