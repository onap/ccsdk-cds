package org.onap.ccsdk.config.data.adaptor.dao;

import java.util.List;
import java.util.Map;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public interface QueryExecutorDao {

    /**
     * Query given SQL to create a prepared statement from SQL and a list of arguments to bind to the
     * query, expecting a result list.
     * <p>
     * The results will be mapped to a List (one entry for each row) of Maps (one entry for each column,
     * using the column name as the key). Each element in the list will be of the form returned by this
     * interface's queryForMap() methods.
     *
     * @param sql SQL query to execute
     * @param data arguments to bind to the query (leaving it to the PreparedStatement to guess the
     *        corresponding SQL type)
     * @return a List that contains a Map per row
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if the query fails
     */
    public List<Map<String, Object>> query(String sql, Object[] data) throws SvcLogicException;

    /**
     * Issue a single SQL update operation (such as an insert, update or delete statement) via a
     * prepared statement, binding the given arguments.
     *
     * @param sql SQL containing bind parameters
     * @param data arguments to bind to the query (leaving it to the PreparedStatement to guess the
     *        corresponding SQL type)
     * @return the number of rows affected
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if there is any problem issuing the update
     */
    public int update(String sql, Object[] data) throws SvcLogicException;

}
