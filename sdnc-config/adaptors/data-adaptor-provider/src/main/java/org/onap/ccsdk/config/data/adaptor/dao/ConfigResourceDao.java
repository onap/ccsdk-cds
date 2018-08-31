package org.onap.ccsdk.config.data.adaptor.dao;


import java.util.List;
import org.onap.ccsdk.config.data.adaptor.domain.ConfigResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public interface ConfigResourceDao {

    /**
     * Issue a single SQL Insert operation for CONFIG_RESOURCE table via a prepared statement, binding
     * the given arguments.
     *
     * @param transactionLog arguments to bind to the query (mapping it to the PreparedStatement to the
     *        corresponding SQL type)
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if there is any problem issuing the insert
     */
    public ConfigResource save(ConfigResource configResourceInput) throws SvcLogicException;

    /**
     * Issue a single SQL Delete operation for CONFIG_RESOURCE table via a prepared statement, binding
     * the given arguments.
     *
     * @param configResource arguments to bind to the query (mapping it to the PreparedStatement to the
     *        corresponding SQL type)
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if there is any problem issuing the insert
     */
    public void deleteByConfigResource(ConfigResource configResourceInput) throws SvcLogicException;

    /**
     * Query CONFIG_RESOURCE table for given input param to create a prepared statement to bind to the
     * query, mapping each row to a Java object via a ConfigResource RowMapper.
     *
     * @param configResource argument to bind to the query (mapping it to the PreparedStatement to the
     *        corresponding SQL type)
     * @return the result List, containing mapped objects
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if the query fails
     */
    public List<ConfigResource> findByConfigResource(ConfigResource configResourceInput) throws SvcLogicException;

    public ConfigResource getConfigResource(ConfigResource configResource) throws SvcLogicException;
}
