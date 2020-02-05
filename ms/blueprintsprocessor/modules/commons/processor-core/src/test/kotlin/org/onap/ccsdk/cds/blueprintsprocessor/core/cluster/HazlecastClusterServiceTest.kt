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
import com.hazelcast.core.IMap
import com.hazelcast.core.Member
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BluePrintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterInfo
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.DiscoveryPlatform
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import java.io.Serializable
import java.time.Duration
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HazlecastClusterServiceTest {
    private val log = logger(HazlecastClusterServiceTest::class)

    @Test
    fun testClusterJoin() {
        runBlocking {
            val bluePrintClusterServiceOne =
                createCluster(arrayListOf(5679, 5680, 5681)).toMutableList()
            // delay(1000)
            // Join as Hazlecast Management Node
            // val bluePrintClusterServiceTwo = createCluster(arrayListOf(5682, 5683), false, arrayListOf(5679))
            // bluePrintClusterServiceOne.addAll(bluePrintClusterServiceTwo)
            printReachableMembers(bluePrintClusterServiceOne)
            testDistributedStore(bluePrintClusterServiceOne)
            testDistributedLock(bluePrintClusterServiceOne)

            // executeScheduler(bluePrintClusterServiceOne[0])
            // delay(1000)
            // Shutdown
            shutdown(bluePrintClusterServiceOne)
        }
    }

    private suspend fun createCluster(
        ports: List<Int>,
        joinAsClient: Boolean? = false
    ): List<BluePrintClusterService> {

        return withContext(Dispatchers.Default) {
            val deferred = ports.map { port ->
                async(Dispatchers.IO) {
                    val nodeId = "node-$port"
                    log.info("********** Starting node($nodeId) on port($port)")
                    val properties = Properties()
                    properties["hazelcast.logging.type"] = "slf4j"
                    val clusterInfo =
                        ClusterInfo(
                            id = "test-cluster", discoverPlatform = DiscoveryPlatform.DOCKER_COMPOSE,
                            nodeId = nodeId, joinAsClient = joinAsClient!!,
                            managementGroupCount = 3, managementMemberCount = 3,
                            properties = properties
                        )
                    val hazlecastClusterService = HazlecastClusterService()
                    hazlecastClusterService.startCluster(clusterInfo)
                    hazlecastClusterService
                }
            }
            deferred.awaitAll()
        }
    }

    private suspend fun shutdown(bluePrintClusterServices: List<BluePrintClusterService>) {
        bluePrintClusterServices.forEach { bluePrintClusterService ->
            bluePrintClusterService.shutDown(Duration.ofMillis(10))
        }
    }

    private suspend fun testDistributedStore(bluePrintClusterServices: List<BluePrintClusterService>) {
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

    private suspend fun testDistributedLock(bluePrintClusterServices: List<BluePrintClusterService>) {
        val lockName = "sample-lock"
        withContext(Dispatchers.IO) {
            val deferred = async {
                executeLock(bluePrintClusterServices[0], "first", lockName)
            }
            val deferred2 = async {
                executeLock(bluePrintClusterServices[0], "second", lockName)
            }
            val deferred3 = async {
                executeLock(bluePrintClusterServices[1], "third", lockName)
            }
            deferred.start()
            deferred2.start()
            deferred3.start()
        }
    }

    private suspend fun executeLock(
        bluePrintClusterService: BluePrintClusterService,
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

    private suspend fun executeScheduler(bluePrintClusterService: BluePrintClusterService) {
        log.info("initialising ...")
        val hazlecastClusterService = bluePrintClusterService as HazlecastClusterService

        val memberNameMap = bluePrintClusterService.clusterMapStore<Member>("member-name-map") as IMap
        assertEquals(3, memberNameMap.size, "failed to match member size")
        memberNameMap.forEach { (key, value) -> log.info("nodeId($key), Member($value)") }
        val scheduler = hazlecastClusterService.clusterScheduler("cleanup")
        // scheduler.scheduleOnAllMembers(SampleSchedulerTask(), 0, TimeUnit.SECONDS)
        // scheduler.scheduleOnKeyOwnerAtFixedRate(SampleSchedulerTask(), "node-5680",0, 1, TimeUnit.SECONDS)
        // scheduler.scheduleAtFixedRate(SampleSchedulerTask(), 0, 1, TimeUnit.SECONDS)
        // scheduler.scheduleOnAllMembersAtFixedRate(SampleSchedulerTask(), 0, 5, TimeUnit.SECONDS)
    }

    private suspend fun printReachableMembers(bluePrintClusterServices: List<BluePrintClusterService>) {
        bluePrintClusterServices.forEach { bluePrintClusterService ->
            val hazlecastClusterService = bluePrintClusterService as HazlecastClusterService
            val hazelcast = hazlecastClusterService.hazelcast
            val self = hazelcast.cluster.localMember
            val master = hazlecastClusterService.masterMember("system").memberAddress
            val members = hazlecastClusterService.allMembers().map { it.memberAddress }
            log.info("Cluster Members for($self): master($master) Members($members)")
        }

        val applicationMembers = bluePrintClusterServices[0].applicationMembers("node-56")
        assertEquals(3, applicationMembers.size, "failed to match applications member size")
        log.info("Cluster applicationMembers ($applicationMembers)")
    }
}

open class SampleSchedulerTask : Runnable, Serializable {
    private val log = logger(SampleSchedulerTask::class)
    override fun run() {
        log.info("I am scheduler action")
    }
}
