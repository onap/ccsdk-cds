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

package org.onap.ccsdk.apps.controllerblueprints;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Header;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SwaggerConfig
 *
 * @author Brinda Santh 8/13/2018
 */
@Deprecated
//@Configuration
//@EnableSwagger2
@SuppressWarnings("unused")
public class SwaggerConfig {
    @Value("${appVersion}")
    private String appVersion;
    @Value("${swagger.contact.name}")
    private String contactName;
    @Value("${swagger.contact.url}")
    private String contactUrl;
    @Value("${swagger.contact.email}")
    private String contactEmail;
    private String stringModelRef = "string";

    @Bean
    @SuppressWarnings("unused")
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .globalResponseMessage(RequestMethod.GET, getDefaultGetResponseMessages())
                .globalResponseMessage(RequestMethod.POST, getDefaultPostResponseMessages())
                .globalResponseMessage(RequestMethod.PUT, getDefaultPutResponseMessages())
                .globalResponseMessage(RequestMethod.DELETE, getDefaultDeleteResponseMessages())
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "Controller Blueprints API",
                "Controller blueprints API for VNF Self Service.",
                appVersion,
                "Terms of service",
                new Contact(contactName, contactUrl, contactEmail),
                "Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0", Collections.emptyList());
    }

    private List<ResponseMessage> getDefaultGetResponseMessages() {
        List<ResponseMessage> defaultResponseMessages = Lists.newArrayList();
        Map<String, Header> defaultHeaders = getDefaultResponseHeaders();
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.OK, defaultHeaders));
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.BAD_REQUEST, defaultHeaders));
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.UNAUTHORIZED, defaultHeaders));
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.FORBIDDEN, defaultHeaders));
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.NOT_FOUND, defaultHeaders));
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR, defaultHeaders));
        return defaultResponseMessages;
    }

    private List<ResponseMessage> getDefaultPostResponseMessages() {
        List<ResponseMessage> defaultResponseMessages = Lists.newArrayList();
        Map<String, Header> defaultHeaders = getDefaultResponseHeaders();
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.OK, defaultHeaders));
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.CREATED, defaultHeaders));
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.BAD_REQUEST, defaultHeaders));
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.UNAUTHORIZED, defaultHeaders));
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.FORBIDDEN, defaultHeaders));
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR, defaultHeaders));
        return defaultResponseMessages;
    }

    private List<ResponseMessage> getDefaultPutResponseMessages() {
        List<ResponseMessage> defaultResponseMessages = Lists.newArrayList();
        Map<String, Header> defaultHeaders = getDefaultResponseHeaders();
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.OK, defaultHeaders));
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.BAD_REQUEST, defaultHeaders));
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.UNAUTHORIZED, defaultHeaders));
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.FORBIDDEN, defaultHeaders));
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR, defaultHeaders));
        return defaultResponseMessages;
    }

    private List<ResponseMessage> getDefaultDeleteResponseMessages() {
        List<ResponseMessage> defaultResponseMessages = Lists.newArrayList();
        Map<String, Header> defaultHeaders = getDefaultResponseHeaders();
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.OK, defaultHeaders));
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.BAD_REQUEST, defaultHeaders));
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.UNAUTHORIZED, defaultHeaders));
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.FORBIDDEN, defaultHeaders));
        defaultResponseMessages.add(getResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR, defaultHeaders));
        return defaultResponseMessages;
    }

    private ResponseMessage getResponseBuilder(@NotNull HttpStatus httpStatus, Map<String, Header> defaultHeaders) {
        ResponseMessageBuilder responseMessageBuilder = new ResponseMessageBuilder();
        responseMessageBuilder.code(httpStatus.value())
                .message(httpStatus.getReasonPhrase())
                .headersWithDescription(defaultHeaders)
                .build();
        return responseMessageBuilder.build();
    }

    private Map<String, Header> getDefaultResponseHeaders() {
        Map<String, Header> defaultHeaders = new HashMap<>();
        defaultHeaders.put(BluePrintConstants.RESPONSE_HEADER_TRANSACTION_ID,
                new Header(BluePrintConstants.RESPONSE_HEADER_TRANSACTION_ID, "Transaction Id", new ModelRef(stringModelRef)));
        defaultHeaders.put(BluePrintConstants.RESPONSE_HEADER_LATEST_VERSION,
                new Header(BluePrintConstants.RESPONSE_HEADER_LATEST_VERSION, "API Latest Version", new ModelRef(stringModelRef)));
        defaultHeaders.put(BluePrintConstants.RESPONSE_HEADER_MINOR_VERSION,
                new Header(BluePrintConstants.RESPONSE_HEADER_MINOR_VERSION, "API Minor Version", new ModelRef(stringModelRef)));
        defaultHeaders.put(BluePrintConstants.RESPONSE_HEADER_PATCH_VERSION,
                new Header(BluePrintConstants.RESPONSE_HEADER_PATCH_VERSION, "API Patch Version", new ModelRef(stringModelRef)));
        return defaultHeaders;
    }
}
