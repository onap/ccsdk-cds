package org.onap.ccsdk.config.data.adaptor.db.utils;

import org.springframework.jdbc.core.JdbcTemplate;

public class TestDb {

    private JdbcTemplate jdbcTemplate;

    public TestTable table(String tableName, String idName, String... columnList) {
        return new TestTable(jdbcTemplate, tableName, idName, columnList);
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
