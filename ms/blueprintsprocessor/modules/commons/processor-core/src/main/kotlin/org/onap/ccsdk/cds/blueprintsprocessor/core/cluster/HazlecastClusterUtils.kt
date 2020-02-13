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

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.cp.CPGroup
import com.hazelcast.cp.CPMember
import com.hazelcast.cp.CPSubsystemManagementService
import com.hazelcast.instance.impl.HazelcastInstanceProxy
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import java.util.UUID
import java.util.concurrent.TimeUnit

object HazlecastClusterUtils {

    private val log = logger(HazlecastClusterUtils::class)

    /** Promote [hazelcastInstance] member to CP Member */
    fun promoteAsCPMember(hazelcastInstance: HazelcastInstance) {
        when (hazelcastInstance) {
            is HazelcastInstanceProxy -> {
                val cpSubsystemManagementService = cpSubsystemManagementService(hazelcastInstance)
                cpSubsystemManagementService.promoteToCPMember()
                    .toCompletableFuture().get()
                log.info("Promoted as CP member(${hazelcastInstance.cluster.localMember})")
                val clusterCPMembers = clusterCPMembers(hazelcastInstance)
                log.info("CP Members(${clusterCPMembers.size}): $clusterCPMembers")
                val cpGroupMembers = cpGroupMembers(hazelcastInstance)
                log.info("CP Group Members(${cpGroupMembers.size}): $cpGroupMembers")
            }
            else -> log.debug("Client instance not eligible for CP Member promotion")
        }
    }

    /** Terminate [hazelcastInstance] member */
    fun terminate(hazelcastInstance: HazelcastInstance) {
        log.info("Terminating Member : ${hazelcastInstance.cluster.localMember}")
        hazelcastInstance.lifecycleService.terminate()
    }

    /** Remove [hazelcastInstance] member from cluster CP Member List*/
    fun removeFromCPMember(hazelcastInstance: HazelcastInstance) {
        // check CP Member, then remove */
        val localCPMemberUuid = localCPMemberUUID(hazelcastInstance)
        localCPMemberUuid?.let { uuid ->
            removeFromCPMember(hazelcastInstance, uuid)
        }
    }

    /** Remove [removeCPMemberUuid] member from cluster CP Member List, using [hazelcastInstance]*/
    fun removeFromCPMember(hazelcastInstance: HazelcastInstance, removeCPMemberUuid: UUID) {
        val cpSubsystemManagementService = cpSubsystemManagementService(hazelcastInstance)
        cpSubsystemManagementService.removeCPMember(removeCPMemberUuid)
            .toCompletableFuture().get()
        log.info("Removed CP member($removeCPMemberUuid)")
    }

    /** Get [hazelcastInstance] CP Group members*/
    fun cpGroupMembers(hazelcastInstance: HazelcastInstance): List<CPMember> {
        return cpGroup(hazelcastInstance).members().toList()
    }

    /** Get [hazelcastInstance] CP Group[groupName] members*/
    fun cpGroup(
        hazelcastInstance: HazelcastInstance,
        groupName: String? = CPGroup.METADATA_CP_GROUP_NAME
    ): CPGroup {
        return cpSubsystemManagementService(hazelcastInstance).getCPGroup(groupName)
            .toCompletableFuture().get()
    }

    /** Get [hazelcastInstance] CP member UUIDs*/
    fun clusterCPMemberUUIDs(hazelcastInstance: HazelcastInstance): List<UUID> {
        return clusterCPMembers(hazelcastInstance).map { it.uuid }
    }

    /** Get [hazelcastInstance] CP members*/
    fun clusterCPMembers(hazelcastInstance: HazelcastInstance): List<CPMember> {
        return cpSubsystemManagementService(hazelcastInstance).cpMembers.toCompletableFuture().get().toList()
    }

    /** Get CPSubsystemManagementService for [hazelcastInstance] */
    fun cpSubsystemManagementService(hazelcastInstance: HazelcastInstance): CPSubsystemManagementService {
        val cpSubsystemManagementService = hazelcastInstance.cpSubsystem.cpSubsystemManagementService
        cpSubsystemManagementService.awaitUntilDiscoveryCompleted(3, TimeUnit.MINUTES)
        return cpSubsystemManagementService
    }

    /** Get local CPMemberUUID for [hazelcastInstance] */
    fun localCPMemberUUID(hazelcastInstance: HazelcastInstance) = localCPMember(hazelcastInstance)?.uuid

    /** Check local member is CP member for [hazelcastInstance] */
    fun checkLocalMemberIsCPMember(hazelcastInstance: HazelcastInstance): Boolean {
        return localCPMember(hazelcastInstance) != null
    }

    /** Get local CP member for [hazelcastInstance] */
    fun localCPMember(hazelcastInstance: HazelcastInstance) =
        cpSubsystemManagementService(hazelcastInstance).localCPMember

    /** Get local CP member UUID for [hazelcastInstance] */
    fun localMemberUUID(hazelcastInstance: HazelcastInstance) = hazelcastInstance.cluster.localMember.uuid
}
