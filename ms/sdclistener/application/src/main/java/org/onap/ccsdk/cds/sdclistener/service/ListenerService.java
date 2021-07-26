/*
 * Copyright © 2019 Bell Canada
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.cds.sdclistener.service;

import io.grpc.ManagedChannel;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;

import java.nio.file.Path;

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
     * @param managedChannel To access the blueprint processor application end point
     */
    void saveBluePrintToCdsDatabase(Path path, ManagedChannel managedChannel);

    /**
     * Extract and store the csar package to local disk.
     *
     * @param result - IDistributionClientDownloadResult contains payload.
     * @param csarArchivePath The destination path where CSAR will be stored.
     */
    void extractCsarAndStore(IDistributionClientDownloadResult result, Path csarArchivePath);

}
