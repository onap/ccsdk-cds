/*
 * Copyright (C) 2019 Bell Canada.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db

import org.springframework.data.jpa.repository.JpaRepository

interface ResourceResolutionRepository : JpaRepository<ResourceResolution, String> {

    fun findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactNameAndName(key: String,
                                                                                     blueprintName: String?,
                                                                                     blueprintVersion: String?,
                                                                                     artifactName: String,
                                                                                     name: String): ResourceResolution

    fun findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactName(resolutionKey: String,
                                                                              blueprintName: String,
                                                                              blueprintVersion: String,
                                                                              artifactPrefix: String): List<ResourceResolution>

    fun findByBlueprintNameAndBlueprintVersionAndResourceIdAndResourceType(blueprintName: String,
                                                                           blueprintVersion: String,
                                                                           resourceId: String,
                                                                           resourceType: String): List<ResourceResolution>

    fun findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKeyAndOccurrence(
        blueprintName: String?,
        blueprintVersion: String?,
        artifactName: String,
        resolutionKey: String,
        occurrence: Int): List<ResourceResolution>

    fun findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResourceIdAndResourceTypeAndOccurrence(
        blueprintName: String?,
        blueprintVersion: String?,
        artifactName: String,
        resourceId: String,
        resourceType: String,
        occurrence: Int): List<ResourceResolution>
}