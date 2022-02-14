package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.query;

import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.K8sConnectionPluginConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod.GET

public class K8sPluginQueryApi(
        private val k8sConfiguration: K8sConnectionPluginConfiguration
) {
    private val log = LoggerFactory.getLogger(K8sPluginQueryApi::class.java)!!

    fun queryK8sResources(
            cloudRegion: String,
            kind: String,
            apiVersion: String,
            name: String? = null,
            namespace: String? = null,
            labels: Map<String, String>? = null
    ): K8sResourceStatus? {
        val rbQueryService = K8sQueryRestClient(k8sConfiguration)
        try {
            var path: String = "?CloudRegion=$cloudRegion&ApiVersion=$apiVersion&Kind=$kind"
            if (name != null)
                path = path.plus("&Name=$name")
            if (namespace != null)
                path = path.plus("&Namespace=$namespace")
            if (labels != null && labels.isNotEmpty()) {
                path = path.plus("&Labels=")
                for ((name, value) in labels)
                    path = path.plus("$name%3D$value,")
                path = path.trimEnd(',')
            }
            val result: BlueprintWebClientService.WebClientResponse<String> = rbQueryService.exchangeResource(
                    GET.name,
                    path,
                    ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sResourceStatus? = JacksonUtils.readValue(
                        result.body, K8sResourceStatus::class.java
                )
                parsedObject
            } else if (result.status == 500 && result.body.contains("Error finding master table"))
                null
            else
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BluePrintProcessorException("${e.message}")
        }
    }
}
