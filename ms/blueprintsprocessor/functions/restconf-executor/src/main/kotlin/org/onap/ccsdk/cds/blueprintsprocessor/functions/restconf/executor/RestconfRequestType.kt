package org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor

enum class RestconfRequestType {
    PUT,
    PATCH,
    POST,
    GET,
    DELETE
}

enum class RestconfRequestDatastore {
    CONFIG,
    OPERATIONAL
}
