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

package org.onap.ccsdk.apps.blueprintsprocessor.db.primary

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.apps.blueprintsprocessor.core.BluePrintProperties
import org.onap.ccsdk.apps.blueprintsprocessor.db.*
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.springframework.stereotype.Service

@Service
class BluePrintDBLibPropertySevice(private var bluePrintProperties: BluePrintProperties) {

    fun JdbcTemplate(jsonNode: JsonNode): BluePrintDBLibGenericService {
        val dBConnetionProperties = dBDataSourceProperties(jsonNode)
        return blueprintDBDataSourceService(dBConnetionProperties)
    }

    fun JdbcTemplate(selector: String): BluePrintDBLibGenericService {
        val prefix = "blueprintsprocessor.database.$selector"
        val dBConnetionProperties = dBDataSourceProperties(prefix)
        return blueprintDBDataSourceService(dBConnetionProperties)
    }

    private fun dBDataSourceProperties(jsonNode: JsonNode): DBDataSourceProperties {
        val type = jsonNode.get("type").textValue()
        return when (type) {
            DBLibConstants.MYSQL_DB -> {
                JacksonUtils.readValue(jsonNode, MySqlDataSourceProperties::class.java)!!
            }
            DBLibConstants.MARIA_DB -> {
                JacksonUtils.readValue(jsonNode, MariaDataSourceProperties::class.java)!!
            }
            else -> {
                throw BluePrintProcessorException("Rest adaptor($type) is not supported")
            }
        }
    }

    private fun dBDataSourceProperties(prefix: String): DBDataSourceProperties {
        val type = bluePrintProperties.propertyBeanType("$prefix.type", String::class.java)
        return when (type) {
            DBLibConstants.MARIA_DB -> {
                mariaDBConnectionProperties(prefix)
            }
            DBLibConstants.MYSQL_DB -> {
                mySqlDBConnectionProperties(prefix)
            }
            DBLibConstants.ORACLE_DB -> {
                TODO("not implemented")
            }
            DBLibConstants.POSTGRES_DB -> {
                TODO("not implemented")
            }
            DBLibConstants.PRIMARY_DB -> {
                primaryDBConnectionProperties(prefix)
            }
            else -> {
                throw BluePrintProcessorException("Rest adaptor($type) is not supported")
            }
        }
    }

    private fun blueprintDBDataSourceService(dBConnetionProperties: DBDataSourceProperties): BluePrintDBLibGenericService {
        when (dBConnetionProperties) {
            is MariaDataSourceProperties -> {
                return MariaDatabaseConfiguration(dBConnetionProperties)
            }
            is MySqlDataSourceProperties -> {
                return MySqlDatabaseConfiguration(dBConnetionProperties)
            }
            else -> {
                throw BluePrintProcessorException("couldn't get rest service for")
            }
        }
    }

    private fun mySqlDBConnectionProperties(prefix: String): MySqlDataSourceProperties {
        return bluePrintProperties.propertyBeanType(prefix, MySqlDataSourceProperties::class.java)
    }

    private fun mariaDBConnectionProperties(prefix: String): MariaDataSourceProperties {
        return bluePrintProperties.propertyBeanType(prefix, MariaDataSourceProperties::class.java)
    }

    private fun primaryDBConnectionProperties(prefix: String): PrimaryDataSourceProperties {
        return bluePrintProperties.propertyBeanType(prefix, PrimaryDataSourceProperties::class.java)
    }

}