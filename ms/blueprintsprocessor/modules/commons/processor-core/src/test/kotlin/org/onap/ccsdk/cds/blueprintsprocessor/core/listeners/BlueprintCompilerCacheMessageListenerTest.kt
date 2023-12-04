package org.onap.ccsdk.cds.blueprintsprocessor.core.listeners

import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.core.cluster.BlueprintClusterTopic
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BluePrintClusterMessage
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BluePrintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterMember
import org.onap.ccsdk.cds.controllerblueprints.core.deleteNBDir
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.reCreateNBDirs
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BlueprintCompilerCacheMessageListenerTest {
    private val path = "deploy"
    private val artifactName = "TEST_CBA"
    private val artifactVersion = "1.0.0"
    private val payload = "$path/$artifactName/$artifactVersion"
    private val deployFile: File = normalizedFile(path, artifactName, artifactVersion)

    @BeforeTest
    fun setUp() {
        runBlocking {
            println("creating ${deployFile.absolutePath}")
            deployFile.reCreateNBDirs()
        }
    }

    @AfterTest
    fun cleanup() {
        runBlocking {
            deleteNBDir(normalizedFile(path).absolutePath)
        }
    }

    @Test
    fun `test deleteNBDir for given path`() {
        runBlocking {
            assertTrue { deployFile.exists() }
            println("deleting ${deployFile.absolutePath}")
            deleteNBDir(deployFile.absolutePath)
            assertFalse { deployFile.exists() }
        }
    }
    @Test
    fun `test onMessage`() {
        assertTrue { deployFile.exists() }
        val messageListener = BlueprintCompilerCacheMessageListener(mockk<BluePrintClusterService>(relaxed = true))
        messageListener.onMessage(
            BluePrintClusterMessage(
                BlueprintClusterTopic.BLUEPRINT_CLEAN_COMPILER_CACHE,
                payload,
                1,
                ClusterMember(
                    id = "id",
                    name = "memberName",
                    memberAddress = "memberAdress"
                )
            )
        )
        assertFalse { deployFile.exists() }
    }
}
