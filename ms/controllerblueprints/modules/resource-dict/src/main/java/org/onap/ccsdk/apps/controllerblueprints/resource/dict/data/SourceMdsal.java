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

package org.onap.ccsdk.apps.controllerblueprints.resource.dict.data;
/**
 *
 * SourceMdsal
 * @author Brinda Santh
 */
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class SourceMdsal implements ResourceSource {

    @JsonProperty(value = "base", required = true)
    private String base;

    @JsonProperty(value = "type", required = true)
    private String type; // XML | JSON

    @JsonProperty(value = "url-path", required = true)
    private String urlPath;

    @JsonProperty(value = "path", required = true)
    private String path;

    @JsonProperty("input-key-mapping")
    private Map<String, String> inputKeyMapping;

    @JsonProperty("output-key-mapping")
    private Map<String, String> outputKeyMapping;

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getInputKeyMapping() {
        return inputKeyMapping;
    }

    public void setInputKeyMapping(Map<String, String> inputKeyMapping) {
        this.inputKeyMapping = inputKeyMapping;
    }

    public Map<String, String> getOutputKeyMapping() {
        return outputKeyMapping;
    }

    public void setOutputKeyMapping(Map<String, String> outputKeyMapping) {
        this.outputKeyMapping = outputKeyMapping;
    }


}
