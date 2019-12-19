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

package org.onap.ccsdk.cds.blueprintsprocessor.atomix.service

import io.atomix.cluster.ClusterMembershipEvent
import io.atomix.core.Atomix
import io.atomix.core.lock.DistributedLock
import kotlinx.coroutines.delay
import org.onap.ccsdk.cds.blueprintsprocessor.atomix.utils.AtomixLibUtils
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BluePrintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterInfo
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterLock
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterMember
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.CompletableFuture

@Service
open class AtomixBluePrintClusterService : BluePrintClusterService {

    private val log = logger(AtomixBluePrintClusterService::class)

    lateinit var atomix: Atomix

    override suspend fun startCluster(clusterInfo: ClusterInfo) {
        log.info(
            "Cluster(${clusterInfo.id}) node(${clusterInfo.nodeId}), node address(${clusterInfo.nodeAddress}) " +
                "starting with members(${clusterInfo.clusterMembers})"
        )

        /** Create Atomix cluster either from config file or default multi-cast cluster*/
        atomix = if (!clusterInfo.configFile.isNullOrEmpty()) {
            AtomixLibUtils.configAtomix(clusterInfo.configFile!!)
        } else {
            AtomixLibUtils.defaultMulticastAtomix(clusterInfo)
        }

        /** Listen for the member chaneg events */
        atomix.membershipService.addListener { membershipEvent ->
            when (membershipEvent.type()) {
                ClusterMembershipEvent.Type.MEMBER_ADDED -> log.info("Member Added : ${membershipEvent.subject()}")
                ClusterMembershipEvent.Type.MEMBER_REMOVED -> log.info("Member Removed: ${membershipEvent.subject()}")
                ClusterMembershipEvent.Type.METADATA_CHANGED -> log.info("Changed : ${membershipEvent.subject()}")
                else -> log.info("Member event unknown")
            }
        }
        atomix.start().join()
        log.info(
            "Cluster(${clusterInfo.id}) node(${clusterInfo.nodeId}), node address(${clusterInfo.nodeAddress}) " +
                "created successfully...."
        )

        /** Receive ping from network */
        val pingHandler = { message: String ->
            log.info("####### ping message received : $message")
            CompletableFuture.completedFuture(message)
        }
        atomix.communicationService.subscribe("ping", pingHandler)

        /** Ping the network */
        atomix.communicationService.broadcast(
            "ping",
            "ping from node(${clusterInfo.nodeId})"
        )
    }

    override fun clusterJoined(): Boolean {
        return atomix.isRunning
    }

    override suspend fun allMembers(): Set<ClusterMember> {
        check(::atomix.isInitialized) { "failed to start and join cluster" }
        check(atomix.isRunning) { "cluster is not running" }

        return atomix.membershipService.members.map {
            ClusterMember(
                id = it.id().id(),
                memberAddress = it.host()
            )
        }.toSet()
    }

    override suspend fun clusterMembersForPrefix(memberPrefix: String): Set<ClusterMember> {
        check(::atomix.isInitialized) { "failed to start and join cluster" }
        check(atomix.isRunning) { "cluster is not running" }

        return atomix.membershipService.members.filter {
            it.id().id().startsWith(memberPrefix, true)
        }.map { ClusterMember(id = it.id().id(), memberAddress = it.host()) }
            .toSet()
    }

    override suspend fun <T> clusterMapStore(name: String): MutableMap<String, T> {
        check(::atomix.isInitialized) { "failed to start and join cluster" }
        return AtomixLibUtils.distributedMapStore<T>(atomix, name)
    }

    /** The DistributedLock is a distributed implementation of Java’s Lock.
     * This API provides monotonically increasing, globally unique lock instance identifiers that can be used to
     * determine ordering of multiple concurrent lock holders.
     * DistributedLocks are designed to account for failures within the cluster.
     * When a lock holder crashes or becomes disconnected from the partition by which the lock’s state is controlled,
     * the lock will be released and granted to the next waiting process.     *
     */
    override suspend fun clusterLock(name: String): ClusterLock {
        check(::atomix.isInitialized) { "failed to start and join cluster" }
        return ClusterLockImpl(atomix, name)
    }

    override suspend fun shutDown(duration: Duration) {
        if (::atomix.isInitialized) {
            val shutDownMilli = duration.toMillis()
            log.info("Received cluster shutdown request, shutdown in ($shutDownMilli)ms")
            delay(shutDownMilli)
            atomix.stop()
        }
    }
}

open class ClusterLockImpl(private val atomix: Atomix, private val name: String) : ClusterLock {

    lateinit var distributedLock: DistributedLock

    override suspend fun lock() {
        distributedLock = AtomixLibUtils.distributedLock(atomix, name)
        distributedLock.lock()
    }

    override suspend fun tryLock(timeout: Long): Boolean {
        distributedLock = AtomixLibUtils.distributedLock(atomix, name)
        return distributedLock.tryLock(Duration.ofMillis(timeout))
    }

    override suspend fun unLock() {
        distributedLock.unlock()
    }

    override fun isLocked(): Boolean {
        return distributedLock.isLocked
    }
}
