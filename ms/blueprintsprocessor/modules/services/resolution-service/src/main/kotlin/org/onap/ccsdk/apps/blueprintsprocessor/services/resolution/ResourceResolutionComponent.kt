/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.apps.blueprintsprocessor.services.resolution

import org.onap.ccsdk.apps.blueprintsprocessor.core.factory.ComponentNode
import org.springframework.stereotype.Component

@Component("component-resource-resolution")
open class ResourceResolutionComponent : ComponentNode {
    override fun validate(context: MutableMap<String, Any>, componentContext: MutableMap<String, Any?>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun process(context: MutableMap<String, Any>, componentContext: MutableMap<String, Any?>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun errorHandle(context: MutableMap<String, Any>, componentContext: MutableMap<String, Any?>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun reTrigger(context: MutableMap<String, Any>, componentContext: MutableMap<String, Any?>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}