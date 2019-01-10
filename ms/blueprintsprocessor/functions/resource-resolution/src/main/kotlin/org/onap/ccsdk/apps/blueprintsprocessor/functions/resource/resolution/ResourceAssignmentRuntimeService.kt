package org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.apps.controllerblueprints.core.service.DefaultBluePrintRuntimeService

class ResourceAssignmentRuntimeService(private var id: String, private var bluePrintContext: BluePrintContext)
    : DefaultBluePrintRuntimeService(id, bluePrintContext){

    private var resourceResolutionStore: MutableMap<String, JsonNode> = hashMapOf()

    override fun getExecutionContext(): MutableMap<String, JsonNode> {
        return resourceResolutionStore
    }

    @Suppress("UNCHECKED_CAST")
    override fun setExecutionContext(executionContext: MutableMap<String, JsonNode>) {
        this.resourceResolutionStore = executionContext
    }

    override fun put(key: String, value: JsonNode) {
        resourceResolutionStore[key] = value
    }

    override fun get(key: String): JsonNode {
        return resourceResolutionStore[key] ?: throw BluePrintProcessorException("failed to get execution property($key)")
    }

    override fun check(key: String): Boolean {
        return resourceResolutionStore.containsKey(key)
    }

    override fun cleanRuntime() {
        resourceResolutionStore.clear()
    }

    private fun getJsonNode(key: String): JsonNode {
        return get(key)
    }

    override fun getAsString(key: String): String? {
        return get(key).asText()
    }

    override fun getAsBoolean(key: String): Boolean? {
        return get(key).asBoolean()
    }

    override fun getAsInt(key: String): Int? {
        return get(key).asInt()
    }

    override fun getAsDouble(key: String): Double? {
        return get(key).asDouble()
    }

}
