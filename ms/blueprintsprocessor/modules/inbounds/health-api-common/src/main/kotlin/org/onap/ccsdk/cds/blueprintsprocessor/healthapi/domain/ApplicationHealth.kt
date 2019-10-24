package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.Status

open class ApplicationHealth(val status: Status?,val details: Map<String, Any>?){

    constructor() : this(null,null)
}
