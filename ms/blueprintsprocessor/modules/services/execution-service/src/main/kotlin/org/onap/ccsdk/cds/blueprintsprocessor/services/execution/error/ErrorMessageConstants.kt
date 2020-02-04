/*
 *  Copyright © 2019 IBM, Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution.error

import org.onap.ccsdk.cds.controllerblueprints.core.ErrorCodeDomainsConstants

/**
 * ErrorMessageConstants
 *
 * @author Steve Siani
 */
object ErrorMessageConstants {
    // CDS component BlueprintRuntimeProcessor error message
    const val BLUEPRINT_PROCESSOR_GENERIC_PROCESS_FAILURE = ErrorCodeDomainsConstants.BLUEPRINT_PROCESSOR + ".generic_process_failure"
    const val SELFSERVICE_API_INVALID_FILE_EXTENSION = ErrorCodeDomainsConstants.SELFSERVICE_API + ".invalid_file_extension"

    // CDS component Command-Executor error message

    // CDS component Py-Executor error message

    // CDS component SDCListener error message
}
