/*
 *  Copyright © 2020 IBM, Bell Canada.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.error.catalog.core

object ErrorMessageLibConstants {

    const val ERROR_CATALOG_DOMAIN = "org.onap.ccsdk.cds.error.catalog"
    const val ERROR_CATALOG_TYPE = "error.catalog.type"
    const val ERROR_CATALOG_TYPE_PROPERTIES = "properties"
    const val ERROR_CATALOG_TYPE_DB = "DB"
    const val ERROR_CATALOG_PROPERTIES_FILENAME = "error-messages_en.properties"
    const val ERROR_CATALOG_PROPERTIES_DIRECTORY = "/opt/app/onap/config/"
    const val ERROR_CATALOG_MODELS = "org.onap.ccsdk.cds.error.catalog.domain"
    const val ERROR_CATALOG_REPOSITORY = "org.onap.ccsdk.cds.error.catalog.repository"
    const val ERROR_CATALOG_DEFAULT_ERROR_CODE = 500
    const val ERROR_CATALOG_PROTOCOL_HTTP = "http"
    const val ERROR_CATALOG_PROTOCOL_GRPC = "grpc"
}
