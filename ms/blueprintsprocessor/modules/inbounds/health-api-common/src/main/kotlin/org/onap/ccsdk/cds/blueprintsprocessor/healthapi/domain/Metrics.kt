package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain

import java.util.*


data class Metrics(val names: ArrayList<Any>?) {
    constructor() : this(null)

    fun getName(): ArrayList<Any>? {
        return this.names
    }
}
