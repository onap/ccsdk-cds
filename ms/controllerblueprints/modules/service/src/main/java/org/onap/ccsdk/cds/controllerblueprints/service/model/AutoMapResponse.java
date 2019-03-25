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

package org.onap.ccsdk.cds.controllerblueprints.service.model;

import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment;
import org.onap.ccsdk.cds.controllerblueprints.service.domain.ResourceDictionary;

import java.util.List;

/**
 * ArtifactRequest.java Purpose: Provide Configuration Generator ArtifactRequest Model
 *
 * @author Brinda Santh
 * @version 1.0
 */
public class AutoMapResponse {

    private List<ResourceAssignment> resourceAssignments;
    private List<ResourceDictionary> dataDictionaries;

    public List<ResourceAssignment> getResourceAssignments() {
        return resourceAssignments;
    }

    public void setResourceAssignments(List<ResourceAssignment> resourceAssignments) {
        this.resourceAssignments = resourceAssignments;
    }

    public List<ResourceDictionary> getDataDictionaries() {
        return dataDictionaries;
    }

    public void setDataDictionaries(List<ResourceDictionary> dataDictionaries) {
        this.dataDictionaries = dataDictionaries;
    }



}
