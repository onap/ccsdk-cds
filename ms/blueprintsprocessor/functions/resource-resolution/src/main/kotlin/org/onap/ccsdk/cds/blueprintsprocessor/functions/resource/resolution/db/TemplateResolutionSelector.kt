package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db

interface TemplateResolutionSelector {
    fun getArtifactName(): String
    fun getResolutionKey(): String
}