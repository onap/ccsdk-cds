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

package org.onap.ccsdk.cds.blueprintsprocessor.atomix.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.MissingNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.atomix.core.Atomix
import io.atomix.core.lock.DistributedLock
import io.atomix.core.map.DistributedMap
import io.atomix.protocols.backup.MultiPrimaryProtocol
import io.atomix.protocols.backup.partition.PrimaryBackupPartitionGroup
import io.atomix.protocols.raft.partition.RaftPartitionGroup
import io.atomix.utils.net.Address
import org.jsoup.nodes.TextNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterInfo
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile

object AtomixLibUtils {
    private val log = logger(AtomixLibUtils::class)

    fun configAtomix(filePath: String): Atomix {
        val configFile = normalizedFile(filePath)
        return Atomix.builder(configFile.absolutePath).build()
    }

    fun defaultMulticastAtomix(
        clusterInfo: ClusterInfo,
        raftPartitions: Int = 1,
        primaryBackupPartitions: Int = 32
    ): Atomix {

        val nodeId = clusterInfo.nodeId

        val raftPartitionGroup = RaftPartitionGroup.builder("system")
            .withNumPartitions(raftPartitions)
            .withMembers(clusterInfo.clusterMembers)
            .withDataDirectory(normalizedFile("${clusterInfo.storagePath}/data-$nodeId"))
            .build()

        val primaryBackupGroup =
            PrimaryBackupPartitionGroup.builder("data")
                .withNumPartitions(primaryBackupPartitions)
                .build()

        return Atomix.builder()
            .withMemberId(nodeId)
            .withAddress(Address.from(clusterInfo.nodeAddress))
            .withManagementGroup(raftPartitionGroup)
            .withPartitionGroups(primaryBackupGroup)
            .withMulticastEnabled()
            .build()
    }

    fun <T> distributedMapStore(atomix: Atomix, storeName: String, numBackups: Int = 2): DistributedMap<String, T> {
        check(atomix.isRunning) { "Cluster is not running, couldn't create distributed store($storeName)" }

        val protocol = MultiPrimaryProtocol.builder()
            .withBackups(numBackups)
            .build()

        return atomix.mapBuilder<String, T>(storeName)
            .withProtocol(protocol)
            .withCacheEnabled()
            .withValueType(JsonNode::class.java)
            .withExtraTypes(
                JsonNode::class.java, TextNode::class.java, ObjectNode::class.java,
                ArrayNode::class.java, NullNode::class.java, MissingNode::class.java
            )
            .build()
    }

    fun distributedLock(atomix: Atomix, lockName: String, numBackups: Int = 2): DistributedLock {
        check(atomix.isRunning) { "Cluster is not running, couldn't create distributed lock($lockName)" }

        val protocol = MultiPrimaryProtocol.builder()
            .withBackups(numBackups)
            .build()

        val lock = atomix.lockBuilder(lockName)
            .withProtocol(protocol)
            .build()
        return lock
    }
}
