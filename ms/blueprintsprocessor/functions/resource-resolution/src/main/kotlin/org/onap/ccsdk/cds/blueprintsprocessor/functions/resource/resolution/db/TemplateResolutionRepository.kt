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
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

@Repository
interface TemplateResolutionRepository : JpaRepository<TemplateResolution, String> {

    @Query(
        value = """
        SELECT * FROM TEMPLATE_RESOLUTION
             WHERE resource_type = :resourceType AND resource_id = :resourceId
             AND blueprint_name = :blueprintName AND blueprint_version = :blueprintVersion
             AND artifact_name = :artifactName AND occurrence = :occurrence
             ORDER BY creation_date DESC LIMIT 1
        """,
        nativeQuery = true
    )
    fun findByResourceIdAndResourceTypeAndBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
        resourceId: String,
        resourceType: String,
        blueprintName: String?,
        blueprintVersion: String?,
        artifactName: String,
        occurrence: Int
    ): TemplateResolution?

    @Query(
        value = """
        SELECT * FROM TEMPLATE_RESOLUTION WHERE resolution_key = :key 
             AND blueprint_name = :blueprintName AND blueprint_version = :blueprintVersion 
             AND artifact_name = :artifactName AND occurrence = :occurrence
             ORDER BY creation_date DESC LIMIT 1
        """,
        nativeQuery = true
    )
    fun findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactNameAndOccurrence(
        key: String,
        blueprintName: String?,
        blueprintVersion: String?,
        artifactName: String,
        occurrence: Int
    ): TemplateResolution?

    @Query(
        value = """
         SELECT * FROM TEMPLATE_RESOLUTION WHERE resolution_key = :key 
            AND blueprint_name = :blueprintName AND blueprint_version = :blueprintVersion 
            AND artifact_name = :artifactName 
            AND occurrence <=  :firstN
        """,
        nativeQuery = true
    )
    fun findFirstNOccurrences(
        @Param("key")key: String,
        @Param("blueprintName")blueprintName: String,
        @Param("blueprintVersion")blueprintVersion: String,
        @Param("artifactName")artifactName: String,
        @Param("firstN")begin: Int
    ): List<TemplateResolution>

    @Query(
        value = """
        SELECT * FROM TEMPLATE_RESOLUTION WHERE resolution_key = :key 
            AND blueprint_name = :blueprintName AND blueprint_version = :blueprintVersion 
            AND artifact_name = :artifactName 
            AND occurrence > ( 
                select max(occurrence) - :lastN from RESOURCE_RESOLUTION 
                WHERE resolution_key = :key 
                    AND blueprint_name = :blueprintName AND blueprint_version = :blueprintVersion 
                    AND artifact_name = :artifactName) 
                    ORDER BY occurrence DESC, creation_date DESC
      """,
        nativeQuery = true
    )
    fun findLastNOccurrences(
        @Param("key")key: String,
        @Param("blueprintName")blueprintName: String,
        @Param("blueprintVersion")blueprintVersion: String,
        @Param("artifactName")artifactName: String,
        @Param("lastN")begin: Int
    ): List<TemplateResolution>

    @Query(
        value = """
        SELECT * FROM TEMPLATE_RESOLUTION WHERE resolution_key = :key 
            AND blueprint_name = :blueprintName AND blueprint_version = :blueprintVersion 
            AND artifact_name = :artifactName 
            AND occurrence BETWEEN :begin AND :end 
            ORDER BY occurrence DESC, creation_date DESC
       """,
        nativeQuery = true
    )
    fun findOccurrencesWithinRange(
        @Param("key")key: String,
        @Param("blueprintName")blueprintName: String,
        @Param("blueprintVersion")blueprintVersion: String,
        @Param("artifactName")artifactName: String,
        @Param("begin")begin: Int,
        @Param("end")end: Int
    ): List<TemplateResolution>

    fun findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactName(
        resolutionKey: String,
        blueprintName: String,
        blueprintVersion: String,
        artifactPrefix: String
    ): List<TemplateResolution>

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

    @Transactional
    fun deleteByResourceIdAndResourceTypeAndBlueprintNameAndBlueprintVersionAndArtifactName(
        resourceId: String,
        resourceType: String,
        blueprintName: String,
        blueprintVersion: String,
        artifactName: String
    ): Int

    @Transactional
    fun deleteByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactName(
        key: String,
        blueprintName: String,
        blueprintVersion: String,
        artifactName: String
    ): Int

    @Transactional
    @Modifying
    @Query(
        value = """
        DELETE FROM TEMPLATE_RESOLUTION WHERE resolution_key = :resolutionKey
            AND blueprint_name = :blueprintName AND blueprint_version = :blueprintVersion
            AND artifact_name = :artifactName
            AND occurrence > (
                SELECT MAX(occurrence) - :lastN FROM TEMPLATE_RESOLUTION
                WHERE resolution_key = :resolutionKey AND blueprint_name = :blueprintName
                    AND blueprint_version = :blueprintVersion AND artifact_name = :artifactName
                )
    """,
        nativeQuery = true
    )
    fun deleteTemplates(
        blueprintName: String,
        blueprintVersion: String,
        artifactName: String,
        resolutionKey: String,
        lastN: Int
    ): Int

    @Transactional
    @Modifying
    @Query(
        value = """
        DELETE FROM TEMPLATE_RESOLUTION WHERE resource_type = :resourceType
            AND resource_id = :resourceId AND artifact_name = :artifactName
            AND blueprint_name = :blueprintName AND blueprint_version = :blueprintVersion
            AND occurrence > (
                SELECT MAX(occurrence) - :lastN FROM TEMPLATE_RESOLUTION
                WHERE resource_type = :resourceType
                    AND resource_id = :resourceId AND blueprint_name = :blueprintName
                    AND blueprint_version = :blueprintVersion AND artifact_name = :artifactName
            )
    """,
        nativeQuery = true
    )
    fun deleteTemplates(
        blueprintName: String,
        blueprintVersion: String,
        artifactName: String,
        resourceType: String,
        resourceId: String,
        lastN: Int
    ): Int
}
