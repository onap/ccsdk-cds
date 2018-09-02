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

import static com.jayway.jsonpath.JsonPath.using;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

@SuppressWarnings("squid:S1118")
public class JsonParserUtils {
    
    public static final Configuration JACKSON_JSON_NODE_CONFIGURATION = Configuration.builder()
            .mappingProvider(new JacksonMappingProvider()).jsonProvider(new JacksonJsonNodeJsonProvider()).build();
    
    public static final Configuration PATH_CONFIGURATION = Configuration.builder().options(Option.AS_PATH_LIST).build();
    
    public static List<String> paths(String jsonContent, String expression) {
        return using(PATH_CONFIGURATION).parse(jsonContent).read(expression);
    }
    
    public static List<String> paths(JsonNode jsonNode, String expression) {
        return paths(jsonNode.toString(), expression);
    }
    
    public static JsonNode parse(String jsonContent, String expression) {
        return using(JACKSON_JSON_NODE_CONFIGURATION).parse(jsonContent).read(expression);
    }
    
    public static JsonNode parse(JsonNode jsonNode, String expression) {
        return parse(jsonNode.toString(), expression);
    }
    
    public static JsonNode parseNSet(String jsonContent, String expression, JsonNode value) {
        return using(JACKSON_JSON_NODE_CONFIGURATION).parse(jsonContent).set(expression, value).json();
    }
    
    public static JsonNode parseNSet(JsonNode jsonNode, String expression, JsonNode valueNode) {
        return parseNSet(jsonNode.toString(), expression, valueNode);
    }
}
