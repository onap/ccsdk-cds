package org.onap.ccsdk.config.data.adaptor.domain;

import java.io.Serializable;

public class ConfigPropertyMapData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String referenceKey;
    private String referenceValue;

    public String getReferenceKey() {
        return referenceKey;
    }

    public void setReferenceKey(String referenceKey) {
        this.referenceKey = referenceKey;
    }

    public String getReferenceValue() {
        return referenceValue;
    }

    public void setReferenceValue(String referenceValue) {
        this.referenceValue = referenceValue;
    }

    @Override
    public String toString() {
        return "ConfigPropertyMapData [referenceKey=" + referenceKey + ", referenceValue=" + referenceValue + "]";
    }

}
