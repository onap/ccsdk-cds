/*
 * Copyright (C) 2019 Bell Canada. All rights reserved.
 *
 * NOTICE:  All the intellectual and technical concepts contained herein are
 * proprietary to Bell Canada and are protected by trade secret or copyright law.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */

package org.onap.ccsdk.cds.cdssdclistener.service;

import java.io.File;

public interface ListenerService {

    void extractBluePrint(File file);

    void storeBluePrint(File file);
}
