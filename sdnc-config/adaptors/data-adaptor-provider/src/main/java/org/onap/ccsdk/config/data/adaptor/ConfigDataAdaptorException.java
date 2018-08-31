package org.onap.ccsdk.config.data.adaptor;

/**
 * ConfigDataAdaptorException.java Purpose: Provide Configuration Data Adaptor Exception
 *
 * @author Kapil Singal
 * @version 1.0
 */
public class ConfigDataAdaptorException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * This is a ConfigDataAdaptorException constructor
     *
     * @param message
     */
    public ConfigDataAdaptorException(String message) {
        super(message);
    }

    /**
     * This is a ConfigDataAdaptorException constructor
     *
     * @param message
     */
    public ConfigDataAdaptorException(String message, Throwable cause) {
        super(message, cause);
    }


}
