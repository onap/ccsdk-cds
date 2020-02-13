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
import com.hazelcast.cp.CPSubsystemManagementService
import com.hazelcast.instance.impl.HazelcastInstanceProxy
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import java.util.UUID
import java.util.concurrent.TimeUnit

object HazlecastClusterUtils {

    private val log = logger(HazlecastClusterUtils::class)

    fun promoteAsCPMember(hazelcastInstance: HazelcastInstance) {
        when (hazelcastInstance) {
            is HazelcastInstanceProxy -> {
                val cpSubsystemManagementService = cpSubsystemManagementService(hazelcastInstance)
                cpSubsystemManagementService.promoteToCPMember()
                    .toCompletableFuture().get()
                log.info("Promoted as CP member(${hazelcastInstance.cluster.localMember})")
            }
            else -> log.info("${hazelcastInstance.javaClass} CP Member promote is not supported")
        }
    }

    fun removeFromCPMember(hazelcastInstance: HazelcastInstance) {
        removeFromCPMember(hazelcastInstance, localMemberUuid(hazelcastInstance))
    }

    fun removeFromCPMember(hazelcastInstance: HazelcastInstance, removeMemberUuid: UUID) {
        val cpSubsystemManagementService = cpSubsystemManagementService(hazelcastInstance)
        cpSubsystemManagementService.removeCPMember(removeMemberUuid)
            .toCompletableFuture().get()
        log.info("Removed CP member($removeMemberUuid)")
    }

    fun checkLocalMemberIsCPMember(hazelcastInstance: HazelcastInstance): Boolean {
        return cpMembers(hazelcastInstance)
            .contains(hazelcastInstance.cluster.localMember.uuid)
    }

    fun cpMembers(hazelcastInstance: HazelcastInstance): List<UUID> {
        return cpSubsystemManagementService(hazelcastInstance).cpMembers.toCompletableFuture().get().map { it.uuid }
    }

    fun cpSubsystemManagementService(hazelcastInstance: HazelcastInstance): CPSubsystemManagementService {
        val cpSubsystemManagementService = hazelcastInstance.cpSubsystem.cpSubsystemManagementService
        cpSubsystemManagementService.awaitUntilDiscoveryCompleted(3, TimeUnit.MINUTES)
        return cpSubsystemManagementService
    }

    fun terminate(hazelcastInstance: HazelcastInstance) {
        log.info("Terminating Member : ${hazelcastInstance.cluster.localMember}")
        hazelcastInstance.lifecycleService.terminate()
    }

    fun localMemberUuid(hazelcastInstance: HazelcastInstance) = hazelcastInstance.cluster.localMember.uuid
}
