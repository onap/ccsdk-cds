/*
 * Copyright © 2019-2020 Orange.
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

package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.configuration

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.net.util.Base64
import org.springframework.stereotype.Component

@Component
class SecurityEncryptionConfiguration {

    private val key = "aesEncryptionKey"
    private val initVector = "encryptionIntVec"

    fun encrypt(value: String): String? {
        try {
            val (iv, skeySpec, cipher) = initChiper()
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)
            val encrypted = cipher.doFinal(value.toByteArray())
            return Base64.encodeBase64String(encrypted)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return String()
    }

    open fun decrypt(encrypted: String): String? {
        try {
            val (iv, skeySpec, cipher) = initChiper()
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv)
            val original = cipher.doFinal(Base64.decodeBase64(encrypted))
            return String(original)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return String()
    }

    private fun initChiper(): Triple<IvParameterSpec, SecretKeySpec, Cipher> {
        val iv = IvParameterSpec(initVector.toByteArray(charset("UTF-8")))
        val secretKeySpec = SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        return Triple(iv, secretKeySpec, cipher)
    }
}
