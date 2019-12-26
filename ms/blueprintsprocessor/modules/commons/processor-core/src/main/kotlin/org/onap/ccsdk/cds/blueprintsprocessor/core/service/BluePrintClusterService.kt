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

interface BluePrintClusterService {

    /** Start the cluster with [clusterInfo], By default clustering service is disabled.
     * Application module has to start cluster */
    suspend fun startCluster(clusterInfo: ClusterInfo)

    fun clusterJoined(): Boolean

    /** Returns [partitionGroup] master member */
    suspend fun masterMember(partitionGroup: String): ClusterMember

    /** Returns all the data cluster members */
    suspend fun allMembers(): Set<ClusterMember>

    /** Returns data cluster members starting with prefix */
    suspend fun clusterMembersForPrefix(memberPrefix: String): Set<ClusterMember>

    /** Create and get or get the distributed data map store with [name] */
    suspend fun <T> clusterMapStore(name: String): MutableMap<String, T>

    /** Create and get the distributed lock with [name] */
    suspend fun clusterLock(name: String): ClusterLock

    /** Shut down the cluster with [duration] */
    suspend fun shutDown(duration: Duration)
}

data class ClusterInfo(
    val id: String,
    var configFile: String? = null,
    val nodeId: String,
    val nodeAddress: String,
    var clusterMembers: List<String>,
    var storagePath: String
)

data class ClusterMember(val id: String, val memberAddress: String?, val state: String? = null)

interface ClusterLock {
    suspend fun lock()
    suspend fun tryLock(timeout: Long): Boolean
    suspend fun unLock()
    fun isLocked(): Boolean
    fun close()
}
