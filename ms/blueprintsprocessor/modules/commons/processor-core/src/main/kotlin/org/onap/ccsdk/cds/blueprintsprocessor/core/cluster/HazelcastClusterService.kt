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
import com.hazelcast.config.MemberAttributeConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.cp.CPSubsystemManagementService
import com.hazelcast.cp.lock.FencedLock
import com.hazelcast.scheduledexecutor.IScheduledExecutorService
import com.hazelcast.topic.Message
import com.hazelcast.topic.MessageListener
import kotlinx.coroutines.delay
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BluePrintClusterMessage
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BluePrintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BlueprintClusterMessageListener
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterInfo
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterJoinedEvent
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterLock
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterMember
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.utils.ClusterUtils
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID

import java.util.concurrent.TimeUnit

@Service
open class HazelcastClusterService(private val applicationEventPublisher: ApplicationEventPublisher) : BluePrintClusterService {

    private val log = logger(HazelcastClusterService::class)
    lateinit var hazelcast: HazelcastInstance
    lateinit var cpSubsystemManagementService: CPSubsystemManagementService
    var joinedClient = false
    var joinedLite = false

    override suspend fun <T> startCluster(configuration: T) {
        /** Get the Hazelcast Client or Server instance */
        hazelcast =
            when (configuration) {
                is Config -> {
                    joinedLite = configuration.isLiteMember
                    val hazelcastInstance = Hazelcast.newHazelcastInstance(configuration)
                    /** Promote as CP Member */
                    promoteAsCPMember(hazelcastInstance)
                    hazelcastInstance
                }
                is ClientConfig -> {
                    joinedClient = true
                    HazelcastClient.newHazelcastClient(configuration)
                }
                is ClusterInfo -> {

                    System.setProperty(BluePrintConstants.PROPERTY_CLUSTER_ID, configuration.id)
                    System.setProperty(BluePrintConstants.PROPERTY_CLUSTER_NODE_ID, configuration.nodeId)

                    val memberAttributeConfig = MemberAttributeConfig()
                    memberAttributeConfig.setAttribute(
                        BluePrintConstants.PROPERTY_CLUSTER_NODE_ID,
                        configuration.nodeId
                    )

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
                        hazelcastServerConfiguration.clusterName = configuration.id
                        hazelcastServerConfiguration.instanceName = configuration.nodeId
                        hazelcastServerConfiguration.properties = configuration.properties
                        hazelcastServerConfiguration.memberAttributeConfig = memberAttributeConfig
                        joinedLite = hazelcastServerConfiguration.isLiteMember
                        val hazelcastInstance = Hazelcast.newHazelcastInstance(hazelcastServerConfiguration)
                        /** Promote as CP Member */
                        promoteAsCPMember(hazelcastInstance)
                        hazelcastInstance
                    }
                }
                else -> {
                    throw BluePrintProcessorException("couldn't understand the cluster configuration")
                }
            }

