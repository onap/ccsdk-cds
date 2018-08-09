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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
/**
 *
 * SourceDb
 * @author Brinda Santh
 */
public class SourceDb {
    @JsonProperty(value = "base", required = true)
    private String base;
    @JsonProperty(value = "type", required = true)
    private String type; // SQL | PLSQL
    @JsonProperty(value = "query", required = true)
    private String query;

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

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
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
