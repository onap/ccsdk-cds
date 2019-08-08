package org.onap.ccsdk.cds.blueprintsprocessor.functions.config.snapshots.db

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

    @Test(expected = NoSuchElementException::class)
    fun notFoundEntryReturnsExceptionTest() {
        val tr = ResourceConfigSnapshot()
        runBlocking {
            every {
                cfgRepository.findByResourceIdAndResourceTypeAndStatus(any(), any(), any())
            } returns tr
            val snap = cfgService.findByResourceIdAndResourceTypeAndStatus("MISSING_ID", "UNKNOWN_TYPE")
            assertTrue ( snap.isBlank(), "Not found but returned a non empty string" )
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
            val res = cfgService.write( configSnapshot, resourceId, resourceType, resourceStatus)
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
            val res = cfgService.write( configSnapshot, resourceId, resourceType)
            verify {
                cfgRepository.deleteByResourceIdAndResourceTypeAndStatus(eq(resourceId), eq(resourceType), eq(resourceStatus))
            }
            assertEquals(tr, res)
        }
    }
}