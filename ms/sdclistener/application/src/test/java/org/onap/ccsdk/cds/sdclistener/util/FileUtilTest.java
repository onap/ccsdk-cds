/*-
 * ============LICENSE_START==========================================
 * ONAP Portal
 * ===================================================================
 * Copyright (C) 2020 IBM Intellectual Property. All rights reserved.
 * ===================================================================
 *
 * Unless otherwise specified, all software contained herein is licensed
 * under the Apache License, Version 2.0 (the "License");
 * you may not use this software except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Unless otherwise specified, all documentation contained herein is licensed
 * under the Creative Commons License, Attribution 4.0 Intl. (the "License");
 * you may not use this documentation except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             https://creativecommons.org/licenses/by/4.0/
 *
 * Unless required by applicable law or agreed to in writing, documentation
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ============LICENSE_END============================================
 *
 *
 */

package org.onap.ccsdk.cds.sdclistener.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.File;
import java.io.IOException;
import static org.hamcrest.MatcherAssert.assertThat;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.hamcrest.collection.IsEmptyCollection;

import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.*;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {FileUtilTest.class})
public class FileUtilTest {

    FileUtil fs;

    @Test
    public void testDeleteFile() throws IOException {
        File tempFile = File.createTempFile("tempFile", ".txt");
        // System.out.println(tempFile.getRoot());
        fs.deleteFile(tempFile, tempFile.getAbsolutePath());
        assertFalse(tempFile.exists());

    }

    @Test
    public void testGetFilesFromDisk() throws IOException {

        Path resourceDirectory = Paths.get("src", "test", "resources");
        int totalfile = resourceDirectory.getNameCount();
        List fileList = fs.getFilesFromDisk(resourceDirectory);
        assertNotNull(fileList);
        assertEquals(fileList.size(), totalfile);
    }


}
