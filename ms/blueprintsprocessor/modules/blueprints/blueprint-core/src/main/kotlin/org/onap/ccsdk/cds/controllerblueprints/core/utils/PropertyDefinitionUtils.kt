package org.onap.ccsdk.cds.controllerblueprints.core.utils

import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants.LOG_PROTECT
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition

class PropertyDefinitionUtils {
    companion object {
        fun hasLogProtect(metadata: MutableMap<String, String>?) = metadata?.get(LOG_PROTECT)
                ?.let {
                    when (it.toLowerCase()) {
                        "true" -> true
                        "yes" -> true
                        "y" -> true
                        else -> false
                    }
                } ?: false

        fun hasLogProtect(propertyDefinition: PropertyDefinition?) = propertyDefinition
                ?.let { p -> hasLogProtect(p.metadata) } ?: false
    }
}
