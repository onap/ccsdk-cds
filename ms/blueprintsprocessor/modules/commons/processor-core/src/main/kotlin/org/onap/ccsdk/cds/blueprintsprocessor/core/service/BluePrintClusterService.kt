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
    suspend fun <T> startCluster(configuration: T)

    fun clusterJoined(): Boolean

    fun isClient(): Boolean

    fun isLiteMember(): Boolean

    /** Returns [partitionGroup] master member */
    suspend fun masterMember(partitionGroup: String): ClusterMember

    /** Returns all the data cluster members */
    suspend fun allMembers(): Set<ClusterMember>

    /**
     * Returns application cluster members for [appName] joined as server or lite member,
     * Node joined as client won't be visible. Here the assumption is node-id is combination of
     * application id and replica number, for an example Application cds-cluster then the node ids will be
     * cds-cluster-1, cds-cluster-2, cds-cluster-3
     */
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
    var joinAsClient: Boolean = false,
    var properties: Properties?,
    var configFile: String
)

data class ClusterMember(
    val id: String,
    val name: String,
    val memberAddress: String?,
    val state: String? = null
)

interface ClusterLock {

    fun name(): String
    suspend fun lock()
    suspend fun fenceLock(): String
    suspend fun tryLock(timeout: Long): Boolean
    suspend fun tryFenceLock(timeout: Long): String
    suspend fun unLock()
    fun isLocked(): Boolean
    fun isLockedByCurrentThread(): Boolean
    fun close()
}

const val CDS_LOCK_GROUP = "cds-lock"
