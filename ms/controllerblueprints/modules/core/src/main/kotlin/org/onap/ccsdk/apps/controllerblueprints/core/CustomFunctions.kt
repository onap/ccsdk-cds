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

package org.onap.ccsdk.apps.controllerblueprints.core

import org.slf4j.helpers.MessageFormatter
import java.io.File
import java.io.InputStream
import kotlin.reflect.KClass

/**
 *
 *
 * @author Brinda Santh
 */

fun format(message: String, vararg args: Any?) : String{
    if(args != null && args.isNotEmpty()){
        return MessageFormatter.arrayFormat(message, args).message
    }
   return message
}

fun <T : Any> MutableMap<String, *>.getCastOptionalValue(key: String, valueType: KClass<T>): T? {
    if (containsKey(key)) {
        return get(key) as? T
    } else {
        return null
    }
}

fun <T : Any> MutableMap<String, *>.getCastValue(key: String, valueType: KClass<T>): T {
    if (containsKey(key)) {
        return get(key) as T
    } else {
        throw BluePrintException("couldn't find the key " + key)
    }
}

fun checkNotEmpty(value : String?) : Boolean{
    return value != null && value.isNotEmpty()
}

fun checkNotEmptyNThrow(value : String?, message : String? = value.plus(" is null/empty ")) : Boolean{
    val notEmpty = value != null && value.isNotEmpty()
    if(!notEmpty){
        throw BluePrintException(message!!)
    }
    return notEmpty
}

fun InputStream.toFile(path: String) : File {
    val file = File(path)
    file.outputStream().use { this.copyTo(it) }
    return file
}






