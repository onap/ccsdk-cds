package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.DefaultBlueprintRuntimeService

class ResourceAssignmentRuntimeService(private var id: String, private var bluePrintContext: BlueprintContext) :
    DefaultBlueprintRuntimeService(id, bluePrintContext) {

    private lateinit var resolutionId: String
    private var resourceStore: MutableMap<String, JsonNode> = hashMapOf()

    fun createUniqueId(key: String) {
        resolutionId = "$id-$key"
    }

    fun cleanResourceStore() {
        resourceStore.clear()
    }

    fun putResolutionStore(key: String, value: JsonNode) {
        resourceStore[key] = value
    }

    fun getResolutionStore(): MutableMap<String, JsonNode> {
        return resourceStore.mapValues { e -> e.value.deepCopy() as JsonNode }.toMutableMap()
    }

    fun getResolutionStore(key: String): JsonNode {
        return resourceStore[key]
            ?: throw BlueprintProcessorException("failed to get execution property ($key)")
    }

    fun checkResolutionStore(key: String): Boolean {
        return resourceStore.containsKey(key)
    }

    fun getJsonNodeFromResolutionStore(key: String): JsonNode {
        return getResolutionStore(key)
    }

    fun getStringFromResolutionStore(key: String): String? {
        return getResolutionStore(key).asText()
    }

    fun getBooleanFromResolutionStore(key: String): Boolean? {
        return getResolutionStore(key).asBoolean()
    }

    fun getIntFromResolutionStore(key: String): Int? {
        return getResolutionStore(key).asInt()
    }

    fun getDoubleFromResolutionStore(key: String): Double? {
        return getResolutionStore(key).asDouble()
    }

    fun putDictionaryStore(key: String, value: JsonNode) {
        resourceStore["dictionary-$key"] = value
    }

    fun getDictionaryStore(key: String): JsonNode {
        return resourceStore["dictionary-$key"]
            ?: throw BlueprintProcessorException("failed to get execution property (dictionary-$key)")
    }

    fun checkDictionaryStore(key: String): Boolean {
        return resourceStore.containsKey("dictionary-$key")
    }

    fun getJsonNodeFromDictionaryStore(key: String): JsonNode {
        return getResolutionStore("dictionary-$key")
    }

    fun getStringFromDictionaryStore(key: String): String? {
        return getResolutionStore("dictionary-$key").asText()
    }

    fun getBooleanFromDictionaryStore(key: String): Boolean? {
        return getResolutionStore("dictionary-$key").asBoolean()
    }

    fun getIntFromDictionaryStore(key: String): Int? {
        return getResolutionStore("dictionary-$key").asInt()
    }

    fun getDoubleFromDictionaryStore(key: String): Double? {
        return getResolutionStore("dictionary-$key").asDouble()
    }
}
