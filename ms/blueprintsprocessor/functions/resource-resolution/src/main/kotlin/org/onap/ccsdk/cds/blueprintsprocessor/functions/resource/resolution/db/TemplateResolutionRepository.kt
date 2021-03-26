/*
 * Copyright (C) 2019 Bell Canada.
 * Modifications Copyright © 2021 Nokia.
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

    fun findAllByResolutionKeyAndBlueprintNameAndBlueprintVersionAndOccurrence(
        key: String,
        blueprintName: String?,
        blueprintVersion: String?,
        occurrence: Int
    ): List<TemplateResolution?>

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
