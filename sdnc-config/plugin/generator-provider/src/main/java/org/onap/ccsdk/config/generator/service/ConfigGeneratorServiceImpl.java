/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.onap.ccsdk.config.generator.service;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.onap.ccsdk.config.data.adaptor.domain.ConfigResource;
import org.onap.ccsdk.config.data.adaptor.service.ConfigResourceService;
import org.onap.ccsdk.config.generator.data.ConfigGeneratorInfo;
import org.onap.ccsdk.config.generator.tool.CustomJsonNodeFactory;
import org.onap.ccsdk.config.model.utils.TransformationUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigGeneratorServiceImpl implements ConfigGeneratorService {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ConfigGeneratorServiceImpl.class);
    private static final String CLASS_NAME = "ConfigGeneratorServiceImpl";
    
    private ConfigResourceService configResourceService;
    
    public ConfigGeneratorServiceImpl(ConfigResourceService configResourceService) {
        logger.info("{} Constuctor Initated...", CLASS_NAME);
        this.configResourceService = configResourceService;
    }
    
    @Override
    public ConfigGeneratorInfo generateConfiguration(ConfigGeneratorInfo configGeneratorInfo) throws SvcLogicException {
        
        if (configGeneratorInfo != null && StringUtils.isNotBlank(configGeneratorInfo.getResourceId())
                && StringUtils.isNotBlank(configGeneratorInfo.getResourceType())
                && StringUtils.isNotBlank(configGeneratorInfo.getRecipeName())
                && StringUtils.isNotBlank(configGeneratorInfo.getTemplateName())
                && StringUtils.isNotBlank(configGeneratorInfo.getTemplateContent())) {
            
            ConfigResource configResourceQuery = new ConfigResource();
            configResourceQuery.setResourceId(configGeneratorInfo.getResourceId());
            configResourceQuery.setResourceType(configGeneratorInfo.getResourceType());
            configResourceQuery.setTemplateName(configGeneratorInfo.getTemplateName());
            
            List<ConfigResource> configResourceList = configResourceService.getConfigResource(configResourceQuery);
            
            if (CollectionUtils.isEmpty(configResourceList)) {
                throw new SvcLogicException("No Config Resource found");
            } else if (configResourceList.size() > 1) {
                throw new SvcLogicException("More than one Config Resource found for specified parameter for"
                        + " resourceId " + configGeneratorInfo.getResourceId() + ", resourceType "
                        + configGeneratorInfo.getResourceType() + ", recipeName " + configGeneratorInfo.getRecipeName()
                        + ", templateName " + configGeneratorInfo.getTemplateName());
            }
            
            ConfigResource configResource = configResourceList.get(0);
            
            if (configResource != null && StringUtils.isNotBlank(configResource.getResourceData())) {
                configGeneratorInfo.setResourceData(configResource.getResourceData());
                logger.debug("Retrieve ConfigResource Data : ({})", configResource.getResourceData());
                ConfigGeneratorInfo generatorInfo = generateConfiguration(configGeneratorInfo.getTemplateContent(),
                        configResource.getResourceData());
                if (generatorInfo != null) {
                    configGeneratorInfo.setMashedData(generatorInfo.getMashedData());
                    configGeneratorInfo.setMaskData(generatorInfo.getMaskData());
                }
            } else {
                throw new SvcLogicException(
                        "Failed to get the Resource Data for the Resource Id :" + configGeneratorInfo.getResourceId()
                                + " of template :" + configGeneratorInfo.getTemplateName());
            }
        }
        return configGeneratorInfo;
    }
    
    @Override
    public ConfigGeneratorInfo generateConfiguration(String templateContent, String templateData)
            throws SvcLogicException {
        return generateConfiguration(templateContent, templateData, true);
    }
    
    @Override
    public ConfigGeneratorInfo generateConfiguration(String templateContent, String templateData, boolean ignoreNull)
            throws SvcLogicException {
        ConfigGeneratorInfo configGeneratorInfo = null;
        try {
            if (StringUtils.isNotBlank(templateContent) && StringUtils.isNotBlank(templateData)) {
                configGeneratorInfo = new ConfigGeneratorInfo();
                
                Velocity.init();
                
                ObjectMapper mapper = new ObjectMapper();
                CustomJsonNodeFactory f = new CustomJsonNodeFactory();
                mapper.setNodeFactory(f);
                
                JsonNode jsonObj = mapper.readValue(templateData, JsonNode.class);
                if (ignoreNull) {
                    TransformationUtils.removeJsonNullNode(jsonObj);
                }
                
                VelocityContext context = new VelocityContext();
                context.put("StringUtils", org.apache.commons.lang3.StringUtils.class);
                context.put("BooleanUtils", org.apache.commons.lang3.BooleanUtils.class);
                
                Iterator<String> ii = jsonObj.fieldNames();
                while (ii.hasNext()) {
                    String key = ii.next();
                    JsonNode node = jsonObj.get(key);
                    logger.info("Adding key ({}) with value ({})", key, node);
                    context.put(key, node);
                }
                
                StringWriter writer = new StringWriter();
                Velocity.evaluate(context, writer, "TemplateData", templateContent);
                writer.flush();
                configGeneratorInfo.setMashedData(writer.toString());
            }
        } catch (Exception e) {
            logger.error("Failed to generate Configuration ({})", e.getMessage());
            throw new SvcLogicException(e.getMessage(), e);
        }
        return configGeneratorInfo;
    }
    
}
