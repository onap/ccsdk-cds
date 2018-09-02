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

package org.onap.ccsdk.config.model.data;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ServiceTemplate.java Purpose: Provide ServiceTemplate TOSCO Model POJO bean.
 *
 * @version 1.0
 */
public class ServiceTemplate {
    @JsonIgnore
    private String id;
    @JsonProperty("tosca_definitions_version")
    private String toscaDefinitionsVersion;
    private Map<String, String> metadata;
    private String description;
    @JsonProperty("dsl_definitions")
    private Map<String, Object> dslDefinitions;
    @JsonProperty("topology_template")
    private TopologyTemplate topologyTemplate;
    @JsonProperty("artifact_types")
    private Map<String, ArtifactType> artifactTypes;
    @JsonProperty("node_types")
    private Map<String, NodeType> nodeTypes;
    @JsonProperty("data_types")
    private Map<String, DataType> dataTypes;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getToscaDefinitionsVersion() {
        return toscaDefinitionsVersion;
    }
    
    public void setToscaDefinitionsVersion(String toscaDefinitionsVersion) {
        this.toscaDefinitionsVersion = toscaDefinitionsVersion;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Map<String, Object> getDslDefinitions() {
        return dslDefinitions;
    }
    
    public void setDslDefinitions(Map<String, Object> dslDefinitions) {
        this.dslDefinitions = dslDefinitions;
    }
    
    public TopologyTemplate getTopologyTemplate() {
        return topologyTemplate;
    }
    
    public void setTopologyTemplate(TopologyTemplate topologyTemplate) {
        this.topologyTemplate = topologyTemplate;
    }
    
    public Map<String, NodeType> getNodeTypes() {
        return nodeTypes;
    }
    
    public void setNodeTypes(Map<String, NodeType> nodeTypes) {
        this.nodeTypes = nodeTypes;
    }
    
    public Map<String, DataType> getDataTypes() {
        return dataTypes;
    }
    
    public void setDataTypes(Map<String, DataType> dataTypes) {
        this.dataTypes = dataTypes;
    }
    
    public Map<String, ArtifactType> getArtifactTypes() {
        return artifactTypes;
    }
    
    public void setArtifactTypes(Map<String, ArtifactType> artifactTypes) {
        this.artifactTypes = artifactTypes;
    }
}
