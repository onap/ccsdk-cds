package org.onap.ccsdk.config.data.adaptor.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.data.adaptor.DataAdaptorConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.springframework.jdbc.core.JdbcTemplate;
import com.google.common.base.Preconditions;

public class ConfigPropertyMapDaoImpl implements ConfigPropertyMapDao {

    private static EELFLogger logger = EELFManager.getInstance().getLogger(ConfigPropertyMapDaoImpl.class);

    private JdbcTemplate jdbcTemplate;
    private Map<String, String> configPropertyMap = new ConcurrentHashMap<>();

    public ConfigPropertyMapDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        initializeMap();

        String envType = configPropertyMap.get(DataAdaptorConstants.PROPERTY_ENV_TYPE);
        if (!(DataAdaptorConstants.PROPERTY_ENV_PROD.equalsIgnoreCase(envType)
                || DataAdaptorConstants.PROPERTY_ENV_SOLO.equalsIgnoreCase(envType))) {
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            Runnable task = () -> {
                initializeMap();
            };
            executor.scheduleWithFixedDelay(task, 60, 15, TimeUnit.MINUTES);
        }
    }

    private void initializeMap() {
        String getPropQuery = "SELECT * FROM CONFIG_PROPERTY_MAP";
        jdbcTemplate.queryForList(getPropQuery).forEach(rows -> {
            String key = StringUtils.trimToEmpty((String) rows.get("reference_key"));
            String value = StringUtils.trimToEmpty((String) rows.get("reference_value"));
            configPropertyMap.put(key, value);
        });
        logger.trace("loaded configPropertyMap : ({})", configPropertyMap);
    }

    @Override
    public String getConfigPropertyByKey(String key) throws SvcLogicException {
        Preconditions.checkArgument(StringUtils.isNotBlank(key), "missing property key");
        return configPropertyMap.get(key);
    }

}
