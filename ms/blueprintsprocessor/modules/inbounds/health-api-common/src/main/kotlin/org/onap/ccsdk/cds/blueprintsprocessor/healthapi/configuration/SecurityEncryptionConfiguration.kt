package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.configuration


import org.apache.commons.net.util.Base64
import org.springframework.stereotype.Service
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


@Service
class SecurityEncryptionConfiguration {
    private val key = "aesEncryptionKey"
    private val initVector = "encryptionIntVec"

    fun encrypt(value: String): String? {
        try {
            val iv = IvParameterSpec(initVector.toByteArray(charset("UTF-8")))
            val skeySpec = SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)

            val encrypted = cipher.doFinal(value.toByteArray())
            return Base64.encodeBase64String(encrypted)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return null
    }

    open fun decrypt(encrypted: String): String? {
        try {
            val iv = IvParameterSpec(initVector.toByteArray(charset("UTF-8")))
            val skeySpec = SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv)
            val original = cipher.doFinal(Base64.decodeBase64(encrypted))

            return String(original)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return null
    }
}
