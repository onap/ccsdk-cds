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

import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.springframework.http.codec.multipart.FilePart
import org.springframework.util.StringUtils
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


fun currentTimestamp(): String {
    val now = LocalDateTime.now(ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    return formatter.format(now)
}


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