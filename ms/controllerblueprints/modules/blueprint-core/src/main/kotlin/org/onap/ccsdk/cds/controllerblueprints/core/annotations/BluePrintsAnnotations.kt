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

package org.onap.ccsdk.cds.controllerblueprints.core.annotations

import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class BluePrintsDataType(
    val name: String,
    val version: String = BluePrintConstants.DEFAULT_VERSION_NUMBER,
    val description: String,
    val derivedFrom: String = "tosca.datatypes.root"
)

@Target(AnnotationTarget.CLASS)
annotation class BluePrintsWorkflowInput

@Target(AnnotationTarget.CLASS)
annotation class BluePrintsWorkflowOutput

@Target(AnnotationTarget.CLASS)
annotation class BluePrintsNodeType(
    val propertiesType: KClass<*>,
    val attributesType: KClass<*>,
    val inputsType: KClass<*>,
    val outputsType: KClass<*>
)

@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.PROPERTY)
annotation class BluePrintsProperty(
    val name: String = "",
    val description: String = ""
)

@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.PROPERTY)
@Repeatable
annotation class BluePrintsConstrain()

@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.PROPERTY)
annotation class PropertyDefaultValue(val value: String)

annotation class PropertyValidValue(val value: String)

@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.PROPERTY)
annotation class InputExpression(
    val propertyName: String
)

@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.PROPERTY)
annotation class PropertyExpression(
    val modelableEntityName: String = "SELF",
    val reqOrCapEntityName: String = "",
    val propertyName: String,
    val subPropertyName: String = ""
)

@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.PROPERTY)
annotation class AttributeExpression(
    val modelableEntityName: String = "SELF",
    val reqOrCapEntityName: String = "",
    val attributeName: String,
    val subAttributeName: String = ""
)

@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.PROPERTY)
annotation class ArtifactExpression(
    val modelableEntityName: String = "SELF",
    val artifactName: String,
    val location: String = "LOCAL_FILE",
    val remove: Boolean = false
)

@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.PROPERTY)
annotation class OperationOutputExpression(
    val modelableEntityName: String = "SELF",
    val interfaceName: String,
    val operationName: String,
    val propertyName: String,
    val subPropertyName: String = ""
)

@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.PROPERTY)
annotation class DSLExpression(
    val propertyName: String
)
