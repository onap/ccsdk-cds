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

package org.onap.ccsdk.cds.controllerblueprints.service.common;

import io.swagger.models.*;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.*;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants;
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes;
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition;
import org.onap.ccsdk.cds.controllerblueprints.core.data.ServiceTemplate;

import java.util.*;

/**
 * SwaggerGenerator.java Purpose: Provide Service to generate service template input schema definition and Sample Json
 * generation.
 *
 * @author Brinda Santh
 * @version 1.0
 */
@Deprecated
public class SwaggerGenerator {

    private ServiceTemplate serviceTemplate;
    public static final String INPUTS="inputs";

    /**
     * This is a SwaggerGenerator constructor
     */
    public SwaggerGenerator(ServiceTemplate serviceTemplate) {
        this.serviceTemplate = serviceTemplate;
    }

    /**
     * This is a generateSwagger
     *
     * @return String
     */
    public String generateSwagger() {
        
        Swagger swagger = new Swagger().info(getInfo());

        swagger.setPaths(getPaths());
        swagger.setDefinitions(getDefinition());


        return swagger.toString();
    }

    private Info getInfo() {
        Info info = new Info();
        Contact contact = new Contact();
        contact.setName(serviceTemplate.getMetadata().get(BluePrintConstants.METADATA_TEMPLATE_AUTHOR));
        info.setContact(contact);
        info.setTitle(serviceTemplate.getMetadata().get(BluePrintConstants.METADATA_TEMPLATE_NAME));
        info.setDescription(serviceTemplate.getDescription());
        info.setVersion(serviceTemplate.getMetadata().get(BluePrintConstants.METADATA_TEMPLATE_VERSION));
        return info;
    }

    private Map<String, Path> getPaths() {
        Map<String, Path> paths = new HashMap<>();
        Path path = new Path();
        Operation post = new Operation();
        post.setOperationId("configure");
        post.setConsumes(Arrays.asList("application/json", "application/xml"));
        post.setProduces(Arrays.asList("application/json", "application/xml"));
        List<Parameter> parameters = new ArrayList<>();
        Parameter in = new BodyParameter().schema(new RefModel("#/definitions/inputs"));
        in.setRequired(true);
        in.setName(INPUTS);
        parameters.add(in);
        post.setParameters(parameters);

        Map<String, Response> responses = new HashMap<>();
        Response response = new Response().description("Success");
        responses.put("200", response);

        Response failureResponse = new Response().description("Failure");
        responses.put("400", failureResponse);
        post.setResponses(responses);

        path.setPost(post);
        paths.put("/operations/config-selfservice-api:configure", path);
        return paths;
    }

    private Map<String, Model> getDefinition() {
        Map<String, Model> models = new HashMap<>();

        ModelImpl inputmodel = new ModelImpl();
        inputmodel.setTitle(INPUTS);
        serviceTemplate.getTopologyTemplate().getInputs().forEach((propertyName, property) -> {
            Property defProperty = getPropery(propertyName, property);
            inputmodel.property(propertyName, defProperty);
        });
        models.put(INPUTS, inputmodel);

        if (MapUtils.isNotEmpty(serviceTemplate.getDataTypes())) {
            serviceTemplate.getDataTypes().forEach((name, dataType) -> {
                ModelImpl model = new ModelImpl();
                model.setDescription(dataType.getDescription());
                if (dataType != null && MapUtils.isNotEmpty(dataType.getProperties())) {

                    dataType.getProperties().forEach((propertyName, property) -> {
                        Property defProperty = getPropery(propertyName, property);
                        model.addProperty(propertyName, defProperty);
                    });
                }
                models.put(name, model);
            });
        }
        return models;

    }

    private Property getPropery(String name, PropertyDefinition propertyDefinition) {
        Property defProperty = null;

        if (BluePrintTypes.validPrimitiveTypes().contains(propertyDefinition.getType())) {
            if (BluePrintConstants.DATA_TYPE_BOOLEAN.equals(propertyDefinition.getType())) {
                defProperty = new BooleanProperty();
            } else if (BluePrintConstants.DATA_TYPE_INTEGER.equals(propertyDefinition.getType())) {
                StringProperty stringProperty = new StringProperty();
                stringProperty.setType("integer");
                defProperty = stringProperty;
            } else if (BluePrintConstants.DATA_TYPE_FLOAT.equals(propertyDefinition.getType())) {
                StringProperty stringProperty = new StringProperty();
                stringProperty.setFormat("float");
                defProperty = stringProperty;
            } else if (BluePrintConstants.DATA_TYPE_TIMESTAMP.equals(propertyDefinition.getType())) {
                DateTimeProperty dateTimeProperty = new DateTimeProperty();
                dateTimeProperty.setFormat("date-time");
                defProperty = dateTimeProperty;
            } else {
                defProperty = new StringProperty();
            }
        } else if (BluePrintTypes.validCollectionTypes().contains(propertyDefinition.getType())) {
            ArrayProperty arrayProperty = new ArrayProperty();
            if (propertyDefinition.getEntrySchema() != null) {
                String entrySchema = propertyDefinition.getEntrySchema().getType();
                if (!BluePrintTypes.validPrimitiveTypes().contains(entrySchema)) {
                    Property innerType = new RefProperty("#/definitions/" + entrySchema);
                    arrayProperty.setItems(innerType);
                } else {
                    Property innerType = new StringProperty();
                    arrayProperty.setItems(innerType);
                }
                defProperty = arrayProperty;
            }

        } else {
            defProperty = new RefProperty("#/definitions/" + propertyDefinition.getType());
        }
        defProperty.setName(name);
        if (propertyDefinition.getDefaultValue() != null) {
            defProperty.setDefault(String.valueOf(propertyDefinition.getDefaultValue()));
        }

        defProperty.setRequired(BooleanUtils.isTrue(propertyDefinition.getRequired()));
        defProperty.setDescription(propertyDefinition.getDescription());
        return defProperty;
    }


}
