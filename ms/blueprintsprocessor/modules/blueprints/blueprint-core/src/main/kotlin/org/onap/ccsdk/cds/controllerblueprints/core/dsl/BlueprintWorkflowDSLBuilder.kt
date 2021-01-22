/*
 *  Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.controllerblueprints.core.dsl

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.asPropertyDefinitionMap
import org.onap.ccsdk.cds.controllerblueprints.core.data.Activity
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.Step
import org.onap.ccsdk.cds.controllerblueprints.core.data.Workflow
import kotlin.reflect.KClass

class WorkflowBuilder(private val id: String, private val description: String) {

    private var workflow = Workflow()
    private var steps: MutableMap<String, Step>? = null
    private var inputs: MutableMap<String, PropertyDefinition>? = null
    private var outputs: MutableMap<String, PropertyDefinition>? = null

    // Used Internally
    fun nodeTemplateStep(nodeTemplateName: String, description: String) {
        step(nodeTemplateName, nodeTemplateName, "$description step")
    }

    fun step(id: String, target: String, description: String) {
        if (steps == null)
            steps = hashMapOf()
        steps!![id] = StepBuilder(id, target, description).build()
    }

    fun step(id: String, target: String, description: String, block: StepBuilder.() -> Unit) {
        if (steps == null)
            steps = hashMapOf()
        steps!![id] = StepBuilder(id, target, description).apply(block).build()
    }

    fun inputs(kClazz: KClass<*>) {
        inputs = kClazz.asPropertyDefinitionMap()
    }

    fun inputs(block: PropertiesDefinitionBuilder.() -> Unit) {
        inputs = PropertiesDefinitionBuilder().apply(block).build()
    }

    fun outputs(block: PropertiesDefinitionBuilder.() -> Unit) {
        outputs = PropertiesDefinitionBuilder().apply(block).build()
    }

    fun outputs(kClazz: KClass<*>) {
        outputs = kClazz.asPropertyDefinitionMap()
    }

    fun build(): Workflow {
        workflow.id = id
        workflow.description = description
        workflow.steps = steps
        workflow.inputs = inputs
        workflow.outputs = outputs
        return workflow
    }
}

class StepBuilder(
    private val id: String,
    private val target: String,
    private val description: String
) {

    private var step = Step()
    private var activities: ArrayList<Activity> = arrayListOf()
    private var onSuccess: ArrayList<String>? = null
    private var onFailure: ArrayList<String>? = null

    fun activity(callOperation: String) {
        val activity = Activity()
        activity.callOperation = callOperation
        activities.add(activity)
    }

    fun success(vararg successTargets: String) {
        if (onSuccess == null)
            onSuccess = arrayListOf()
        successTargets.forEach {
            onSuccess!!.add(it)
        }
    }

    fun failure(vararg failureTargets: String) {
        if (onFailure == null)
            onFailure = arrayListOf()
        failureTargets.forEach {
            onFailure!!.add(it)
        }
    }

    fun build(): Step {
        step.id = id
        step.target = target
        // Add Default Activity, Assumption is only one Operation
        activity(".${BlueprintConstants.DEFAULT_STEP_OPERATION}")
        step.description = description
        step.activities = activities
        step.onSuccess = onSuccess
        step.onFailure = onFailure
        return step
    }
}
