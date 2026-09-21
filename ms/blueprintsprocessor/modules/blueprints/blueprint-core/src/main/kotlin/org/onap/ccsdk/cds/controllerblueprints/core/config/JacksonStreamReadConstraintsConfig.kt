package org.onap.ccsdk.cds.controllerblueprints.core.config

import com.fasterxml.jackson.core.StreamReadConstraints
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(JacksonStreamReadConstraintsProperties::class)
open class JacksonStreamReadConstraintsConfig(
    private val properties: JacksonStreamReadConstraintsProperties
) {
    @PostConstruct
    fun configureJackson() {

        if (
            properties.maxStringLength == null &&
            properties.maxDocumentLength == null &&
            properties.maxNameLength == null &&
            properties.maxTokenCount == null &&
            properties.maxNestingDepth == null &&
            properties.maxNumberLength == null
        ) {
            return
        }

        val defaults = StreamReadConstraints.defaults()

        StreamReadConstraints.overrideDefaultStreamReadConstraints(
            StreamReadConstraints.builder()
                .maxStringLength(
                    properties.maxStringLength ?: defaults.maxStringLength
                )
                .maxNumberLength(
                    properties.maxNumberLength ?: defaults.maxNumberLength
                )
                .maxNameLength(
                    properties.maxNameLength ?: defaults.maxNameLength
                )
                .maxNestingDepth(
                    properties.maxNestingDepth ?: defaults.maxNestingDepth
                )
                .maxDocumentLength(
                    properties.maxDocumentLength ?: defaults.maxDocumentLength
                )
                .maxTokenCount(
                    properties.maxTokenCount ?: defaults.maxTokenCount
                )
                .build()
        )
    }
}
