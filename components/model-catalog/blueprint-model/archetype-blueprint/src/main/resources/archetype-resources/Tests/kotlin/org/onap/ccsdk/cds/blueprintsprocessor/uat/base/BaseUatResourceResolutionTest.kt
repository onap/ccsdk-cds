package org.onap.ccsdk.cds.blueprintsprocessor.uat.base

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.mockito.Answers
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.utils.PayloadUtils
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionServiceImpl
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolutionDBService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolutionRepository
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.TemplateResolutionRepository
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.TemplateResolutionService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor.CapabilityResourceResolutionProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor.DefaultResourceResolutionProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor.InputResourceResolutionProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor.RestResourceResolutionProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BluePrintRestLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentFunctionScriptingService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.scripts.DeprecatedBlueprintJythonService
import org.onap.ccsdk.cds.blueprintsprocessor.uat.logging.LogColor
import org.onap.ccsdk.cds.blueprintsprocessor.uat.logging.MockInvocationLogger
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.ExpectationDefinition
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.UatDefinition
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.UatExecutor.MockPreInterceptor
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.UatExecutor.SpyPostInterceptor
import org.onap.ccsdk.cds.controllerblueprints.core.config.BluePrintLoadConfiguration
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintTemplateService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext
import org.springframework.context.ApplicationContext

import java.io.File
import java.nio.file.Path
import kotlin.test.BeforeTest

/**
 * This abstract base class supports tests of single workflow steps in an isolated manner, that are using
 * {@see org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionComponent}.
 * This means all the used external systems like e.g. sdnc or aai are mocked and must response with the correct
 * BlueprintWebClientService.WebClientResponse to let the ressource resolution work properly.
 *
 * The component inside CDS, which is responsible for resolving resources is ResourceResolutionService.
 * The ResourceResolutionService is using ResourceAssignmentProcessor's like e.g.
 * InputResourceResolutionProcessor, DefaultResourceResolutionProcessor, RestResourceResolutionProcessor
 * and CapabilityResourceResolutionProcessor
 *
 * These classes are only partially mocked by mockk spyk() function. This means, that the real classes are used
 * and only some methods are rewritten via every {}.returns(...) functions
 *
 * For example the creation of ResourceAssignmentProcessor's are mocked in multiple
 *
 * every {applicationContext.getBean("${ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR}???")}
 * .returns( ResourceResolutionProcessorXXX() ) functions.
 *
 * The test is also based on UAT concept of the CDS project.
 * The test makes usage of following classes:
 *
 * SpyService: Decorator for overwriting the function for handling the rest based communication with external systems
 * fun exchangeResource(methodType: String,path: String,request: String,headers: Map<String, String>):
 *  WebClientResponse<String>
 * Uses ExpectationDefinition from the uat.yaml
 *
 * uat.MockPreInterceptor: Interceptor that hooks in the factory method of creation the different rest clients for
 * external systems
 * BluePrintRestLibPropertyService.blueprintWebClientService(selector: String): BlueprintWebClientService
 * It is used for exchanging the "real service" by a mock (here a mockito based one)
 *
 * uat.SpyPostInterceptor: Interceptor that hooks in the factory method of creation the different rest clients for
 * external systems
 * BluePrintRestLibPropertyService.blueprintWebClientService(selector: String): BlueprintWebClientService
 * It is used to inject the uat.SpyService decorator
 *
 * In a whole we do have the following decorator chain:  uat.SpyService ( Mockito mock (real service) )
 *
 * {@see UatDefinition}: Model of behaviour of external systems. Used at runtime as a representation for
 * request / response correlation of the external systems. It is loaded from the uat.yaml
 *
  *
 * Prerequisites
 * - in the cba Definition folder the enriched data_types.json and node_types.json needs to be in the actual version.
 *   inside the json workflow file (e.g. parameter-consistency.json) the includes to these json must be available.
 * - uat.yaml for UatDefinition must be inside the cba/Test/ directory of the cba. This uat.yaml file must be adapted,
 *   if the logic inside the steps changes.
 *   See https://wiki.onap.org/pages/viewpage.action?pageId=59965554#ModelingConcepts-tests for details
 */
abstract class BaseUatResourceResolutionTest {
    private val resolutionKey = "resolutionKey"
    private val resourceId = "1"
    private val resourceType = "ServiceInstance"
    private val occurrence = 0

    // protected, bacause they maybe needed to be manipulated in inherited test classes
    protected val props = hashMapOf<String, Any>()
    protected var preInterceptor = MockPreInterceptor()
    protected var postInterceptor = SpyPostInterceptor(ObjectMapper())
    protected lateinit var resourceResolutionService: ResourceResolutionService
    protected lateinit var applicationContext : ApplicationContext

    companion object {
        private val log: Logger = LoggerFactory.getLogger(BaseUatResourceResolutionTest::class.java)
        private val mockLoggingListener = MockInvocationLogger(LogColor.markerOf(LogColor.COLOR_MOCKITO))
    }

