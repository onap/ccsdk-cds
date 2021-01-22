/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.relationshipTypeConnectsTo
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.serviceTemplate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DatabasePropertiesDSLTest {

    @Test
    fun testDatabasePropertiesDSL() {
        val serviceTemplate = serviceTemplate("database-properties-test", "1.0.0", "xxx.@xx.com", "database") {
            topologyTemplate {
                relationshipTemplateDb("sample-maria-db", "Database Server") {
                    mariaDb {
                        url("jdbc://mariadb:3600")
                        username("user")
                        password("credential")
                        hibernateDDLAuto("hibernateDDLAuto")
                        hibernateHbm2ddlAuto("hibernateHbm2ddlAuto")
                        hibernateDDLAuto("hibernateDDLAuto")
                        hibernateNamingStrategy("hibernateNamingStrategy")
                    }
                }
                relationshipTemplateDb("sample-mysql-db", "Database Server") {
                    mySqlDb {
                        url("jdbc://mysql:3600")
                        username("user")
                        password("credential")
                        hibernateDDLAuto("hibernateDDLAuto")
                        hibernateHbm2ddlAuto("hibernateHbm2ddlAuto")
                        hibernateDDLAuto("hibernateDDLAuto")
                        hibernateNamingStrategy("hibernateNamingStrategy")
                    }
                }
            }
            relationshipTypeConnectsToDb()
            relationshipTypeConnectsTo()
        }

        // println(serviceTemplate.asJsonString(true))
        assertNotNull(serviceTemplate, "failed to create service template")
        val relationshipTemplates = serviceTemplate.topologyTemplate?.relationshipTemplates
        assertNotNull(relationshipTemplates, "failed to get relationship templates")
        assertEquals(2, relationshipTemplates.size, "relationshipTemplates doesn't match")
        assertNotNull(relationshipTemplates["sample-maria-db"], "failed to get sample-maria-db")
        assertNotNull(relationshipTemplates["sample-mysql-db"], "failed to get sample-mysql-db")

        val relationshipTypes = serviceTemplate.relationshipTypes
        assertNotNull(relationshipTypes, "failed to get relationship types")
        assertEquals(2, relationshipTypes.size, "relationshipTypes doesn't match")
        assertNotNull(
            relationshipTypes[BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO],
            "failed to get ${BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO}"
        )
        assertNotNull(
            relationshipTypes[BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_DB],
            "failed to get ${BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_DB}"
        )
    }
}
