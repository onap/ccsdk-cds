/*
 *  Copyright © 2019 Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.config.snapshots.db

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

class ResourceConfigSnapshotServiceTest {

    private val cfgRepository = mockk<ResourceConfigSnapshotRepository>()

    private val cfgService = ResourceConfigSnapshotService(cfgRepository)

    private val resourceId = "1"
    private val resourceType = "PNF"
    private val configSnapshot = "config_snapshot"
    private val resourceStatus = ResourceConfigSnapshot.Status.RUNNING

    @Test
    fun findByResourceIdAndResourceTypeTest() {
        val tr = ResourceConfigSnapshot()
        tr.config_snapshot = "res"
        runBlocking {
            every {
                cfgRepository.findByResourceIdAndResourceTypeAndStatus(any(), any(), any())
            } returns tr
            val res = cfgService.findByResourceIdAndResourceTypeAndStatus(resourceId, resourceType)
            assertEquals(tr.config_snapshot, res)
        }
    }

    @Test
    fun createNewResourceConfigSnapshotTest() {
        val tr = ResourceConfigSnapshot()
        runBlocking {
            every { cfgRepository.saveAndFlush(any<ResourceConfigSnapshot>()) } returns tr
            every {
                cfgRepository.findByResourceIdAndResourceTypeAndStatus(any(), any(), any())
            } returns null
            val res = cfgService.write(configSnapshot, resourceId, resourceType, resourceStatus)
            assertEquals(tr, res)
        }
    }

    @Test
    fun updateExistingResourceConfigSnapshotTest() {
        val tr = ResourceConfigSnapshot()
        runBlocking {
            every { cfgRepository.saveAndFlush(any<ResourceConfigSnapshot>()) } returns tr
            every {
                cfgRepository.findByResourceIdAndResourceTypeAndStatus(any(), any(), any())
            } returns tr
            every {
                cfgRepository.deleteByResourceIdAndResourceTypeAndStatus(any(), any(), any())
            } returns Unit
            val res = cfgService.write(configSnapshot, resourceId, resourceType)
            verify {
                cfgRepository.deleteByResourceIdAndResourceTypeAndStatus(eq(resourceId), eq(resourceType), eq(resourceStatus))
            }
            assertEquals(tr, res)
        }
    }

    @Test
    fun deleteResourceConfigSnapshot() {
        runBlocking {
            every {
                cfgRepository.deleteByResourceIdAndResourceTypeAndStatus(any(), any(), any())
            } returns Unit
            cfgService.deleteByResourceIdAndResourceTypeAndStatus(resourceId, resourceType, resourceStatus)
            verify {
                cfgRepository.deleteByResourceIdAndResourceTypeAndStatus(eq(resourceId), eq(resourceType), eq(resourceStatus))
            }
        }
    }
}
