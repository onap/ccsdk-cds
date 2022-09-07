package org.onap.ccsdk.cds.blueprintsprocessor.uat

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.uat.base.BaseUatResourceResolutionTest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.test.Ignore
import kotlin.test.assertEquals


/**
 * This is a sample implementation of a test class using {@see BaseUatResourceResolutionTest}
 * Please find "TODO" comments, where you need to make your changes
 */
class SampleResourceResolutionTest : BaseUatResourceResolutionTest() {

    companion object {
        val log: Logger = LoggerFactory.getLogger(SampleResourceResolutionTest::class.java)
        private val cwd: String = Path.of("").toAbsolutePath().toString()
        val blueprintBasePath : String = cwd
        // TODO: exchange here the real path to the request json
        val fileNameExecutionServiceInput : String = "$cwd/Tests/sample-resourceresolution-request.json"
    }

    // TODO: remove @Ignore to activate the test
    @Ignore
    @Test
    @Throws(Exception::class)
    fun `test resolveResource for nodeTemplate fetch-nf-config-process`() {
        runBlocking {
            callResolveResources(
                blueprintBasePath,

                // TODO: replace the following parameters with yours
                fileNameExecutionServiceInput,
                "workflowName",
                "nodeTemplateName",
                "artifactPrefixName"
            )
        }.let { (templateMap, assignmentList) ->
            // list of pairs
            val expectedAssignmentList = mutableListOf(
                // TODO: only samples
                "service-instance-id" to "fb84c76d-676e-4a36-9237-52089594292b",
                "service-instance-name" to "cucp-1",
                "vnf-id" to "de59d80e-cbce-4898-9e3c-3a917e89d834"
                // TODO: add your key value pairs here
            )

            // assert size of list
            assertEquals(expectedAssignmentList.size, assignmentList.size)

            val list = expectedAssignmentList.zip(assignmentList)
            list.forEach {
                    (expected, actual) ->
                run {

                    // do individual assertions here

                    // names must be equal
                    assertEquals(expected.first, actual.name)
                    when (expected.first) {
                        // TODO: fill in here your concrete ObjectNode names
                        "objectNodeName1", "objectNodeName2" -> {
                            // ObjectNodes
                            log.info("expected name[${expected.first}] actual name[${actual.name}] " +
                                    "-> expected value[${expected.second}] actual value[${actual.property?.value.toString()}]")

                            // values must be equal
                            assertEquals(expected.second, actual.property?.value.toString())
                        }
                        else -> {
                            // TextNodes. This is the default case
                            log.info("expected name[${expected.first}] actual name[${actual.name}] " +
                                    "-> expected value[${expected.second}] actual value[${actual.property?.value?.asText()}]")
                            // values must be equal
                            assertEquals(expected.second, actual.property?.value?.asText())
                        }
                    }
                }
            }
        }
    }
}