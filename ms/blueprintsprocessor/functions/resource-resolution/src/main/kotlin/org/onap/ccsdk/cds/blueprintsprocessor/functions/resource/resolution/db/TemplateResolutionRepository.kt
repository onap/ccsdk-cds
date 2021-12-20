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
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

@Repository
interface TemplateResolutionRepository : JpaRepository<TemplateResolution, String> {

    fun findByResourceIdAndResourceTypeAndBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
        resourceId: String,
        resourceType: String,
        blueprintName: String?,
        blueprintVersion: String?,
        artifactName: String,
        occurrence: Int
    ): TemplateResolution?

    fun findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
        key: String,
        blueprintName: String?,
        blueprintVersion: String?,
        artifactName: String,
        occurrence: Int
    ): TemplateResolution?

    @Query(
        "select tr.resolutionKey from TemplateResolution tr where tr.blueprintName = :blueprintName and tr.blueprintVersion = :blueprintVersion and tr.artifactName = :artifactName and tr.occurrence = :occurrence"
    )
    fun findResolutionKeysByBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
        @Param("blueprintName") blueprintName: String?,
        @Param("blueprintVersion") blueprintVersion: String?,
        @Param("artifactName") artifactName: String,
        @Param("occurrence") occurrence: Int
    ): List<String>?

    @Query(
        "select tr.artifactName as artifactName, tr.resolutionKey as resolutionKey from TemplateResolution tr where tr.blueprintName = :blueprintName and tr.blueprintVersion = :blueprintVersion and tr.occurrence = :occurrence"
    )
    fun findArtifactNamesAndResolutionKeysByBlueprintNameAndBlueprintVersionAndOccurrence(
        @Param("blueprintName") blueprintName: String?,
        @Param("blueprintVersion") blueprintVersion: String?,
        @Param("occurrence") occurrence: Int
    ): List<TemplateResolutionSelector>?

    @Transactional
    fun deleteByResourceIdAndResourceTypeAndBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
        resourceId: String,
        resourceType: String,
        blueprintName: String?,
        blueprintVersion: String?,
        artifactName: String,
        occurrence: Int
    )

    @Transactional
    fun deleteByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
        key: String,
        blueprintName: String?,
        blueprintVersion: String?,
        artifactName: String,
        occurrence: Int
    )
}
