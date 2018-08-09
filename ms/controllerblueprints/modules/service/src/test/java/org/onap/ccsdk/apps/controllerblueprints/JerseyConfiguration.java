/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.apps.controllerblueprints;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.onap.ccsdk.apps.controllerblueprints.service.common.ServiceExceptionMapper;
import org.onap.ccsdk.apps.controllerblueprints.service.rs.ConfigModelRestImpl;
import org.onap.ccsdk.apps.controllerblueprints.service.rs.ModelTypeRestImpl;
import org.onap.ccsdk.apps.controllerblueprints.service.rs.ResourceDictionaryRestImpl;
import org.onap.ccsdk.apps.controllerblueprints.service.rs.ServiceTemplateRestImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;


@Component
public class JerseyConfiguration extends ResourceConfig {
    private static final Logger log = Logger.getLogger(JerseyConfiguration.class.getName());

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        return objectMapper;
    }

    @Autowired
    public JerseyConfiguration() {
        register(ConfigModelRestImpl.class);
        register(ModelTypeRestImpl.class);
        register(ResourceDictionaryRestImpl.class);
        register(ServiceTemplateRestImpl.class);
        // Exception Mapping
        register(ServiceExceptionMapper.class);
        property(ServletProperties.FILTER_FORWARD_ON_404, true);
        register(new LoggingFeature(log));
    }


}