package org.onap.ccsdk.cds.blueprintsprocessor.error.catalog

open class ErrorCatalogProperties {
    lateinit var type: String
    lateinit var applicationId: String
}

open class ErrorCatalogDataBaseProperties : ErrorCatalogProperties() {
    lateinit var dbType: String
    lateinit var url: String
    lateinit var username: String
    lateinit var password: String
}
