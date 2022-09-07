package org.onap.ccsdk.cds.blueprintsprocessor.uat.base

import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.uat.logging.LogColor
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.ExtendedTemporaryFolder
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.TestSecuritySettings
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.UatExecutor
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.WorkingFoldersInitializer
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintArchiveUtils.Companion.compressToBytes
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import java.io.File
import java.nio.file.FileSystems
import kotlin.test.BeforeTest
import kotlin.test.AfterTest


/**
 * This is a SpringBootTest abstract Base class, that executes UAT (User Acceptance Tests) by calling
 * callRunUat in an implementation class.
 * See https://wiki.onap.org/pages/viewpage.action?pageId=59965554#ModelingConcepts-tests for further
 * information concerning CDS UAT concept.
 */

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(
    initializers = [
        WorkingFoldersInitializer::class,
        TestSecuritySettings.ServerContextInitializer::class
    ]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
abstract class BaseBlueprintsAcceptanceTest() {
    @BeforeTest
    fun setScope() {
        LogColor.setContextColor(LogColor.COLOR_TEST_CLIENT)
    }

    @AfterTest
    fun clearScope() {
        LogColor.resetContextColor()
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(BaseBlueprintsAcceptanceTest::class.java)

    }

    @Autowired
    // Bean is created programmatically by {@link WorkingFoldersInitializer#initialize(String)}
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    lateinit var tempFolder: ExtendedTemporaryFolder

    @Autowired
    lateinit var uatExecutor: UatExecutor

    @BeforeTest
    fun cleanupTemporaryFolder() {
        tempFolder.deleteAllFiles()
    }

    protected suspend fun callRunUat(pathName: String, uatFilename: String) {
        runBlocking {
            val dir = File(pathName)
            val rootFs =  FileSystems.newFileSystem(dir.canonicalFile.toPath(), null)
            log.info("dirname: ${dir.toString()} rootFs ${rootFs}")

            val uatSpec = rootFs.getPath(uatFilename).toFile().readText()
            val cbaBytes = compressToBytes(rootFs.getPath("/"))
            uatExecutor.execute(uatSpec, cbaBytes)
        }
    }
}
