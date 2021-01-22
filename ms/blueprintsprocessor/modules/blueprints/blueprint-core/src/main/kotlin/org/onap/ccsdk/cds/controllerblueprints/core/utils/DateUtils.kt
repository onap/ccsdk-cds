/*
 *  Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.controllerblueprints.core.utils

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

fun controllerDate(): Date {
    val localDateTime = LocalDateTime.now(ZoneId.systemDefault())
    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())
}

fun currentTimestamp(): String {
    val localDateTime = LocalDateTime.now(ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern(BlueprintConstants.DATE_TIME_PATTERN)
    return formatter.format(localDateTime)
}

/** Parse string date in CDS string format */
fun String.toControllerDate(): Date {
    val formatter = SimpleDateFormat(BlueprintConstants.DATE_TIME_PATTERN)
    return formatter.parse(this)
}

/** Return date to CDS string format */
fun Date.currentTimestamp(): String {
    val formatter = SimpleDateFormat(BlueprintConstants.DATE_TIME_PATTERN)
    return formatter.format(this)
}

/** Return incremented date for [number] of days */
fun Date.addDate(number: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.add(Calendar.DATE, number)
    return calendar.time
}
