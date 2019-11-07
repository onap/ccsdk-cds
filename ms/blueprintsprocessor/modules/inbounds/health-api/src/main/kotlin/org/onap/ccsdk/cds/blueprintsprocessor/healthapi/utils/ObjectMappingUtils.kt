package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

@Service
class ObjectMappingUtils<T> {

    @Throws(java.io.IOException::class)
    open fun getObjectFromBody(body: String, mappingClass: Class<T>): T {
        return ObjectMapper().readValue(body, mappingClass) as T
    }
}
