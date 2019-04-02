package org.onap.ccsdk.cds.blueprintsprocessor.services.execution.scripts

import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils

import kotlin.test.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.BeforeTest

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [BluePrintPython::class, PythonExecutorProperty::class, String::class])
@TestPropertySource(properties =
["blueprints.processor.functions.python.executor.modulePaths=./../../../../../components/scripts/python/ccsdk_blueprints",
    "blueprints.processor.functions.python.executor.executionPath=./../../../../../components/scripts/python/ccsdk_blueprints"])
class BlueprintPythonInterpreterProxyTest {

    lateinit var blueprintPythonInterpreterProxy: BlueprintPythonInterpreterProxy

    @Autowired
    lateinit var pythonExecutorProperty: PythonExecutorProperty

    @BeforeTest
    fun init() {
        val blueprintBasePath = "./../../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"
        val pythonPath: MutableList<String> = arrayListOf()
        pythonPath.add(blueprintBasePath)
        pythonPath.addAll(pythonExecutorProperty.modulePaths)
        val pythonClassName = "PythonTestScript"
        val content = JacksonUtils.getContent("./src/test/resources/PythonTestScript.py")

        val blueprintPython = BluePrintPython(pythonExecutorProperty.executionPath, pythonPath, arrayListOf())
        blueprintPython.content = content
        blueprintPython.pythonClassName = pythonClassName
        blueprintPython.moduleName = "Unit test - Blueprint Python Script [Class Name = $pythonClassName]"

        blueprintPythonInterpreterProxy = BlueprintPythonInterpreterProxy(blueprintPython)
    }

    @Test
    fun getPythonInterpreter() {
        val pythonObject = blueprintPythonInterpreterProxy.getPythonInstance(hashMapOf())
        assertNotNull(pythonObject, "failed to get python interpreter")
    }
}