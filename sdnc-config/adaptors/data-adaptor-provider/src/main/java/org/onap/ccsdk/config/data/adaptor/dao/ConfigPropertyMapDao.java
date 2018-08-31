package org.onap.ccsdk.config.data.adaptor.dao;

import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public interface ConfigPropertyMapDao {

    /**
     * Query ConcurrentHashMap having CONFIG_PROPERTY_MAP table data for given key.
     *
     * @param key key mapped to a value
     * @return the result string, containing mapped string value
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if fails
     */
    public String getConfigPropertyByKey(String key) throws SvcLogicException;

}
