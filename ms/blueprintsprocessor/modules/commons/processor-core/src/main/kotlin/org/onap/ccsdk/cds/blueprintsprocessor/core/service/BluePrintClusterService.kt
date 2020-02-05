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

package org.onap.ccsdk.cds.blueprintsprocessor.core.service

import java.time.Duration
import java.util.Properties

interface BluePrintClusterService {

    /** Start the cluster with [clusterInfo], By default clustering service is disabled.
     * Application module has to start cluster */
    suspend fun startCluster(clusterInfo: ClusterInfo)

    fun clusterJoined(): Boolean

    /** Returns [partitionGroup] master member */
    suspend fun masterMember(partitionGroup: String): ClusterMember

    /** Returns all the data cluster members */
    suspend fun allMembers(): Set<ClusterMember>

    /** Returns application cluster members for [appName] */
    suspend fun applicationMembers(appName: String): Set<ClusterMember>

    /** Create and get or get the distributed data map store with [name] */
    suspend fun <T> clusterMapStore(name: String): MutableMap<String, T>

    /** Create and get the distributed lock with [name] */
    suspend fun clusterLock(name: String): ClusterLock

    /** Shut down the cluster with [duration] */
    suspend fun shutDown(duration: Duration)
}

data class ClusterInfo(
    val id: String,
    val nodeId: String,
    var discoverPlatform: DiscoveryPlatform = DiscoveryPlatform.DOCKER_COMPOSE, // DOCKER_COMPOSE, KUBERNETES, AZURE, GCP, OPEN_STACK
    var joinAsClient: Boolean = false,
    var managementMemberCount: Int = 3,
    var managementGroupCount: Int = -1,
    var properties: Properties?,
    var configFile: String? = null
)

data class ClusterMember(
    val id: String,
    val memberAddress: String?,
    val state: String? = null
)

enum class DiscoveryPlatform { DOCKER_COMPOSE, AWS, AZURE, GCP, EUREKA, KUBERNETES }

interface ClusterLock {
    fun name(): String
    suspend fun lock()
    suspend fun fenceLock(): String
    suspend fun tryLock(timeout: Long): Boolean
    suspend fun tryFenceLock(timeout: Long): String
    suspend fun unLock()
    fun isLocked(): Boolean
    fun close()
}
