package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain

import org.springframework.boot.actuate.health.Status

data class ApplicationHealth(val status: Status?, val details: Map<String, Any>?) {
    constructor() : this(null, HashMap())
}


