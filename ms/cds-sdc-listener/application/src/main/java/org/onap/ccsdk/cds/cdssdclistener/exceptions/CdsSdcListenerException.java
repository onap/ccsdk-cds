/*
 * Copyright (C) 2019 Bell Canada. All rights reserved.
 *
 * NOTICE:  All the intellectual and technical concepts contained herein are
 * proprietary to Bell Canada and are protected by trade secret or copyright law.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */

package org.onap.ccsdk.cds.cdssdclistener.exceptions;

public class CdsSdcListenerException extends Exception {

    /**
     * @param message The message to dump
     */
    public CdsSdcListenerException(final String message) {
        super(message);
    }

    /**
     * @param message The message to dump
     * @param cause The Throwable cause object
     */
    public CdsSdcListenerException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
