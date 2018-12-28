/*
 * Copyright © 2018 IBM Intellectual Property.
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

package org.onap.ccsdk.apps.controllerblueprints.service.model;

/**
 * BlueprintModelResponse.java Purpose: Model response for Upload CBA service
 *
 */
public class BlueprintModelResponse {
    private Long id;
    private String name;
    private String version;
    private String description;
    private String cbaUUID;

    public BlueprintModelResponse(Long id, String name, String version, String description, String cbaUUID) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
        this.cbaUUID = cbaUUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCbaUUID() {
        return cbaUUID;
    }

    public void setCbaUUID(String cbaUUID) {
        this.cbaUUID = cbaUUID;
    }
}