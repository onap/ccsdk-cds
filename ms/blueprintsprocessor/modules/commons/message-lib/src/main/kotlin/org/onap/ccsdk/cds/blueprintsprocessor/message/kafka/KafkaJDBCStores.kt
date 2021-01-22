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

package org.onap.ccsdk.cds.blueprintsprocessor.message.kafka

/*
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.processor.StateStore
import org.apache.kafka.streams.state.StoreBuilder
import org.apache.kafka.streams.state.StoreSupplier
import org.onap.ccsdk.cds.blueprintsprocessor.db.BlueprintDBLibGenericService
import org.onap.ccsdk.cds.blueprintsprocessor.db.primaryDBLibGenericService
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintDependencyService
import java.util.*


class KafkaJDBCKeyStoreSupplier(private val name: String) : StoreSupplier<KafkaJDBCStore> {

    override fun get(): KafkaJDBCStore {
        // Get the DBLibGenericService Instance
        val bluePrintDBLibGenericService = BlueprintDependencyService.primaryDBLibGenericService()
        return KafkaJDBCStoreImpl(name, bluePrintDBLibGenericService)
    }

    override fun name(): String {
        return name
    }

    override fun metricsScope(): String {
        return "jdbc-state"
    }
}

class KafkaJDBCKeyStoreBuilder(private val storeSupplier: KafkaJDBCKeyStoreSupplier)
    : StoreBuilder<KafkaJDBCStore> {

    private var logConfig: MutableMap<String, String> = HashMap()
    private var enableCaching: Boolean = false
    private var enableLogging = true

    override fun logConfig(): MutableMap<String, String> {
        return logConfig
    }

    override fun withCachingDisabled(): StoreBuilder<KafkaJDBCStore> {
        enableCaching = false
        return this
    }

    override fun loggingEnabled(): Boolean {
        return enableLogging
    }

    override fun withLoggingDisabled(): StoreBuilder<KafkaJDBCStore> {
        enableLogging = false
        return this
    }

    override fun withCachingEnabled(): StoreBuilder<KafkaJDBCStore> {
        enableCaching = true
        return this
    }

    override fun withLoggingEnabled(config: MutableMap<String, String>?): StoreBuilder<KafkaJDBCStore> {
        enableLogging = true
        return this
    }

    override fun name(): String {
        return "KafkaJDBCKeyStoreBuilder"
    }

    override fun build(): KafkaJDBCStore {
        return storeSupplier.get()
    }
}

interface KafkaJDBCStore : StateStore {

    suspend fun query(sql: String, params: Map<String, Any>): List<Map<String, Any>>

    suspend fun update(sql: String, params: Map<String, Any>): Int
}


class KafkaJDBCStoreImpl(private val name: String,
                         private val bluePrintDBLibGenericService: BlueprintDBLibGenericService)
    : KafkaJDBCStore {

    private val log = logger(KafkaJDBCStoreImpl::class)

    override fun isOpen(): Boolean {
        log.info("isOpen...")
        return true
    }

    override fun init(context: ProcessorContext, root: StateStore) {
        log.info("init...")
    }

    override fun flush() {
        log.info("flush...")
    }

    override fun close() {
        log.info("Close...")
    }

    override fun name(): String {
        return name
    }

    override fun persistent(): Boolean {
        return true
    }

    override suspend fun query(sql: String, params: Map<String, Any>): List<Map<String, Any>> {
        log.info("Query : $sql")
        log.info("Params : $params")
        return bluePrintDBLibGenericService.query(sql, params)
    }

    override suspend fun update(sql: String, params: Map<String, Any>): Int {
        log.info("Query : $sql")
        log.info("Params : $params")
        return bluePrintDBLibGenericService.update(sql, params)
    }
}
*/
