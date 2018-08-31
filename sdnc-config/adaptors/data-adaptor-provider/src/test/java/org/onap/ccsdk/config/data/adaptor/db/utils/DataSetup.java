
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
