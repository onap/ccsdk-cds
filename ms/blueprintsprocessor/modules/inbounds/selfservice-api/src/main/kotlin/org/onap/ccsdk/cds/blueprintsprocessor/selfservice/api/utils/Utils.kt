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
package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.utils

import io.micrometer.core.instrument.Tag
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.SelfServiceMetricConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.util.StringUtils
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.util.UUID

const val INTERNAL_SERVER_ERROR_HTTP_STATUS_CODE = 500

@Throws(BluePrintException::class, IOException::class)
fun saveCBAFile(filePart: FilePart, targetDirectory: Path): Path {

    val fileName = StringUtils.cleanPath(filePart.filename())

    if (StringUtils.getFilenameExtension(fileName) != "zip") {
        throw BluePrintException("Invalid file extension required ZIP")
    }

    val changedFileName = UUID.randomUUID().toString() + ".zip"

    val targetLocation = targetDirectory.resolve(changedFileName)

    val file = File(targetLocation.toString())
    if (file.exists()) {
        file.delete()
    }
    file.createNewFile()

    filePart.transferTo(file)

    return targetLocation
}

fun determineHttpStatusCode(statusCode: Int): HttpStatus {

    try {
        return HttpStatus.valueOf(statusCode)
    } catch (exception: Exception) {
        // if statusCode cannot be converted to a proper HttpStatus, the resource still needs to assign a HTTP status
        // code to the response. In this case, a 500 Internal Server Error will be returned as default.
        return HttpStatus.valueOf(INTERNAL_SERVER_ERROR_HTTP_STATUS_CODE)
    }
}

fun cbaMetricTags(executionServiceInput: ExecutionServiceInput): MutableList<Tag> =
    executionServiceInput.actionIdentifiers.let {
        mutableListOf(
            Tag.of(SelfServiceMetricConstants.TAG_BP_NAME, it.blueprintName),
            Tag.of(SelfServiceMetricConstants.TAG_BP_VERSION, it.blueprintVersion),
            Tag.of(SelfServiceMetricConstants.TAG_BP_ACTION, it.actionName)
        )
    }

fun cbaMetricTags(executionServiceOutput: ExecutionServiceOutput): MutableList<Tag> =
    executionServiceOutput.let {
        mutableListOf(
            Tag.of(SelfServiceMetricConstants.TAG_BP_NAME, it.actionIdentifiers.blueprintName),
            Tag.of(SelfServiceMetricConstants.TAG_BP_VERSION, it.actionIdentifiers.blueprintVersion),
            Tag.of(SelfServiceMetricConstants.TAG_BP_ACTION, it.actionIdentifiers.actionName),
            Tag.of(SelfServiceMetricConstants.TAG_BP_STATUS, it.status.code.toString()),
            Tag.of(SelfServiceMetricConstants.TAG_BP_OUTCOME, it.status.message)
        )
    }
