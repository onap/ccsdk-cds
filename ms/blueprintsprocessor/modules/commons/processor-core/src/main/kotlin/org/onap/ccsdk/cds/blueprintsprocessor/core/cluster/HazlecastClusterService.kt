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

package org.onap.ccsdk.cds.blueprintsprocessor.core.cluster

import com.hazelcast.config.Config
import com.hazelcast.config.GroupConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.MemberAttributeEvent
import com.hazelcast.core.MembershipEvent
import com.hazelcast.core.MembershipListener
import com.hazelcast.cp.lock.FencedLock
import kotlinx.coroutines.delay
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BluePrintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterInfo
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterLock
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterMember
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.DiscoveryPlatform
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.TimeUnit

@Service
open class HazlecastClusterService : BluePrintClusterService {

    private val log = logger(HazlecastClusterService::class)
    lateinit var hazelcast: HazelcastInstance

    override suspend fun startCluster(clusterInfo: ClusterInfo) {
        val config = Config()
        /** Set Cluster Name and their Cluster Groups*/
        config.groupConfig = GroupConfig(clusterInfo.id)
        config.instanceName = clusterInfo.nodeId
        config.isLiteMember = clusterInfo.joinAsClient

        config.properties = clusterInfo.properties
        config.cpSubsystemConfig.cpMemberCount = clusterInfo.managementMemberCount
        if (clusterInfo.managementGroupCount > -1) {
            config.cpSubsystemConfig.groupSize = clusterInfo.managementGroupCount
        }

        val joinConfig = config.networkConfig.join
        when (clusterInfo.discoverPlatform) {
            DiscoveryPlatform.KUBERNETES -> {
                joinConfig.multicastConfig.isEnabled = false
                joinConfig.kubernetesConfig.isEnabled = true
            }
            DiscoveryPlatform.AWS -> {
                joinConfig.multicastConfig.isEnabled = false
                joinConfig.awsConfig.isEnabled = true
            }
            DiscoveryPlatform.AZURE -> {
                joinConfig.multicastConfig.isEnabled = false
                joinConfig.azureConfig.isEnabled = true
            }
            DiscoveryPlatform.GCP -> {
                joinConfig.multicastConfig.isEnabled = false
                joinConfig.gcpConfig.isEnabled = true
            }
            DiscoveryPlatform.EUREKA -> {
                joinConfig.multicastConfig.isEnabled = false
                joinConfig.eurekaConfig.isEnabled = true
            }
            DiscoveryPlatform.DOCKER_COMPOSE -> {
            }
        }
        hazelcast = Hazelcast.newHazelcastInstance(config)
        /** Add the Membership Listeners */
        hazelcast.cluster.addMembershipListener(BlueprintsClusterMembershipListener(hazelcast))
        log.info(
            "Cluster(${clusterInfo.id}) node(${clusterInfo.nodeId}), " +
                "discovery(${clusterInfo.discoverPlatform.name}) created successfully...."
        )
    }

    override fun clusterJoined(): Boolean {
        return hazelcast.lifecycleService.isRunning
    }

    override suspend fun masterMember(partitionGroup: String): ClusterMember {
        check(::hazelcast.isInitialized) { "failed to start and join cluster" }
        return hazelcast.cluster.members.first().toClusterMember()
    }

    override suspend fun allMembers(): Set<ClusterMember> {
        check(::hazelcast.isInitialized) { "failed to start and join cluster" }
        return hazelcast.cluster.members.map { it.toClusterMember() }.toSet()
    }

    override suspend fun applicationMembers(appName: String): Set<ClusterMember> {
        check(::hazelcast.isInitialized) { "failed to start and join cluster" }
        // FIXME("Later")
        return allMembers()
    }

    override suspend fun <T> clusterMapStore(name: String): MutableMap<String, T> {
        check(::hazelcast.isInitialized) { "failed to start and join cluster" }
        return hazelcast.getMap<String, T>(name)
    }

    /**
     * The DistributedLock is a distributed implementation of Java’s Lock.
     * This API provides monotonically increasing, globally unique lock instance identifiers that can be used to
     * determine ordering of multiple concurrent lock holders.
     * DistributedLocks are designed to account for failures within the cluster.
     * When a lock holder crashes or becomes disconnected from the partition by which the lock’s state is controlled,
     * the lock will be released and granted to the next waiting process.
     */
    override suspend fun clusterLock(name: String): ClusterLock {
        check(::hazelcast.isInitialized) { "failed to start and join cluster" }
        return ClusterLockImpl(hazelcast, name)
    }

    override suspend fun shutDown(duration: Duration) {
        if (::hazelcast.isInitialized && clusterJoined()) {
            delay(duration.toMillis())
            hazelcast.lifecycleService.terminate()
        }
    }
}

open class BlueprintsClusterMembershipListener(val hazelcast: HazelcastInstance) : MembershipListener {
    private val log = logger(BlueprintsClusterMembershipListener::class)

    override fun memberRemoved(membershipEvent: MembershipEvent) {
        log.info("${hazelcast.cluster.localMember} : Member Removed: $membershipEvent")
    }

    override fun memberAdded(membershipEvent: MembershipEvent) {
        log.info("${hazelcast.cluster.localMember} : Member Added : $membershipEvent")
    }

    override fun memberAttributeChanged(memberAttributeEvent: MemberAttributeEvent) {
        log.info("${hazelcast.cluster.localMember} : Changed : ${memberAttributeEvent.eventType} : $memberAttributeEvent")
    }
}

open class ClusterLockImpl(private val hazelcast: HazelcastInstance, private val name: String) : ClusterLock {
    private val log = logger(ClusterLockImpl::class)

    lateinit var distributedLock: FencedLock

    override fun name(): String {
        return distributedLock.name
    }

    override suspend fun lock() {
        distributedLock = hazelcast.cpSubsystem.getLock(name)
        distributedLock.lock()
        log.trace("Cluster lock($name) created..")
    }

    override suspend fun tryLock(timeout: Long): Boolean {
        distributedLock = hazelcast.cpSubsystem.getLock(name)
        return distributedLock.tryLock(timeout, TimeUnit.MILLISECONDS)
    }

    override suspend fun unLock() {
        distributedLock.unlock()
        log.trace("Cluster unlock(${name()}) successfully..")
    }

    override fun isLocked(): Boolean {
        return distributedLock.isLocked
    }

    override suspend fun fenceLock(): String {
        distributedLock = hazelcast.cpSubsystem.getLock(name)
        val fence = distributedLock.lockAndGetFence()
        log.trace("Cluster lock($name) fence($fence) created..")
        return fence.toString()
    }

    override suspend fun tryFenceLock(timeout: Long): String {
        distributedLock = hazelcast.cpSubsystem.getLock(name)
        return distributedLock.tryLockAndGetFence(timeout, TimeUnit.MILLISECONDS).toString()
    }

    override fun close() {
    }
}
