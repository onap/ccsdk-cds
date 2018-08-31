package org.onap.ccsdk.config.data.adaptor.dao;

import java.util.List;
import java.util.Map;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class QueryExecutorDaoImpl implements QueryExecutorDao {

    private static EELFLogger logger = EELFManager.getInstance().getLogger(QueryExecutorDaoImpl.class);
    private static final String CLASS_NAME = "QueryExecutorDaoImpl";

    @Autowired(required = true)
    private JdbcTemplate jdbcTemplate;

    public QueryExecutorDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        logger.info("{} Constructor initialised..", CLASS_NAME);
    }

    @Override
    public List<Map<String, Object>> query(String sql, Object[] data) throws SvcLogicException {
        logger.debug("Query  ({}) with data ({})", sql, data);
        return jdbcTemplate.queryForList(sql, data);
    }

    @Override
    public int update(String sql, Object[] data) throws SvcLogicException {
        logger.debug("Query  ({}) with data ({})", sql, data);
        return jdbcTemplate.update(sql, data);
    }

}
