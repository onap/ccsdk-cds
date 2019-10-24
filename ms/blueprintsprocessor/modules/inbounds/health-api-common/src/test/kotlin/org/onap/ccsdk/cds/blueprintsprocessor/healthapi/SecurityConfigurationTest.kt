package org.onap.ccsdk.cds.blueprintsprocessor.healthapi

import org.junit.Assert
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.configuration.SecurityEncryptionConfiguration


class SecurityConfigurationTest {

    @Test
    fun testEncryption() {
        val passWord = "ccsdkapps"
        val securityConfiguration = SecurityEncryptionConfiguration()
        val encryptedValue = securityConfiguration.encrypt(passWord)
        println(encryptedValue)
        Assert.assertEquals(passWord, securityConfiguration.decrypt(encryptedValue!!))
    }
}
