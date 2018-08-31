package org.onap.ccsdk.config.rest.adaptor;

/**
 * ConfigRestAdaptorException.java Purpose: Provide Configuration Rest Adaptor Exception
 *
 * @author Kapil Singal
 * @version 1.0
 */
public class ConfigRestAdaptorException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * This is a ConfigRestAdaptorException constructor
     *
     * @param message
     */
    public ConfigRestAdaptorException(String message) {
        super(message);
    }

    /**
     * This is a ConfigRestAdaptorException constructor
     *
     * @param message
     */
    public ConfigRestAdaptorException(String message, Throwable cause) {
        super(message, cause);
    }



}
