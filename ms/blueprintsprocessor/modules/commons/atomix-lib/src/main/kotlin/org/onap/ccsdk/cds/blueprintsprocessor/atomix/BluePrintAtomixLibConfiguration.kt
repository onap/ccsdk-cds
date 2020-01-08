/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.atomix

import org.onap.ccsdk.cds.blueprintsprocessor.atomix.service.AtomixBluePrintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BluePrintClusterService
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.springframework.context.annotation.Configuration

@Configuration
open class BluePrintAtomixLibConfiguration

/**
 * Exposed Dependency Service by this Atomix Lib Module
 */
fun BluePrintDependencyService.clusterService(): BluePrintClusterService =
    instance(AtomixBluePrintClusterService::class)

/** Optional Cluster Service, returns only if Cluster is enabled */
fun BluePrintDependencyService.optionalClusterService(): BluePrintClusterService? {
    return if (BluePrintConstants.CLUSTER_ENABLED) {
        BluePrintDependencyService.clusterService()
    } else null
}
