/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
 *  Modifications Copyright © 2018 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.web.bind.annotation.RequestMapping
import io.swagger.v3.oas.annotations.Operation as OperationAnnotation

/**
 * SwaggerConfig
 *
 * @author Brinda Santh
 */
@Configuration
open class SwaggerConfig {

    @Bean
    open fun api(buildProperties: BuildProperties): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("CDS Blueprint Processor API Reference")
                    .description(
                        "Shows all resources and endpoints which CDS BP processor currently provides with sample " +
                            "requests/responses, parameter description and other information."
                    )
                    .version(buildProperties.version)
                    .termsOfService("https://www.onap.org/")
                    .contact(Contact().name("ONAP Community").url("https://www.onap.org/").email("onap-discuss@lists.onap.org"))
                    .license(License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0.html"))
            )
            .components(Components().addSecuritySchemes(BASIC_AUTH, SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
            .addSecurityItem(SecurityRequirement().addList(BASIC_AUTH))
    }

    @Bean
    open fun operationIdCustomizer() = OperationCustomizer { operation, handlerMethod ->
        if (handlerMethod.getMethodAnnotation(OperationAnnotation::class.java)?.operationId.isNullOrEmpty()) {
            val requestMethod = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.method, RequestMapping::class.java)!!
                .method.first()
            val beanType = handlerMethod.beanType
            operation.operationId = "${beanType.simpleName}_${handlerMethod.method.name}_$requestMethod.${beanType.packageName}"
        }
        operation
    }

    companion object {

        private const val BASIC_AUTH = "basicAuth"
    }
}
