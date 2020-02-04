package org.onap.ccsdk.cds.blueprintsprocessor.error.catalogue

open class ErrorCatalogueProperties {
    lateinit var type: String
    lateinit var applicationId: String
}

open class ErrorCatalogueDataBaseProperties: ErrorCatalogueProperties() {
    lateinit var dbType: String
    lateinit var url: String
    lateinit var username: String
    lateinit var password: String
}