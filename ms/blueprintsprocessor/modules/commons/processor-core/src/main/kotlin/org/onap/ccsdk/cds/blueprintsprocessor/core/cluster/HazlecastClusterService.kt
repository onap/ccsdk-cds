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

import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.client.config.YamlClientConfigBuilder
import com.hazelcast.cluster.Member
import com.hazelcast.cluster.MembershipEvent
import com.hazelcast.cluster.MembershipListener
import com.hazelcast.config.Config
import com.hazelcast.config.FileSystemYamlConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.cp.lock.FencedLock
import com.hazelcast.map.IMap
import com.hazelcast.scheduledexecutor.IScheduledExecutorService
import kotlinx.coroutines.delay
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BluePrintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterInfo
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterLock
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterMember
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.utils.ClusterUtils
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.TimeUnit

@Service
open class HazlecastClusterService : BluePrintClusterService {

    private val log = logger(HazlecastClusterService::class)
    lateinit var hazelcast: HazelcastInstance
    lateinit var memberMap: IMap<String, Member>
    var joinedClient = false
    var joinedLite = false

    override suspend fun <T> startCluster(configuration: T) {
        /** Get the Hazelcast Cliet or Server instance */
        hazelcast =
            when (configuration) {
                is Config -> {
                    joinedLite = configuration.isLiteMember
                    Hazelcast.newHazelcastInstance(configuration)
                }
                is ClientConfig -> {
                    joinedClient = true
                    HazelcastClient.newHazelcastClient(configuration)
                }
                is ClusterInfo -> {

                    System.setProperty("cluster-id", configuration.id)
                    System.setProperty("node-id", configuration.nodeId)

                    val configFile = configuration.configFile
                    /** Check file exists */
                    val clusterConfigFile = normalizedFile(configuration.configFile)
                    check(clusterConfigFile.absolutePath.endsWith("yaml", true)) {
                        "couldn't understand cluster config file(${configuration.configFile}) format, it should be yaml"
                    }
                    check(clusterConfigFile.exists()) {
                        "couldn't file cluster configuration file(${clusterConfigFile.absolutePath})"
                    }
                    log.info("****** Cluster configuration file(${clusterConfigFile.absolutePath}) ****")

                    /** Hazelcast Client from config file */
                    if (configuration.joinAsClient) {
                        /** Set the configuration file to system properties, so that Hazelcast will read automatically */
                        System.setProperty("hazelcast.client.config", clusterConfigFile.absolutePath)
                        joinedClient = true
                        val hazelcastClientConfiguration = YamlClientConfigBuilder().build()
                        hazelcastClientConfiguration.properties = configuration.properties
                        HazelcastClient.newHazelcastClient(hazelcastClientConfiguration)
                    } else {
                        /** Hazelcast Server from config file */
                        val hazelcastServerConfiguration = FileSystemYamlConfig(normalizedFile(configFile))
                        hazelcastServerConfiguration.properties = configuration.properties
                        joinedLite = hazelcastServerConfiguration.isLiteMember
                        Hazelcast.newHazelcastInstance(hazelcastServerConfiguration)
                    }
                }
                else -> {
                    throw BluePrintProcessorException("couldn't understand the cluster configuration")
                }
            }

        /** Add the Membership Listeners */
        hazelcast.cluster.addMembershipListener(BlueprintsClusterMembershipListener(this))
        log.info(
            "Cluster(${hazelcast.config.clusterName}) node(${hazelcast.name}) created successfully...."
        )

        /** Register self node name to member */
        memberMap = hazelcast.getMap<String, Member>("member-name-map")
        /** If joined as client, Local member wont be available */
        if (!joinedClient) {
            memberMap[hazelcast.name] = hazelcast.cluster.localMember
        }
    }

    override fun isClient(): Boolean {
        return joinedClient
    }

    override fun isLiteMember(): Boolean {
        return joinedLite
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
        check(!isClient()) { "local member is client, join as server to get this functionality." }
        return hazelcastApplicationMembers(appName).mapNotNull { it.value.toClusterMember() }.toSet()
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

    /** Return interface may change and it will be included in BluePrintClusterService */
    @UseExperimental
    suspend fun clusterScheduler(name: String): IScheduledExecutorService {
        check(::hazelcast.isInitialized) { "failed to start and join cluster" }
        return hazelcast.getScheduledExecutorService(name)
    }

    override suspend fun shutDown(duration: Duration) {
        if (::hazelcast.isInitialized && clusterJoined()) {
            delay(duration.toMillis())
            hazelcast.lifecycleService.terminate()
        }
    }

    /** Utils */
    suspend fun myHazelcastApplicationMembers(): Map<String, Member> {
        check(::hazelcast.isInitialized) { "failed to start and join cluster" }
        return memberMap.filter { it.key.startsWith(ClusterUtils.applicationName()) }
    }

    suspend fun hazelcastApplicationMembers(appName: String): Map<String, Member> {
        check(::hazelcast.isInitialized) { "failed to start and join cluster" }
        return memberMap.filter { it.key.startsWith(appName) }
    }
}

open class BlueprintsClusterMembershipListener(val hazlecastClusterService: HazlecastClusterService) :
    MembershipListener {
    private val log = logger(BlueprintsClusterMembershipListener::class)

    override fun memberRemoved(membershipEvent: MembershipEvent) {
        log.info("${hazlecastClusterService.hazelcast.cluster.localMember} : Member Removed: $membershipEvent")
        /** Remove the disconnected members from the store */
        val toRemoveKeys =
            hazlecastClusterService.memberMap.filter { it.value == membershipEvent.member }.map { it.key }
        toRemoveKeys.forEach { hazlecastClusterService.memberMap.remove(it) }
    }

    override fun memberAdded(membershipEvent: MembershipEvent) {
        log.info("${hazlecastClusterService.hazelcast.cluster.localMember} : Member Added : $membershipEvent")
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