    @BeforeTest
    fun setup() {
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_STORE_RESULT] = false
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY] = resolutionKey
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_ID] = resourceId
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_TYPE] = resourceType
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_OCCURRENCE] = occurrence
        props[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOLUTION_SUMMARY] = false

        applicationContext = spyk(ReactiveWebServerApplicationContext())
        val templateResolutionRepository = mockk<TemplateResolutionRepository>()
        val templateResolutionService = spyk(TemplateResolutionService(templateResolutionRepository))
        val bluePrintLoadConfiguration = spyk(BluePrintLoadConfiguration())
        val bluePrintTemplateService = spyk(BluePrintTemplateService(bluePrintLoadConfiguration))
        val resourceResolutionRepository = mockk<ResourceResolutionRepository>()
        val resourceResolutionDBService = spyk(ResourceResolutionDBService(resourceResolutionRepository))

        // ResourceResolutionService
        resourceResolutionService = spyk(
            ResourceResolutionServiceImpl(
            applicationContext,
            templateResolutionService,
            bluePrintTemplateService,
            resourceResolutionDBService)
        )

        // BluePrintRestLibPropertyService: setInterceptors "MockPreInterceptor" and "SpyPostInterceptor"
        val bluePrintPropertyConfiguration = spyk(BluePrintPropertyConfiguration())
        val bluePrintPropertiesService = spyk(BluePrintPropertiesService(bluePrintPropertyConfiguration))
        val bluePrintRestLibPropertyService = BluePrintRestLibPropertyService(bluePrintPropertiesService)
        bluePrintRestLibPropertyService.setInterceptors(preInterceptor, postInterceptor)

        // ComponentFunctionScriptingService needed for CapabilityResourceResolutionProcessor
        val blueprintJythonService = spyk(DeprecatedBlueprintJythonService())
        val componentFunctionScriptingService = ComponentFunctionScriptingService(applicationContext, blueprintJythonService)

        // Add ResourceResolutionProcessor's to the Spring ApplicationContext
        // if more needed, add them in your own setUp method in derived test class
        every { applicationContext.getBean("${ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR}source-input") }.returns(
            InputResourceResolutionProcessor()
        )
        every { applicationContext.getBean("${ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR}source-default") }.returns(
            DefaultResourceResolutionProcessor()
        )
        every { applicationContext.getBean("${ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR}source-rest") }.returns(
            RestResourceResolutionProcessor(bluePrintRestLibPropertyService)
        )
        every { applicationContext.getBean("${ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR}source-capability") }.returns(
            CapabilityResourceResolutionProcessor(componentFunctionScriptingService)
        )

        // create the Mockit mocks defined in the uat.yaml
        createMockitoMocksByUatDefinition()
    }

    private fun createMockitoMocksByUatDefinition() {
        // read uat file
        val cwd: String = Path.of("").toAbsolutePath().toString()
        log.info("current working directory : $cwd")

        val uatSpec: String = File("$cwd/Tests/uat.yaml").readText(Charsets.UTF_8)
        val uat = UatDefinition.load(jacksonObjectMapper(), uatSpec)
        val expectationsPerClient = uat.externalServices.associateBy(
            { service ->
                createRestClientMock(service.expectations).also { restClient ->
                    // side-effect: register restClient to override real instance
                    preInterceptor.registerMock(service.selector, restClient)
                }
            },
            { service -> service.expectations }
        )
    }

    private fun createRestClientMock(restExpectations: List<ExpectationDefinition>):
            BlueprintWebClientService {
        val restClient = mock<BlueprintWebClientService>(
            defaultAnswer = Answers.RETURNS_SMART_NULLS,
            // our custom verboseLogging handler
            invocationListeners = arrayOf(mockLoggingListener)
        )

        // Delegates to overloaded exchangeResource(String, String, String, Map<String, String>)
        whenever(restClient.exchangeResource(any(), any(), any()))
            .thenAnswer { invocation ->
                val method = invocation.arguments[0] as String
                val path = invocation.arguments[1] as String
                val request = invocation.arguments[2] as String
                restClient.exchangeResource(method, path, request, emptyMap())
            }
        for (expectation in restExpectations) {
            var stubbing = whenever(
                restClient.exchangeResource(
                    eq(expectation.request.method),
                    eq(expectation.request.path),
                    any(),
                    any()
                )
            )
            for (response in expectation.responses) {
                stubbing = stubbing.thenReturn(
                    BlueprintWebClientService.WebClientResponse(
                        response.status,
                        response.body.toString()
                    )
                )
            }
        }
        return restClient
    }

    protected suspend fun callResolveResources(
        blueprintBasePath: String,
        fileNameExecutionServiceInput: String,
        workflowName: String,
        nodeTemplateName: String,
        artifactPrefixName: String
    ): Pair<String, MutableList<ResourceAssignment>> {
        val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime(
            "12345",
            blueprintBasePath
        )

        val executionServiceInput =
            JacksonUtils.readValueFromFile(
                fileNameExecutionServiceInput,
                ExecutionServiceInput::class.java
            )!!

        val resourceAssignmentRuntimeService =
            ResourceAssignmentUtils.transformToRARuntimeService(
                bluePrintRuntimeService,
                "testResolveResource"
            )

        // Prepare Inputs
        PayloadUtils.prepareInputsFromWorkflowPayload(
            bluePrintRuntimeService,
            executionServiceInput.payload,
            workflowName
        )

        return resourceResolutionService.resolveResources(
            resourceAssignmentRuntimeService,
            nodeTemplateName,
            artifactPrefixName,
            props
        )
    }
}