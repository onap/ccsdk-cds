/*
 * Copyright © 2019 Bell Canada.
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

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterLock
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import kotlin.test.assertEquals

class BluePrintClusterExtensionsTest {

    private lateinit var clusterLockMock: ClusterLock

    @Before
    fun setup() {
        clusterLockMock = mockk()
        every { clusterLockMock.name() } returns "mock-lock"
    }

    @Test
    fun `executeWithLock - should call unlock and return block result`() {
        runBlocking {
            every { runBlocking { clusterLockMock.tryLock(more(0L)) } } returns true
            every { runBlocking { clusterLockMock.unLock() } } returns Unit

            val result = clusterLockMock.executeWithLock(1_000) { "result" }

            verify { runBlocking { clusterLockMock.unLock() } }
            assertEquals("result", result)
        }
    }

    @Test
    fun `executeWithLock - should call unlock even when block throws exception`() {
        runBlocking {
            every { runBlocking { clusterLockMock.tryLock(more(0L)) } } returns true
            every { runBlocking { clusterLockMock.unLock() } } returns Unit

            try {
                clusterLockMock.executeWithLock(1_000) { throw RuntimeException("It crashed") }
            } catch (e: Exception) {
            }

            verify { runBlocking { clusterLockMock.unLock() } }
        }
    }

    @Test(expected = BluePrintException::class)
    fun `executeWithLock - should throw exception when lock was not acquired within timeout`() {
        runBlocking {
            every { runBlocking { clusterLockMock.tryLock(eq(0L)) } } returns false
            clusterLockMock.executeWithLock(0) { "Will not run" }
        }
    }
}
