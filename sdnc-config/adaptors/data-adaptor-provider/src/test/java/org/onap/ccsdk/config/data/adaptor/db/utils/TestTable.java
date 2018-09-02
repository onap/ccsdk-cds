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

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public class TestTable {
    
    private String tableName;
    private String[] columnList;
    private String idName;
    
    private String insertSql;
    
    private JdbcTemplate jdbcTemplate;
    
    public TestTable(JdbcTemplate jdbcTemplate, String tableName, String idName, String... columnList) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
        this.idName = idName;
        this.columnList = columnList;
        createInsertSql();
    }
    
    private void createInsertSql() {
        StringBuilder ss = new StringBuilder();
        ss.append("INSERT INTO ").append(tableName).append(" (");
        for (String s : columnList)
            ss.append(s).append(", ");
        ss.setLength(ss.length() - 2);
        ss.append(") VALUES (");
        for (int i = 0; i < columnList.length; i++)
            ss.append("?, ");
        ss.setLength(ss.length() - 2);
        ss.append(")");
        insertSql = ss.toString();
    }
    
    public void add(Object... values) {
        jdbcTemplate.update(insertSql, values);
    }
    
    public long getLastId() {
        return jdbcTemplate.queryForObject("SELECT max(" + idName + ") FROM " + tableName, Long.class);
    }
    
    public Long getId(String where) {
        String selectSql = "SELECT " + idName + " FROM " + tableName + " WHERE " + where;
        SqlRowSet rs = jdbcTemplate.queryForRowSet(selectSql);
        if (rs.first())
            return rs.getLong(idName);
        return null;
    }
    
    public boolean exists(String where) {
        String selectSql = "SELECT * FROM " + tableName + " WHERE " + where;
        SqlRowSet rs = jdbcTemplate.queryForRowSet(selectSql);
        return rs.first();
    }
    
    public void delete(String where) {
        jdbcTemplate.update("DELETE FROM " + tableName + " WHERE " + where);
    }
}
