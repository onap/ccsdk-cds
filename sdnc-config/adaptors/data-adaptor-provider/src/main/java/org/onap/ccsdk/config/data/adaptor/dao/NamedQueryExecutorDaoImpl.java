package org.onap.ccsdk.config.data.adaptor.dao;

import java.util.List;
import java.util.Map;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class NamedQueryExecutorDaoImpl implements NamedQueryExecutorDao {
    private static EELFLogger logger = EELFManager.getInstance().getLogger(QueryExecutorDaoImpl.class);
    private static final String CLASS_NAME = "NamedQueryExecutorDaoImpl";

    @Autowired(required = true)
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public NamedQueryExecutorDaoImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        logger.info("{} Constructor initialised..", CLASS_NAME);
    }

    @Override
    public List<Map<String, Object>> query(String sql, Map<String, Object> parameters) throws SvcLogicException {
        logger.debug("Query  ({}) with parameters ({})", sql, parameters);
        return namedParameterJdbcTemplate.queryForList(sql, parameters);
    }

    @Override
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return namedParameterJdbcTemplate;
    }

    @Override
    public int update(String sql, Map<String, Object> parameters) throws SvcLogicException {
        logger.debug("update ({}) with parameters ({})", sql, parameters);
        return namedParameterJdbcTemplate.update(sql, parameters);
    }
}
