package org.onap.ccsdk.config.data.adaptor.dao;

import java.util.List;
import org.onap.ccsdk.config.data.adaptor.domain.TransactionLog;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public interface TransactionLogDao {

    /**
     * Issue a single SQL Insert operation for CONFIG_TRANSACTION_LOG table via a prepared statement,
     * binding the given arguments.
     *
     * @param transactionLog arguments to bind to the query (mapping it to the PreparedStatement to the
     *        corresponding SQL type)
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if there is any problem issuing the insert
     */
    public void save(TransactionLog transactionLog) throws SvcLogicException;

    /**
     * Query CONFIG_TRANSACTION_LOG table for given request_id, mapping each row to a Java object via a
     * TransactionLog RowMapper.
     *
     * @param requestId argument to bind to the query (leaving it to the PreparedStatement to guess the
     *        corresponding SQL type)
     * @return the result List, containing mapped objects
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if the query fails
     */
    public List<TransactionLog> getTransactionsByRequestId(String requestId) throws SvcLogicException;

    /**
     * Query CONFIG_TRANSACTION_LOG table for given request_id, mapping each row to a Java object via a
     * TransactionLog RowMapper.
     *
     * @param requestId argument to bind to the query (leaving it to the PreparedStatement to guess the
     *        corresponding SQL type)
     * @param messageType argument to bind to the query (leaving it to the PreparedStatement to guess
     *        the corresponding SQL type)
     * @return the result List, containing mapped objects
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if the query fails
     */
    public List<TransactionLog> getTransactionsByRequestId(String requestId, String messageType)
            throws SvcLogicException;

}
