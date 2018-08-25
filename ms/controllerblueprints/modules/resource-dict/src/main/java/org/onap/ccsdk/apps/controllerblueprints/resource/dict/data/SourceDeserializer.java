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

package org.onap.ccsdk.apps.controllerblueprints.resource.dict.data;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
@Deprecated
public class SourceDeserializer extends JsonDeserializer<Map<String, ResourceSource>> {

    private static final Logger log = LoggerFactory.getLogger(SourceDeserializer.class);

    private Class<?> keyAs;

    private Class<?> contentAs;

    private static Map<String, Class<? extends ResourceSource>> registry = new HashMap<String, Class<? extends ResourceSource>>();

    public static void registerSource(String uniqueAttribute, Class<? extends ResourceSource> sourceClass) {
        registry.put(uniqueAttribute, sourceClass);
    }

    @Override
    public Map<String, ResourceSource> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        ObjectNode root = (ObjectNode) mapper.readTree(p);
        Map<String, ResourceSource> sources = new HashMap();
        root.fields().forEachRemaining((node) -> {
            String key = node.getKey();
            JsonNode valueNode = node.getValue();
            Preconditions.checkArgument(StringUtils.isNotBlank(key), "missing source key");
            Preconditions.checkArgument(registry.containsKey(key), key +" source not registered");
            if (StringUtils.isNotBlank(key) && registry.containsKey(key)) {
                Class<? extends ResourceSource> sourceClass = registry.get(key);
                ResourceSource resourceSource = JacksonUtils.readValue(valueNode.toString(), sourceClass);
                sources.put(key, resourceSource);
            }
        });
        return sources;
    }
}
