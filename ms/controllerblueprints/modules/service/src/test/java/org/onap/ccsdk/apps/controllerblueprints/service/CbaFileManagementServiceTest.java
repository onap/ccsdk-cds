/*
 * Copyright © 2018 IBM Intellectual Property.
 * Modifications Copyright © 2018 IBM.
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

package org.onap.ccsdk.apps.controllerblueprints.service;
import org.junit.*;
import org.junit.runner.RunWith;
import org.onap.ccsdk.apps.controllerblueprints.TestApplication;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintFileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileSystemUtils;
import java.nio.file.Path;


/**
 * CbaFileManagementServiceTest.java Purpose: Test the decompressing method of CbaCompressionService
 *
 * @author Vinal Patel
 * @version 1.0
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {TestApplication.class})
public class CbaFileManagementServiceTest {

    @Value("${controllerblueprints.loadBlueprintsExamplesPath}")
    private String cbaPath;
    private String zipfile;
    private String directorypath;
    private Path zipfilepath;

    @Autowired
    CbaFileManagementService cbaCompressionService;


    /**
     *
     */
    @Before
    public void setUp() {
        try {
            zipfilepath = BluePrintFileUtils.Companion.getCbaStorageDirectory(cbaPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        zipfile = "CBA_Zip_Test.zip";
        directorypath = zipfilepath.resolve(zipfile.substring(0,zipfile.lastIndexOf("."))).toAbsolutePath().toString();
    }
    @After
    public void clenup() throws BluePrintException {

        try {
            //Delete the Zip file from the repository
            FileSystemUtils.deleteRecursively(BluePrintFileUtils.Companion.getBluePrintFile(directorypath, zipfilepath));
        }
        catch (Exception ex){
            throw new BluePrintException("Fail while cleaning up CBA saved!", ex);
        }
    }

    /**
     * @throws BluePrintException
     * Test will get success if it is able to decompress CBA file and returns the folder path
     */
    @Test
    public void testDecompressCBAFile_success() throws BluePrintException {
        Assert.assertEquals(directorypath,cbaCompressionService.decompressCBAFile(zipfile,zipfilepath));
    }

}
