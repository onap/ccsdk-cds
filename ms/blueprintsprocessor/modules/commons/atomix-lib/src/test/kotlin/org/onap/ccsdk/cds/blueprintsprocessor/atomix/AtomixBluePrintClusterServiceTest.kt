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
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.atomix.service.AtomixBluePrintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.atomix.utils.AtomixLibUtils
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterInfo
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.deleteNBDir
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import kotlin.test.assertNotNull

class AtomixBluePrintClusterServiceTest {
    val log = logger(AtomixBluePrintClusterServiceTest::class)

    @Before
    fun init() {
        runBlocking {
            deleteNBDir("target/cluster")
        }
    }

    /** Testing two cluster with distributed map store creation, This is time consuming test casetake around 10s **/
    @Test
    fun testClusterJoin() {
        runBlocking {
            val members = arrayListOf("node-5679", "node-5680")
            val deferred = arrayListOf(5679, 5680).map { port ->
                async(Dispatchers.IO) {
                    val nodeId = "node-$port"
                    log.info("********** Starting node($nodeId) on port($port)")
                    val clusterInfo = ClusterInfo(
                        id = "test-cluster", nodeId = nodeId,
                        clusterMembers = members, nodeAddress = "localhost:$port", storagePath = "target/cluster"
                    )
                    val atomixClusterService = AtomixBluePrintClusterService()
                    atomixClusterService.startCluster(clusterInfo)
                    atomixClusterService.atomix
                }
            }
            val atomix = deferred.awaitAll()
            /** Test Distributed store creation */
            repeat(2) { storeId ->
                val store = AtomixLibUtils.distributedMapStore<JsonNode>(atomix.get(0), "blueprint-runtime-$storeId")
                assertNotNull(store, "failed to get store")
                val store1 = AtomixLibUtils.distributedMapStore<JsonNode>(atomix.get(1), "blueprint-runtime-$storeId")
                store1.addListener {
                    log.info("Received map event : $it")
                }
                repeat(10) {
                    store["key-$storeId-$it"] = "value-$it".asJsonPrimitive()
                }
                delay(100)
                store.close()
            }
        }
    }
}
