package org.onap.ccsdk.cds.controllerblueprints.core.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jackson.stream-read-constraints")
data class JacksonStreamReadConstraintsProperties(
    var maxStringLength: Int? = null,
    var maxNumberLength: Int? = null,
    var maxNameLength: Int? = null,
    var maxNestingDepth: Int? = null,
    var maxDocumentLength: Long? = null,
    var maxTokenCount: Long? = null,
)
