/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.onap.ccsdk.config.data.adaptor.db.utils;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class DataSetup {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(DataSetup.class);
    private TestDb testDb;
    
    private TestTable capability = null;
    
    private static final String[] CAPABILITY_COLUMNS =
            {"capability_id", "capability_name", "implementation_name", "operation_name", "operation_description",
                    "input_definition", "output_definition", "dependency_definition", "tags", "creation_date"};
    
    private void initTables() {
        if (capability == null)
            capability = testDb.table("CAPABILITY", "capability_id", CAPABILITY_COLUMNS);
        
    }
    
    public void cleanup() {
        initTables();
        capability.delete("true");
        logger.info("Cleaned All tables");
        
    }
    
    public void setupVpePort(String capability_id, String capability_name, String implementation_name,
            String operation_name, String operation_description, String tags) {
        initTables();
        capability.add(capability_id, capability_name, implementation_name, operation_name, operation_description,
                tags);
    }
    
    public void setTestDb(TestDb testDb) {
        this.testDb = testDb;
    }
}
