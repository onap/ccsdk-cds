/*
 * Copyright © 2019 Bell Canada Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.db.primary

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.db.BlueprintDBLibGenericService
import org.onap.ccsdk.cds.blueprintsprocessor.db.DBDataSourceProperties
import org.onap.ccsdk.cds.blueprintsprocessor.db.DBLibConstants.Companion.MARIA_DB
import org.onap.ccsdk.cds.blueprintsprocessor.db.DBLibConstants.Companion.MYSQL_DB
import org.onap.ccsdk.cds.blueprintsprocessor.db.DBLibConstants.Companion.MSSQL_DB
import org.onap.ccsdk.cds.blueprintsprocessor.db.DBLibConstants.Companion.PROCESSOR_DB
import org.onap.ccsdk.cds.blueprintsprocessor.db.MariaDataSourceProperties
import org.onap.ccsdk.cds.blueprintsprocessor.db.MySqlDataSourceProperties
import org.onap.ccsdk.cds.blueprintsprocessor.db.MSSqlDataSourceProperties
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.stereotype.Service

@Service
class BlueprintDBLibPropertyService(private var bluePrintPropertiesService: BlueprintPropertiesService) {

    fun JdbcTemplate(jsonNode: JsonNode): BlueprintDBLibGenericService =
        blueprintDBDataSourceService(dBDataSourceProperties(jsonNode))

    fun JdbcTemplate(selector: String): BlueprintDBLibGenericService =
        blueprintDBDataSourceService(dBDataSourceProperties("blueprintsprocessor.db.$selector"))

    private fun dBDataSourceProperties(jsonNode: JsonNode): DBDataSourceProperties =
        when (val type = jsonNode.get("type").textValue()) {
            MYSQL_DB -> JacksonUtils.readValue(jsonNode, MySqlDataSourceProperties::class.java)
            MARIA_DB -> JacksonUtils.readValue(jsonNode, MariaDataSourceProperties::class.java)
            MSSQL_DB -> JacksonUtils.readValue(jsonNode, MSSqlDataSourceProperties::class.java)
            else -> {
                throw BlueprintProcessorException(
                    "DB type ($type) is not supported. Valid types: $MARIA_DB, $MYSQL_DB, $MSSQL_DB"
                )
            }
        }!!

    private fun dBDataSourceProperties(prefix: String): DBDataSourceProperties =
        bluePrintPropertiesService.propertyBeanType("$prefix.type", String::class.java).let {
            return when (it) {
                MARIA_DB, PROCESSOR_DB -> mariaDBConnectionProperties(prefix)
                MYSQL_DB -> mySqlDBConnectionProperties(prefix)
                MSSQL_DB -> mssqlDBConnectionProperties(prefix)
                else -> {
                    throw BlueprintProcessorException(
                        "DB type ($it) is not supported. Valid types: $MARIA_DB, $MYSQL_DB, $PROCESSOR_DB"
                    )
                }
            }
        }

    private fun blueprintDBDataSourceService(dBConnetionProperties: DBDataSourceProperties): BlueprintDBLibGenericService =
        when (dBConnetionProperties) {
            is MariaDataSourceProperties -> MariaDatabaseConfiguration(dBConnetionProperties)
            is MySqlDataSourceProperties -> MySqlDatabaseConfiguration(dBConnetionProperties)
            is MSSqlDataSourceProperties -> MSSqlDatabaseConfiguration(dBConnetionProperties)
            else -> throw BlueprintProcessorException(
                "Failed to create db configuration for ${dBConnetionProperties.url}"
            )
        }

    private fun mySqlDBConnectionProperties(prefix: String): MySqlDataSourceProperties =
        bluePrintPropertiesService.propertyBeanType(prefix, MySqlDataSourceProperties::class.java)

    private fun mariaDBConnectionProperties(prefix: String): MariaDataSourceProperties =
        bluePrintPropertiesService.propertyBeanType(prefix, MariaDataSourceProperties::class.java)

    private fun mssqlDBConnectionProperties(prefix: String): MSSqlDataSourceProperties =
        bluePrintPropertiesService.propertyBeanType(prefix, MSSqlDataSourceProperties::class.java)
}
