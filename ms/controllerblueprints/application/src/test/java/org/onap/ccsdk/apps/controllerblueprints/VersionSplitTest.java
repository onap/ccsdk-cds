/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.controllerblueprints;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * VersionSplitTest
 *
 * @author Brinda Santh
 */
public class VersionSplitTest {
    private static EELFLogger log = EELFManager.getInstance().getLogger(VersionSplitTest.class);

    @Test
    public void testVersionSplit() {
        String version = "1.03.04";
        String[] tokens = StringUtils.split(version, '.');
        Assert.assertNotNull("failed to tokenize", tokens);
        Assert.assertEquals("failed to three token ", 3, tokens.length);
    }

    @Test
    public void encodeTest() {
        String name = "ccsdkapps";
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encodedValue = bCryptPasswordEncoder.encode(name);
        Assert.assertTrue("Failed to match", bCryptPasswordEncoder.matches(name, encodedValue));
    }
}