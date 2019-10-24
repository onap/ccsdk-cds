package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain


data class Metrics(val names: ArrayList<Any>?) {
    constructor() : this(ArrayList())
}
