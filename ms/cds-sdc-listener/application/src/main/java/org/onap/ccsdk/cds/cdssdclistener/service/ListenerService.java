/*
 * Copyright (C) 2019 Bell Canada. All rights reserved.
 *
 * NOTICE:  All the intellectual and technical concepts contained herein are
 * proprietary to Bell Canada and are protected by trade secret or copyright law.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */

package org.onap.ccsdk.cds.cdssdclistener.service;

import java.nio.file.Path;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;

public interface ListenerService {

    /**
     * Get the controller blueprint archive from CSAR package.
     *
     * @param csarArchivePath The path where CSAR archive is stored.
     * @param cbaArchivePath The destination path where CBA will be stored.
     */
    void extractBluePrint(String csarArchivePath, String cbaArchivePath);

    /**
     * Store the Zip file into CDS database.
     *
     * @param path path where zip file exists.
     */
    void saveBluePrintToCdsDatabase(Path path);

    /**
     * Extract and store CSAR to file.
     *
     * @param result - IDistributionClientDownloadResult contains payload.
     * @param csarArchivePath The destination path where CSAR will be stored.
     */
    void extractCsarAndStore(IDistributionClientDownloadResult result, String csarArchivePath);
}
