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

package org.onap.ccsdk.cds.blueprintsprocessor.atomix

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.atomix.service.AtomixBluePrintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BluePrintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterInfo
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.deleteNBDir
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AtomixBluePrintClusterServiceTest {
    val log = logger(AtomixBluePrintClusterServiceTest::class)

    @Before
    fun init() {
        runBlocking {
            deleteNBDir("target/cluster")
        }
    }

    /** Testing two cluster with distributed map store creation, This is time consuming test case, taks around 10s **/
    @Test
    fun testClusterJoin() {
        runBlocking {
            val bluePrintClusterServiceOne = createCluster(arrayListOf(5679, 5680))
            // val bluePrintClusterServiceTwo = createCluster(arrayListOf(5681, 5682))
            val bluePrintClusterService = bluePrintClusterServiceOne.get(0)
            log.info("Members : ${bluePrintClusterService.allMembers()}")
            log.info("Master(System) Members : ${bluePrintClusterService.masterMember("system")}")
            log.info("Master(Data) Members : ${bluePrintClusterService.masterMember("data")}")
            testDistributedStore(bluePrintClusterServiceOne)
            testDistributedLock(bluePrintClusterServiceOne)
        }
    }

    private suspend fun createCluster(ports: List<Int>): List<BluePrintClusterService> {
        return withContext(Dispatchers.Default) {
            val members = ports.map { "node-$it" }
            val deferred = ports.map { port ->
                async(Dispatchers.IO) {
                    val nodeId = "node-$port"
                    log.info("********** Starting node($nodeId) on port($port)")
                    val clusterInfo = ClusterInfo(
                        id = "test-cluster", nodeId = nodeId,
                        clusterMembers = members, nodeAddress = "localhost:$port", storagePath = "target/cluster"
                    )
                    val atomixClusterService = AtomixBluePrintClusterService()
                    atomixClusterService.startCluster(clusterInfo)
                    atomixClusterService
                }
            }
            deferred.awaitAll()
        }
    }

    private suspend fun testDistributedStore(bluePrintClusterServices: List<BluePrintClusterService>) {
        /** Test Distributed store creation */
        repeat(2) { storeId ->
            val store = bluePrintClusterServices.get(0).clusterMapStore<JsonNode>(
                "blueprint-runtime-$storeId"
            ).toDistributedMap()
            assertNotNull(store, "failed to get store")
            val store1 = bluePrintClusterServices.get(0).clusterMapStore<JsonNode>(
                "blueprint-runtime-$storeId"
            ).toDistributedMap()

            store1.addListener {
                log.info("Received map event : $it")
            }
            repeat(5) {
                store["key-$storeId-$it"] = "value-$it".asJsonPrimitive()
            }
            delay(10)
            store.close()
        }
    }

    private suspend fun testDistributedLock(bluePrintClusterServices: List<BluePrintClusterService>) {
        val lockName = "sample-lock"
        withContext(Dispatchers.IO) {
            val deferred = async {
                executeLock(bluePrintClusterServices.get(0), "first", lockName)
            }
            val deferred2 = async {
                executeLock(bluePrintClusterServices.get(0), "second", lockName)
            }
            val deferred3 = async {
                executeLock(bluePrintClusterServices.get(0), "third", lockName)
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
}
