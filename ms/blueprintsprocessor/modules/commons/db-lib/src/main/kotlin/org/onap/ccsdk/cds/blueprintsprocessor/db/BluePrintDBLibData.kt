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

package org.onap.ccsdk.cds.blueprintsprocessor.db

open class DBDataSourceProperties {

    var type: String = DBLibConstants.MARIA_DB
    lateinit var url: String
    lateinit var username: String
    lateinit var password: String
    open lateinit var driverClassName: String
}

open class PrimaryDataSourceProperties : DBDataSourceProperties() {

    lateinit var hibernateHbm2ddlAuto: String
    lateinit var hibernateDDLAuto: String
    lateinit var hibernateNamingStrategy: String
    lateinit var hibernateDialect: String
}

open class MariaDataSourceProperties : DBDataSourceProperties() {

    lateinit var hibernateHbm2ddlAuto: String
    lateinit var hibernateDDLAuto: String
    lateinit var hibernateNamingStrategy: String
    lateinit var hibernateDialect: String
    override var driverClassName = DBLibConstants.DRIVER_MARIA_DB
}

open class MySqlDataSourceProperties : DBDataSourceProperties() {

    lateinit var hibernateHbm2ddlAuto: String
    lateinit var hibernateDDLAuto: String
    lateinit var hibernateNamingStrategy: String
    lateinit var hibernateDialect: String
    override var driverClassName = DBLibConstants.DRIVER_MYSQL_DB
}
