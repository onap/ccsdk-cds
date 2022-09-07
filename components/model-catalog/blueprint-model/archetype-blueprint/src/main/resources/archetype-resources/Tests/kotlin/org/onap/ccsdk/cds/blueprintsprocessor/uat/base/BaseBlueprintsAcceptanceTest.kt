package org.onap.ccsdk.cds.blueprintsprocessor.uat.base

import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationStateService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.service.SampleMessagePrioritizationService
import org.onap.ccsdk.cds.blueprintsprocessor.uat.base.BaseBlueprintsAcceptanceTest.TestSecuritySettings
import org.onap.ccsdk.cds.blueprintsprocessor.uat.base.BaseBlueprintsAcceptanceTest.WorkingFoldersInitializer
import org.onap.ccsdk.cds.blueprintsprocessor.uat.logging.LogColor
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.UatExecutor
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintArchiveUtils.Companion.compressToBytes
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.support.TestPropertySourceUtils
import org.springframework.util.Base64Utils
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import javax.annotation.PreDestroy
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

    @Service
    open class TestMessagePrioritizationService(messagePrioritizationStateService: MessagePrioritizationStateService) :
        SampleMessagePrioritizationService(messagePrioritizationStateService)
    @Component
    class WorkingFoldersInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

        override fun initialize(context: ConfigurableApplicationContext) {
            val tempFolder = ExtendedTemporaryFolder()
            val properties = listOf("Deploy", "Archive", "Working")
                .map { "blueprintsprocessor.blueprint${it}Path=${tempFolder.newFolder(it).absolutePath.replace("\\", "/")}" }
                .toTypedArray()
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context, *properties)
            // Expose tempFolder as a bean so it can be accessed via DI
            registerSingleton(context, "tempFolder", ExtendedTemporaryFolder::class.java, tempFolder)
        }

        @Suppress("SameParameterValue")
        private fun <T> registerSingleton(
            context: ConfigurableApplicationContext,
            beanName: String,
            beanClass: Class<T>,
            instance: T
        ) {
            val builder = BeanDefinitionBuilder.genericBeanDefinition(beanClass) { instance }
            (context.beanFactory as BeanDefinitionRegistry).registerBeanDefinition(beanName, builder.beanDefinition)
        }
    }

    class ExtendedTemporaryFolder {

        private val tempFolder = createTempDir("uat")

        @PreDestroy
        fun delete() = tempFolder.deleteRecursively()

        /**
         * A delegate to org.junit.rules.TemporaryFolder.TemporaryFolder.newFolder(String).
         */
        fun newFolder(folderName: String): File {
            val dir = File(tempFolder, folderName)
            if (!dir.mkdir()) {
                throw IOException("Unable to create temporary directory $dir.")
            }
            return dir
        }

        /**
         * Delete all files under the root temporary folder recursively. The folders are preserved.
         */
        fun deleteAllFiles() {
            Files.walkFileTree(
                tempFolder.toPath(),
                object : SimpleFileVisitor<Path>() {
                    @Throws(IOException::class)
                    override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
                        file?.toFile()?.delete()
                        return FileVisitResult.CONTINUE
                    }
                }
            )
        }
    }
    class TestSecuritySettings {
        companion object {

            private const val authUsername = "walter.white"
            private const val authPassword = "Heisenberg"

            fun clientAuthToken() =
                "Basic " + Base64Utils.encodeToString("$authUsername:$authPassword".toByteArray())
        }

        class ServerContextInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

            override fun initialize(context: ConfigurableApplicationContext) {
                TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    context,
                    "security.user.name=$authUsername",
                    "security.user.password={noop}$authPassword"
                )
            }
        }
    }

}