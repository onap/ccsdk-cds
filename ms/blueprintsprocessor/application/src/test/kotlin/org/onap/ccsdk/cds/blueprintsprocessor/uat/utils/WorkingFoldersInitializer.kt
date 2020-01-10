/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.cds.blueprintsprocessor.uat.utils

import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component
import org.springframework.test.context.support.TestPropertySourceUtils

@Component
class WorkingFoldersInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    override fun initialize(context: ConfigurableApplicationContext) {
        val tempFolder = ExtendedTemporaryFolder()
        val properties = listOf("Deploy", "Archive", "Working")
            .map { "blueprintsprocessor.blueprint${it}Path=${tempFolder.newFolder(it).absolutePath.replace("\\", "/")}" }
            .toTypedArray()
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context, *properties)
        // Expose tempFolder as a bean so it can be accessed via DI
        registerSingleton(context, "tempFolder", ExtendedTemporaryFolder::class.java, tempFolder)
    }

    @Suppress("SameParameterValue")
    private fun <T> registerSingleton(
        context: ConfigurableApplicationContext,
        beanName: String,
        beanClass: Class<T>,
        instance: T
    ) {
        val builder = BeanDefinitionBuilder.genericBeanDefinition(beanClass) { instance }
        (context.beanFactory as BeanDefinitionRegistry).registerBeanDefinition(beanName, builder.beanDefinition)
    }
}
