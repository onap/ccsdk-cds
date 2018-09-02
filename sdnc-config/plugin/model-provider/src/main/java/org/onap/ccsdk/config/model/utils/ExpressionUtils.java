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

package org.onap.ccsdk.config.model.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.data.PropertyDefinition;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ExpressionUtils {
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ExpressionUtils.class);
    
    private final SvcLogicContext context;
    private final Map<String, String> inParams;
    
    public ExpressionUtils(final SvcLogicContext context, final Map<String, String> inParams) {
        this.context = context;
        this.inParams = inParams;
    }
    
    public void populatePropertAssignmentsWithPrefix(String prefix, Map<String, PropertyDefinition> typeProperties,
            Map<String, Object> templateProperties) throws IOException {
        if (typeProperties != null && templateProperties != null) {
            ObjectMapper mapper = new ObjectMapper();
            String templatejsonContent = mapper.writeValueAsString(templateProperties);
            logger.debug("Expression inputs ({}).", templatejsonContent);
            
            JsonNode rootArray = mapper.readTree(templatejsonContent);
            processJsonExpression(rootArray);
            if (rootArray != null) {
                Map<String, String> prefixParams = new HashMap<>();
                TransformationUtils.convertJson2RootProperties(prefixParams, rootArray);
                logger.info("Resolved inputs ({}).", rootArray);
                prefixParams.forEach((key, value) -> {
                    this.inParams.put(prefix + "." + key, value);
                });
            }
        }
    }
    
    public void populatePropertAssignments(Map<String, PropertyDefinition> typeProperties,
            Map<String, Object> templateProperties) throws IOException {
        if (typeProperties != null && templateProperties != null) {
            ObjectMapper mapper = new ObjectMapper();
            String templatejsonContent = mapper.writeValueAsString(templateProperties);
            logger.info("Expression inputs ({}).", templatejsonContent);
            
            JsonNode rootArray = mapper.readTree(templatejsonContent);
            processJsonExpression(rootArray);
            TransformationUtils.convertJson2RootProperties(this.inParams, rootArray);
            logger.info("Resolved inputs ({}).", rootArray);
            
        }
    }
    
    public void populateOutPropertAssignments(Map<String, PropertyDefinition> typeProperties,
            Map<String, Object> templateProperties) throws IOException {
        if (typeProperties != null && templateProperties != null) {
            ObjectMapper mapper = new ObjectMapper();
            String templatejsonContent = mapper.writeValueAsString(templateProperties);
            logger.info("Expression outputs ({}).", templatejsonContent);
            
            JsonNode rootArray = mapper.readTree(templatejsonContent);
            processJsonExpression(rootArray);
            logger.info("Resolved outputs ({}).", rootArray);
        }
    }
    
    public void processJsonExpression(JsonNode rootArray) throws IOException {
        if (rootArray != null) {
            Iterator<Map.Entry<String, JsonNode>> fields = rootArray.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                processJsonNode(rootArray, entry.getKey(), entry.getValue());
            }
        }
    }
    
    private void processJsonNode(JsonNode parentNode, String nodeName, JsonNode node) throws IOException {
        if (node == null) {
            // Do Nothing
        } else if (node.isArray()) {
            String[] a = new String[node.size()];
            
            for (int i = 0; i < a.length; i++) {
                processJsonNode(null, null, node.get(i));
            }
        } else if (node.isObject()) {
            
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                processJsonNode(node, entry.getKey(), entry.getValue());
            }
        } else if (node.isTextual()) {
            boolean expression = checkExpressionContent(node.asText());
            if (expression) {
                processExpressionContent(parentNode, nodeName, node);
            }
        }
    }
    
    private boolean checkExpressionContent(String content) {
        return (StringUtils.isNotBlank(content) && (content.contains(ConfigModelConstant.EXPRESSION_GET_INPUT)
                || content.contains(ConfigModelConstant.EXPRESSION_GET_ATTRIBUTE)
                || content.contains(ConfigModelConstant.EXPRESSION_SET_VALUE)));
    }
    
    @SuppressWarnings("squid:S3776")
    private void processExpressionContent(JsonNode parentNode, String nodeName, JsonNode node) throws IOException {
        
        if (node != null && StringUtils.isNotBlank(node.asText())) {
            String content = node.asText();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> expressionMap = mapper.readValue(content, Map.class);
            boolean isExpressionNode = false;
            
            if (expressionMap != null) {
                String expressionValue = null;
                String expressionKey = null;
                
                if (expressionMap.containsKey(ConfigModelConstant.EXPRESSION_GET_INPUT)) {
                    isExpressionNode = true;
                    expressionKey = expressionMap.get(ConfigModelConstant.EXPRESSION_GET_INPUT);
                    expressionValue = resolveGetInputExpression(expressionKey, context);
                    if (expressionValue == null) {
                        expressionValue = resolveGetInputExpression("inputs." + expressionKey + ".default", context);
                    }
                } else if (expressionMap.containsKey(ConfigModelConstant.EXPRESSION_GET_ATTRIBUTE)) {
                    isExpressionNode = true;
                    expressionValue =
                            context.getAttribute(expressionMap.get(ConfigModelConstant.EXPRESSION_GET_ATTRIBUTE));
                } else if (expressionMap.containsKey(ConfigModelConstant.EXPRESSION_SET_VALUE)) {
                    isExpressionNode = true;
                    expressionKey = expressionMap.get(ConfigModelConstant.EXPRESSION_SET_VALUE);
                    expressionValue = context.getAttribute(expressionKey);
                    
                    if (StringUtils.isNotBlank(expressionKey)) {
                        context.setAttribute(expressionKey, expressionValue);
                    }
                }
                
                if (isExpressionNode && expressionValue == null) {
                    ((ObjectNode) parentNode).put(nodeName, "");
                }
                if (StringUtils.isNotBlank(expressionValue)) {
                    if (expressionValue.trim().startsWith("[") || expressionValue.trim().startsWith("{")) {
                        JsonNode valueNode = mapper.readTree(expressionValue);
                        ((ObjectNode) parentNode).put(nodeName, valueNode);
                    } else {
                        ((ObjectNode) parentNode).put(nodeName, expressionValue);
                    }
                }
                logger.debug("expression ({}), expression key ({}), expression Value ({})", expressionMap,
                        expressionKey, expressionValue);
            }
        }
    }
    
    private String resolveGetInputExpression(String key, final SvcLogicContext context) {
        if (StringUtils.isNotBlank(key) && context != null) {
            return context.getAttribute(key);
        }
        return null;
    }
    
}