        /** Add the Membership Listeners */
        hazelcast.cluster.addMembershipListener(BlueprintsClusterMembershipListener())
        log.info(
            "Cluster(${hazelcast.config.clusterName}) node(${hazelcast.name}) created successfully...."
        )
        applicationEventPublisher.publishEvent(ClusterJoinedEvent(this))
    }

    override fun isClient(): Boolean {
        return joinedClient
    }

    override fun isLiteMember(): Boolean {
        return joinedLite
    }

    override fun clusterJoined(): Boolean {
        return ::hazelcast.isInitialized && hazelcast.lifecycleService.isRunning
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
            HazelcastClusterUtils.terminate(hazelcast)
        }
    }

    override suspend fun <T> sendMessage(topic: BlueprintClusterTopic, message: T) {
        hazelcast.getReliableTopic<T>(topic.name).publish(message)
    }

    override fun <T> addBlueprintClusterMessageListener(topic: BlueprintClusterTopic, listener: BlueprintClusterMessageListener<T>): UUID {
        log.info("Cluster(${hazelcast.config.clusterName}) node(${hazelcast.name}) listening to topic($topic)...")
        return hazelcast.getReliableTopic<T>(topic.name)
            .addMessageListener(HazelcastMessageListenerAdapter(listener))
    }

    override fun removeBlueprintClusterMessageListener(topic: BlueprintClusterTopic, uuid: UUID): Boolean {
        log.info("Cluster(${hazelcast.config.clusterName}) node(${hazelcast.name}) has stopped listening to topic($topic)...")
        return hazelcast.getReliableTopic<Any>(topic.name).removeMessageListener(uuid)
    }

    /** Utils */
    suspend fun promoteAsCPMember(hazelcastInstance: HazelcastInstance) {
        if (!joinedClient && !joinedLite) {
            HazelcastClusterUtils.promoteAsCPMember(hazelcastInstance)
        }
    }

    suspend fun myHazelcastApplicationMembers(): Map<String, Member> {
        check(::hazelcast.isInitialized) { "failed to start and join cluster" }
        check(!isClient()) { "not supported for cluster client members." }
        return hazelcastApplicationMembers(ClusterUtils.applicationName())
    }

    suspend fun hazelcastApplicationMembers(appName: String): Map<String, Member> {
        check(::hazelcast.isInitialized) { "failed to start and join cluster" }
        val applicationMembers: MutableMap<String, Member> = hashMapOf()
        hazelcast.cluster.members.map { member ->
            val memberName: String = member.getAttribute(BluePrintConstants.PROPERTY_CLUSTER_NODE_ID)
            if (memberName.startsWith(appName, true)) {
                applicationMembers[memberName] = member
            }
        }
        return applicationMembers
    }
}

open class BlueprintsClusterMembershipListener() :
    MembershipListener {

    private val log = logger(BlueprintsClusterMembershipListener::class)

    override fun memberRemoved(membershipEvent: MembershipEvent) {
        log.info("MembershipEvent: $membershipEvent")
    }

    override fun memberAdded(membershipEvent: MembershipEvent) {
        log.info("MembershipEvent: $membershipEvent")
    }
}

open class ClusterLockImpl(private val hazelcast: HazelcastInstance, private val name: String) : ClusterLock {

    private val log = logger(ClusterLockImpl::class)

    private val distributedLock: FencedLock = hazelcast.cpSubsystem.getLock(name)

    override fun name(): String {
        return distributedLock.name
    }

    override suspend fun lock() {
        distributedLock.lock()
        log.trace("Cluster lock($name) created..")
    }

    override suspend fun tryLock(timeout: Long): Boolean {
        return distributedLock.tryLock(timeout, TimeUnit.MILLISECONDS)
            .also {
                if (it) log.trace("Cluster lock acquired: $name")
                else log.trace("Failed to acquire Cluster lock $name within timeout $timeout")
            }
    }

    override suspend fun unLock() {
        distributedLock.unlock()
        log.trace("Cluster unlock(${name()}) successfully..")
    }

    override fun isLocked(): Boolean {
        return distributedLock.isLocked
    }

    override fun isLockedByCurrentThread(): Boolean {
        return distributedLock.isLockedByCurrentThread
    }

    override suspend fun fenceLock(): String {
        val fence = distributedLock.lockAndGetFence()
        log.trace("Cluster lock($name) fence($fence) created..")
        return fence.toString()
    }

    override suspend fun tryFenceLock(timeout: Long): String {
        return distributedLock.tryLockAndGetFence(timeout, TimeUnit.MILLISECONDS).toString()
    }

    override fun close() {
    }
}

class HazelcastMessageListenerAdapter<E>(val listener: BlueprintClusterMessageListener<E>) : MessageListener<E> {
    override fun onMessage(message: Message<E>?) = message?.let {
        BluePrintClusterMessage<E>(
            BlueprintClusterTopic.valueOf(it.source as String),
            it.messageObject,
            it.publishTime,
            it.publishingMember.toClusterMember()
        )
    }.let { listener.onMessage(it) }
}
