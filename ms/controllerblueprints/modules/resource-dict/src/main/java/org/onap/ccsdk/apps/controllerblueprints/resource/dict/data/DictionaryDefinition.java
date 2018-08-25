/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;
import java.util.Map;
/**
 *
 * DictionaryDefinition.java Purpose:
 * @author Brinda Santh
 */
@Deprecated
public class DictionaryDefinition {
    @JsonProperty(value = "name", required = true)
    private String name;

    @JsonProperty(value = "description")
    private String description;

    @JsonProperty(value = "valid-values")
    private String validValues;

    @JsonProperty(value = "sample-value")
    private String sampleValue;

    private String tags;

    @JsonProperty(value = "updated-by")
    private String updatedBy;

    @JsonProperty(value = "resource-type", required = true)
    private String resourceType;

    @JsonProperty(value = "resource-path", required = true)
    private String resourcePath;

    @JsonProperty(value = "data-type", required = true)
    private String dataType;

    @JsonProperty("entry-schema")
    private String entrySchema;

    @JsonProperty(value = "default")
    private Object defaultValue;

    @JsonProperty(value = "source", required = true)
    @JsonDeserialize(using = SourceDeserializer.class, keyAs = String.class, contentAs = ResourceSource.class)
    private Map<String, ResourceSource> source;

    @JsonProperty("candidate-dependency")
    private Map<String, DictionaryDependency> dependency;

    @JsonProperty("decryption-rules")
    private List<DecryptionRule> decryptionRules;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getValidValues() {
        return validValues;
    }

    public void setValidValues(String validValues) {
        this.validValues = validValues;
    }

    public String getSampleValue() {
        return sampleValue;
    }

    public void setSampleValue(String sampleValue) {
        this.sampleValue = sampleValue;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getEntrySchema() {
        return entrySchema;
    }

    public void setEntrySchema(String entrySchema) {
        this.entrySchema = entrySchema;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Map<String, ResourceSource> getSource() {
        return source;
    }

    public void setSource(Map<String, ResourceSource> source) {
        this.source = source;
    }

    public Map<String, DictionaryDependency> getDependency() {
        return dependency;
    }

    public void setDependency(Map<String, DictionaryDependency> dependency) {
        this.dependency = dependency;
    }

    public List<DecryptionRule> getDecryptionRules() {
        return decryptionRules;
    }

    public void setDecryptionRules(List<DecryptionRule> decryptionRules) {
        this.decryptionRules = decryptionRules;
    }

}
