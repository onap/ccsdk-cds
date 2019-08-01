/*
 *  Copyright © 2019 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution

import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentScriptExecutor
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.nodeType

fun BluePrintTypes.nodeTypeSourceInput(): NodeType {
    return nodeType(id = "source-input", version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
            derivedFrom = BluePrintConstants.MODEL_TYPE_NODE_RESOURCE_SOURCE,
            description = "This is Input Resource Source Node Type") {}
}

fun BluePrintTypes.nodeTypeSourceDefault(): NodeType {
    return nodeType(id = "source-default", version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
            derivedFrom = BluePrintConstants.MODEL_TYPE_NODE_RESOURCE_SOURCE,
            description = "This is Default Resource Source Node Type") {}
}

fun BluePrintTypes.nodeTypeSourceDb(): NodeType {
    return nodeType(id = "source-db", version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
            derivedFrom = BluePrintConstants.MODEL_TYPE_NODE_RESOURCE_SOURCE,
            description = "This is Database Resource Source Node Type") {
        property("type", BluePrintConstants.DATA_TYPE_STRING,
                true, "") {
            defaultValue("SQL".asJsonPrimitive())
            constrain {
                validValues(arrayListOf("SQL".asJsonPrimitive(), "PLSQL".asJsonPrimitive()))
            }
        }
        property("endpoint-selector", BluePrintConstants.DATA_TYPE_STRING,
                false, "")
        property("query", BluePrintConstants.DATA_TYPE_STRING,
                true, "")
        property("input-key-mapping", BluePrintConstants.DATA_TYPE_MAP,
                true, "") {
            entrySchema(BluePrintConstants.DATA_TYPE_STRING)
        }
        property("output-key-mapping", BluePrintConstants.DATA_TYPE_MAP,
                false, "") {
            entrySchema(BluePrintConstants.DATA_TYPE_STRING)
        }
        property("key-dependencies", BluePrintConstants.DATA_TYPE_LIST,
                true, "Resource Resolution dependency dictionary names.") {
            entrySchema(BluePrintConstants.DATA_TYPE_STRING)
        }
    }
}

fun BluePrintTypes.nodeTypeSourceRest(): NodeType {
    return nodeType(id = "source-rest", version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
            derivedFrom = BluePrintConstants.MODEL_TYPE_NODE_RESOURCE_SOURCE,
            description = "This is Rest Resource Source Node Type") {
        property("type", BluePrintConstants.DATA_TYPE_STRING,
                true, "") {
            defaultValue("JSON".asJsonPrimitive())
            constrain {
                validValues(arrayListOf("JSON".asJsonPrimitive(), "XML".asJsonPrimitive()))
            }
        }
        property("verb", BluePrintConstants.DATA_TYPE_STRING,
                true, "") {
            defaultValue("GET".asJsonPrimitive())
            constrain {
                validValues(arrayListOf("GET".asJsonPrimitive(), "POST".asJsonPrimitive(),
                        "DELETE".asJsonPrimitive(), "PUT".asJsonPrimitive()))
            }
        }
        property("payload", BluePrintConstants.DATA_TYPE_STRING,
                false, ""){
            defaultValue("".asJsonPrimitive())
        }
        property("endpoint-selector", BluePrintConstants.DATA_TYPE_STRING,
                false, "")
        property("url-path", BluePrintConstants.DATA_TYPE_STRING,
                true, "")
        property("path", BluePrintConstants.DATA_TYPE_STRING,
                true, "")
        property("expression-type", BluePrintConstants.DATA_TYPE_STRING,
                false, "") {
            defaultValue("JSON_PATH".asJsonPrimitive())
            constrain {
                validValues(arrayListOf("JSON_PATH".asJsonPrimitive(), "JSON_POINTER".asJsonPrimitive()))
            }
        }
        property("input-key-mapping", BluePrintConstants.DATA_TYPE_MAP,
                true, "") {
            entrySchema(BluePrintConstants.DATA_TYPE_STRING)
        }
        property("output-key-mapping", BluePrintConstants.DATA_TYPE_MAP,
                false, "") {
            entrySchema(BluePrintConstants.DATA_TYPE_STRING)
        }
        property("key-dependencies", BluePrintConstants.DATA_TYPE_LIST,
                true, "Resource Resolution dependency dictionary names.") {
            entrySchema(BluePrintConstants.DATA_TYPE_STRING)
        }
    }
}

fun BluePrintTypes.nodeTypeSourceCapability(): NodeType {
    return nodeType(id = "source-capability", version = BluePrintConstants.DEFAULT_VERSION_NUMBER,
            derivedFrom = BluePrintConstants.MODEL_TYPE_NODE_RESOURCE_SOURCE,
            description = "This is Component Resource Source Node Type") {
        property(ComponentScriptExecutor.INPUT_SCRIPT_TYPE, BluePrintConstants.DATA_TYPE_STRING,
                true, "Request Id, Unique Id for the request.") {
            defaultValue(BluePrintConstants.SCRIPT_KOTLIN)
            constrain {
                validValues(arrayListOf(BluePrintConstants.SCRIPT_KOTLIN.asJsonPrimitive(),
                        BluePrintConstants.SCRIPT_INTERNAL.asJsonPrimitive(),
                        BluePrintConstants.SCRIPT_JYTHON.asJsonPrimitive()))
            }
        }
        property(ComponentScriptExecutor.INPUT_SCRIPT_CLASS_REFERENCE, BluePrintConstants.DATA_TYPE_STRING,
                true, "Kotlin Script class name or jython script name.")
        property(ComponentScriptExecutor.INPUT_DYNAMIC_PROPERTIES, BluePrintConstants.DATA_TYPE_JSON,
                false, "Dynamic Json Content or DSL Json reference.")
        property("key-dependencies", BluePrintConstants.DATA_TYPE_LIST,
                true, "Resource Resolution dependency dictionary names.") {
            entrySchema(BluePrintConstants.DATA_TYPE_STRING)
        }
    }
}