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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.apache.kafka.streams.processor.Processor
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.processor.Punctuator
import org.onap.ccsdk.cds.controllerblueprints.core.logger

abstract class AbstractBluePrintMessageProcessor<K, V> : Processor<K, V> {

    private val log = logger(AbstractBluePrintMessageProcessor::class)

    lateinit var processorContext: ProcessorContext
    lateinit var prioritizationConfiguration: PrioritizationConfiguration

    override fun process(key: K, value: V) = runBlocking(Dispatchers.IO) {
        processNB(key, value)
    }

    override fun init(context: ProcessorContext) {
        log.info("initializing processor applicationId(${context.applicationId()}), " +
                "taskId(${context.taskId()})")
        this.processorContext = context
    }

    override fun close() {
        log.info("closing processor applicationId(${processorContext.applicationId()}), " +
                "taskId(${processorContext.taskId()})")
    }


    abstract suspend fun processNB(key: K, value: V)
}

abstract class AbstractBluePrintMessagePunctuator : Punctuator {
    lateinit var processorContext: ProcessorContext
    lateinit var configuration: PrioritizationConfiguration

    override fun punctuate(timestamp: Long) = runBlocking(Dispatchers.IO) {
        punctuateNB(timestamp)
    }

    abstract suspend fun punctuateNB(timestamp: Long)

}