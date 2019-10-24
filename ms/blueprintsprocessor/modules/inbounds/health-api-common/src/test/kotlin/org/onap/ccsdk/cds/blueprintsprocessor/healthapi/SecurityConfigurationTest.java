package org.onap.ccsdk.cds.blueprintsprocessor.healthapi;

import org.junit.Assert;
import org.junit.Test;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.configuration.SecurityEncryptionConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class SecurityConfigurationTest {

  @Test public void testEncryption() {
    String passWord = "ccsdkapps";
    SecurityEncryptionConfiguration securityCOnfiguration = new SecurityEncryptionConfiguration();
    String encryptedValue = securityCOnfiguration.encrypt(passWord);
    System.out.println(encryptedValue);
    Assert.assertEquals(passWord, securityCOnfiguration.decrypt(encryptedValue));
  }



}
