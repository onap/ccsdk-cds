/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 *
 * Modifications Copyright © 2019 IBM, Bell Canada.
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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor

import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.apps.blueprintsprocessor.functions.python.executor.BlueprintPythonService
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.interfaces.DeviceInfo
import org.onap.ccsdk.apps.blueprintsprocessor.functions.python.executor.ComponentJythonExecutor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component("component-netconf-executor")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ComponentNetconfExecutor(private var applicationContext: ApplicationContext, private val netconfExecutorConfiguration: NetconfExecutorConfiguration,
                                    private val blueprintPythonService: BlueprintPythonService)
    : ComponentJythonExecutor(applicationContext, blueprintPythonService) {

    private val log = LoggerFactory.getLogger(ComponentNetconfExecutor::class.java)
    lateinit var deviceInfo: DeviceInfo


    override fun process(executionServiceInput: ExecutionServiceInput) {

         super.process(executionServiceInput)


    }

    fun setdeviceInfo(deviceInfo: DeviceInfo) {
        this.deviceInfo = deviceInfo
    }
}