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

package org.onap.ccsdk.apps.blueprintsprocessor.db.primary.service

import org.onap.ccsdk.apps.blueprintsprocessor.db.BluePrintDBLibGenericService
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service

@Service
class PrimaryDBLibGenericService(private val primaryNamedParameterJdbcTemplate: NamedParameterJdbcTemplate)
    : BluePrintDBLibGenericService {

    override fun namedParameterJdbcTemplate(): NamedParameterJdbcTemplate {
        return primaryNamedParameterJdbcTemplate
    }

    override fun query(sql: String, params: Map<String, Any>): List<Map<String, Any>> {
        return primaryNamedParameterJdbcTemplate.queryForList(sql, params)
    }

    override fun update(sql: String, params: Map<String, Any>): Int {
        return primaryNamedParameterJdbcTemplate.update(sql, params)
    }
}