/*
 * Copyright © 2026 Deutsche Telekom AG.
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

package org.onap.ccsdk.cds.blueprintsprocessor.db

import org.junit.Assume.assumeTrue
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.BlueprintDatabaseConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.BluePrintDBLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.domain.BlueprintModel
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.domain.BlueprintModelContent
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.repository.BlueprintModelRepository
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolution
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolutionRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.PostgreSQLContainer
import java.util.UUID
import javax.sql.DataSource
import kotlin.random.Random
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [BlueprintDatabaseConfiguration::class, BluePrintDBLibPropertyService::class])
@TestPropertySource(
    properties = [
        "blueprintsprocessor.db.driverClassName=org.postgresql.Driver",
        "blueprintsprocessor.db.hibernateHbm2ddlAuto=create-drop",
        "blueprintsprocessor.db.hibernateDDLAuto=update",
        "blueprintsprocessor.db.hibernateNamingStrategy=org.hibernate.cfg.ImprovedNamingStrategy",
        "blueprintsprocessor.blueprintDeployPath=./target/blueprints/deploy",
        "blueprintsprocessor.blueprintArchivePath=./target/blueprints/archive",
        "blueprintsprocessor.blueprintWorkingPath=./target/blueprints/work"
    ]
)
class PostgresCompatibilityTest {

    companion object {

        val postgres: PostgreSQLContainer<*> by lazy {
            PostgreSQLContainer("postgres:13-alpine").apply { start() }
        }

        @JvmStatic
        @BeforeClass
        fun requireDocker() {
            assumeTrue("Docker is not available", DockerClientFactory.instance().isDockerAvailable)
        }

        @JvmStatic
        @DynamicPropertySource
        fun dbProperties(registry: DynamicPropertyRegistry) {
            registry.add("blueprintsprocessor.db.url") { postgres.jdbcUrl }
            registry.add("blueprintsprocessor.db.username") { postgres.username }
            registry.add("blueprintsprocessor.db.password") { postgres.password }
            registry.add("blueprintsprocessor.db.pg-source.type") { "postgres-db" }
            registry.add("blueprintsprocessor.db.pg-source.url") { postgres.jdbcUrl }
            registry.add("blueprintsprocessor.db.pg-source.username") { postgres.username }
            registry.add("blueprintsprocessor.db.pg-source.password") { postgres.password }
            registry.add("blueprintsprocessor.db.pg-source.hibernateHbm2ddlAuto") { "none" }
            registry.add("blueprintsprocessor.db.pg-source.hibernateDDLAuto") { "none" }
            registry.add("blueprintsprocessor.db.pg-source.hibernateNamingStrategy") { "org.hibernate.cfg.ImprovedNamingStrategy" }
            registry.add("blueprintsprocessor.db.pg-source.hibernateDialect") { "org.hibernate.dialect.PostgreSQLDialect" }
        }
    }

    @Autowired
    @Qualifier("primaryDataSource")
    lateinit var dataSource: DataSource

    @Autowired
    lateinit var blueprintModelRepository: BlueprintModelRepository

    @Autowired
    lateinit var resourceResolutionRepository: ResourceResolutionRepository

    @Autowired
    lateinit var dbLibPropertyService: BluePrintDBLibPropertyService

    private val textColumns = listOf(
        "blueprint_model" to "artifact_description",
        "blueprint_model" to "tags",
        "blueprint_model" to "workflows",
        "blueprint_model_content" to "description",
        "model_type" to "definition",
        "model_type" to "description",
        "model_type" to "tags",
        "resource_dictionary" to "definition",
        "resource_dictionary" to "description",
        "resource_dictionary" to "tags",
        "resource_resolution" to "value",
        "template_resolution" to "result",
        "resource_config_snapshot" to "config_snapshot",
        "blueprint_workflow_audit_status" to "workflow_task_content",
        "blueprint_workflow_audit_status" to "workflow_response_content",
        "error_messages" to "cause",
        "error_messages" to "action",
        "error_domains" to "description"
    )

    private fun columnType(table: String, column: String): String? =
        JdbcTemplate(dataSource).queryForList(
            "SELECT data_type FROM information_schema.columns " +
                "WHERE table_schema = 'public' AND table_name = ? AND column_name = ?",
            String::class.java, table, column
        ).firstOrNull()

    @Test
    fun `text columns are created as text`() {
        textColumns.forEach { (table, column) ->
            assertEquals("text", columnType(table, column), "$table.$column")
        }
    }

    @Test
    fun `CBA content column is created as bytea`() {
        assertEquals("bytea", columnType("blueprint_model_content", "content"))
    }

    @Test
    fun `round trips a multi-megabyte CBA archive and multi-byte text`() {
        val archive = Random(42).nextBytes(5 * 1024 * 1024)
        val description = "Grüße ✓ 🚀 ${"x".repeat(70_000)}"
        val model = BlueprintModel().apply {
            id = UUID.randomUUID().toString()
            artifactName = "db-compat"
            artifactVersion = "1.0.0"
            artifactType = "SDNC_MODEL"
            artifactDescription = description
            published = "Y"
            updatedBy = "test@onap.org"
            tags = "db, compat"
            workflows = emptyMap()
        }
        model.blueprintModelContent = BlueprintModelContent().apply {
            id = UUID.randomUUID().toString()
            name = "db-compat"
            contentType = "CBA_ZIP"
            content = archive
            blueprintModel = model
        }
        blueprintModelRepository.saveAndFlush(model)

        val loaded = blueprintModelRepository.findByArtifactNameAndArtifactVersion("db-compat", "1.0.0")
        assertNotNull(loaded)
        assertEquals(description, loaded.artifactDescription)
        assertContentEquals(archive, loaded.blueprintModelContent!!.content)
    }

    @Test
    fun `native resource resolution query runs`() {
        resourceResolutionRepository.saveAndFlush(
            ResourceResolution().apply {
                id = UUID.randomUUID().toString()
                blueprintName = "db-compat"
                blueprintVersion = "1.0.0"
                artifactName = "config"
                name = "hostname"
                value = "router-1"
                status = "SUCCESS"
                resolutionKey = "key-1"
                resourceType = "vnf"
                resourceId = "id-1"
                occurrence = 1
                dictionaryName = "hostname"
                dictionarySource = "input"
                dictionaryVersion = 0
            }
        )

        val found = resourceResolutionRepository
            .findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactNameAndName(
                "key-1", "db-compat", "1.0.0", "config", "hostname"
            )
        assertEquals("router-1", found?.value)
    }

    @Test
    fun `deletes every resource resolution of a resolution key`() {
        (1..3).forEach {
            resourceResolutionRepository.saveAndFlush(
                ResourceResolution().apply {
                    id = UUID.randomUUID().toString()
                    blueprintName = "db-compat"
                    blueprintVersion = "1.0.0"
                    artifactName = "config"
                    name = "hostname"
                    value = "router-$it"
                    status = "SUCCESS"
                    resolutionKey = "delete-key"
                    resourceType = "vnf"
                    resourceId = "id-1"
                    occurrence = it
                    dictionaryName = "hostname"
                    dictionarySource = "input"
                    dictionaryVersion = 0
                }
            )
        }

        val deleted = resourceResolutionRepository.deleteByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKey(
            "db-compat", "1.0.0", "config", "delete-key"
        )

        assertEquals(3, deleted)
    }

    @Test
    fun `postgres-db selector queries a PostgreSQL database`() {
        val db = dbLibPropertyService.JdbcTemplate("pg-source")
        assertEquals(1, db.query("SELECT 1 AS one", emptyMap())[0]["one"])
    }
}
