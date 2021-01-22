/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018-2019 IBM.
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
@file:Suppress("unused")

package org.onap.ccsdk.cds.controllerblueprints.core.data

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.JsonNode
import io.swagger.annotations.ApiModelProperty
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType

/**
 *
 *
 * @author Brinda Santh
 */
open class EntityType {

    @get:JsonIgnore
    var id: String? = null
    var description: String? = null
    var version: String = "1.0.0"
    var metadata: MutableMap<String, String>? = null

    @get:JsonProperty("derived_from")
    lateinit var derivedFrom: String
    var attributes: MutableMap<String, AttributeDefinition>? = null
    var properties: MutableMap<String, PropertyDefinition>? = null
}

/*
 5.3.2 tosca.datatypes.Credential
 The Credential type is a complex TOSCA data Type used when describing
 authorization credentials used to access network accessible resources.
 */
class Credential {

    @get:JsonIgnore
    var id: String? = null
    var protocol: String? = null

    @get:JsonProperty("token_type")
    lateinit var tokenType: String
    lateinit var token: String
    var keys: MutableMap<String, String>? = null
    lateinit var user: String
}

/*
3.5.2 Constraint clause
A constraint clause defines an operation along with one or more compatible values that can be used to define a constraint on a property or parameter’s allowed values when it is defined in a TOSCA Service Template or one of its entities.
 */
class ConstraintClause {

    var equal: JsonNode? = null

    @get:JsonProperty("greater_than")
    var greaterThan: JsonNode? = null

    @get:JsonProperty("greater_or_equal")
    var greaterOrEqual: JsonNode? = null

    @get:JsonProperty("less_than")
    var lessThan: JsonNode? = null

    @get:JsonProperty("less_or_equal")
    var lessOrEqual: JsonNode? = null

    @get:JsonProperty("in_range")
    var inRange: MutableList<JsonNode>? = null

    @get:JsonProperty("valid_values")
    var validValues: MutableList<JsonNode>? = null
    var length: JsonNode? = null

    @get:JsonProperty("min_length")
    var minLength: JsonNode? = null

    @get:JsonProperty("max_length")
    var maxLength: JsonNode? = null
    var pattern: String? = null
    var schema: String? = null
}

/*
3.5.4 Node Filter definition
A node filter definition defines criteria for selection of a TOSCA Node Template based upon the template’s property values, capabilities and capability properties.
 */

class NodeFilterDefinition {

    var properties: MutableMap<String, PropertyDefinition>? = null
    var capabilities: MutableList<String>? = null
}

/*
3.5.5 Repository definition
 A repository definition defines a named external repository which contains deployment
 and implementation artifacts that are referenced within the TOSCA Service Template.
*/
class RepositoryDefinition {

    @get:JsonIgnore
    var id: String? = null
    var description: String? = null
    lateinit var url: String
    var credential: Credential? = null
}

/*
3.5.6 Artifact definition
An artifact definition defines a named, typed file that can be associated with Node Type
or Node Template and used by orchestration engine to facilitate deployment and implementation of interface operations.
 */
class ArtifactDefinition {

    @get:JsonIgnore
    var id: String? = null
    lateinit var type: String
    lateinit var file: String
    var repository: String? = null
    var description: String? = null

    @get:JsonProperty("deploy_Path")
    var deployPath: String? = null
    var properties: MutableMap<String, JsonNode>? = null
}

/*
3.5.7 Import definition
An import definition is used within a TOSCA Service Template to locate and uniquely name
another TOSCA Service Template file which has type and template definitions to be imported (included)
and referenced within another Service Template.
 */
class ImportDefinition {

    @get:JsonIgnore
    var id: String? = null
    lateinit var file: String
    var repository: String? = null

    @get:JsonProperty("namespace_uri")
    var namespaceUri: String? = null

    @get:JsonProperty("namespace_prefix")
    var namespacePrefix: String? = null
}

/*
3.5.8 Property definition A property definition defines a named, typed value and related data that can be associated with an
entity defined in this specification (e.g., Node Types, Relationship Types, Capability Types, etc.).
Properties are used by template authors to provide input values to TOSCA entities which indicate their “desired state” when they are
instantiated. The value of a property can be retrieved using the get_property function within TOSCA Service Templates.
 */
class PropertyDefinition {

    @get:JsonIgnore
    var id: String? = null
    var description: String? = null
    var required: Boolean? = null
    lateinit var type: String

    @get:JsonProperty("input-param")
    var inputparam: Boolean? = null

    @get:JsonProperty("default")
    var defaultValue: JsonNode? = null
    var status: String? = null
    var constraints: MutableList<ConstraintClause>? = null

    @get:JsonProperty("entry_schema")
    var entrySchema: EntrySchema? = null

    @get:JsonProperty("external-schema")
    var externalSchema: String? = null
    var metadata: MutableMap<String, String>? = null

    // Mainly used in Workflow Outputs
    @get:ApiModelProperty(notes = "Property Value, It may be Expression or Json type values")
    var value: JsonNode? = null
}

/*
3.5.10 Attribute definition

An attribute definition defines a named, typed value that can be associated with an entity defined in this
specification (e.g., a Node, Relationship or Capability Type).  Specifically, it is used to expose the
“actual state” of some property of a TOSCA entity after it has been deployed and instantiated
(as set by the TOSCA orchestrator). Attribute values can be retrieved via the get_attribute function
from the instance model and used as values to other entities within TOSCA Service Templates.
 */

class AttributeDefinition {

    @get:JsonIgnore
    var id: String? = null
    var description: String? = null
    var required: Boolean? = null
    lateinit var type: String

    @JsonProperty("default")
    var defaultValue: JsonNode? = null
    var status: String? = null
    var constraints: MutableList<ConstraintClause>? = null

    @JsonProperty("entry_schema")
    var entrySchema: EntrySchema? = null

    // Mainly used in DSL definitions
    @get:ApiModelProperty(notes = "Attribute Value, It may be Expression or Json type values")
    var value: JsonNode? = null
}

/*
3.5.13 Operation definition
An operation definition defines a named function or procedure that can be bound to an implementation artifact (e.g., a script).
 */
class OperationDefinition {

    @get:JsonIgnore
    var id: String? = null
    var description: String? = null
    var implementation: Implementation? = null
    var inputs: MutableMap<String, PropertyDefinition>? = null
    var outputs: MutableMap<String, PropertyDefinition>? = null
}

class Implementation {

    var primary: String? = null
    var dependencies: MutableList<String>? = null

    @get:JsonProperty("operation_host")
    var operationHost: String = BlueprintConstants.PROPERTY_SELF

    // Timeout value in seconds
    var timeout: Int = 180
    var lock: LockAssignment? = null
}

class LockAssignment {

    lateinit var key: JsonNode
    var acquireTimeout: JsonNode = Integer(180).asJsonType()
}

/*
3.5.14 Interface definition
An interface definition defines a named interface that can be associated with a Node or Relationship Type
 */
class InterfaceDefinition {

    @get:JsonIgnore
    var id: String? = null
    var type: String? = null
    var operations: MutableMap<String, OperationDefinition>? = null
    var inputs: MutableMap<String, PropertyDefinition>? = null
}

/*
3.5.15 Event Filter definition
An event filter definition defines criteria for selection of an attribute, for the purpose of monitoring it, within a TOSCA entity, or one its capabilities.
 */
class EventFilterDefinition {

    @get:JsonIgnore
    var id: String? = null
    lateinit var node: String
    var requirement: String? = null
    var capability: String? = null
}

/*
3.5.16 Trigger definition TODO
A trigger definition defines the event, condition and action that is used to “trigger” a policy it is associated with.
 */
class TriggerDefinition {

    @get:JsonIgnore
    var id: String? = null
    var description: String? = null

    @get:JsonProperty("event_type")
    lateinit var eventType: String

    @get:JsonProperty("target_filter")
    var targetFilter: EventFilterDefinition? = null
    var condition: ConditionClause? = null
    var constraint: ConditionClause? = null
    var method: String? = null
    lateinit var action: String
}

/*
    3.5.17 Workflow activity definition
    A workflow activity defines an operation to be performed in a TOSCA workflow. Activities allows to:
    · Delegate the workflow for a node expected to be provided 	by the orchestrator
    · Set the state of a node
    · Call an operation	defined on a TOSCA interface of a node, relationship or group
    · Inline another workflow defined in the topology (to allow reusability)
 */
class Activity {

    var delegate: String? = null

    @get:JsonProperty("set_state")
    var setState: String? = null

    @get:JsonProperty("call_operation")
    var callOperation: String? = null
    var inlines: ArrayList<String>? = null
}

/*
3.5.20 Workflow precondition definition
A workflow condition can be used as a filter or precondition to check if a workflow can be processed or not based on the state of the instances of a TOSCA topology deployment. When not met, the workflow will not be triggered.
 */
class PreConditionDefinition {

    @get:JsonIgnore
    var id: String? = null
    lateinit var target: String

    @get:JsonProperty("target_relationship")
    lateinit var targetRelationship: String
    lateinit var condition: ArrayList<ConditionClause>
}

/*
3.5.21 Workflow step definition
A workflow step allows to define one or multiple sequenced activities in a workflow and how they are connected to other steps in the workflow. They are the building blocks of a declarative workflow.
 */
class Step {

    @get:JsonIgnore
    var id: String? = null
    var description: String? = null
    var target: String? = null

    @JsonProperty("target_relationship")
    var targetRelationship: String? = null

    @JsonProperty("operation_host")
    var operationHost: String? = null
    var activities: ArrayList<Activity>? = null

    @get:JsonProperty("on_success")
    var onSuccess: ArrayList<String>? = null

    @get:JsonProperty("on_failure")
    var onFailure: ArrayList<String>? = null
}

/*
3.6.2 Capability definition
A capability definition defines a named, typed set of data that can be associated with Node Type or Node Template to describe a transparent capability or feature of the software component the node describes.
 */

class CapabilityDefinition {

    @get:JsonIgnore
    var id: String? = null
    lateinit var type: String
    var description: String? = null
    var properties: MutableMap<String, PropertyDefinition>? = null

    @get:JsonProperty("valid_source_types")
    var validSourceTypes: MutableList<String>? = null
    var occurrences: MutableList<Any>? = null
}

/*
3.6.3 Requirement definition
The Requirement definition describes a named requirement (dependencies) of a TOSCA Node Type or Node template which needs to be fulfilled by a matching Capability definition declared by another TOSCA modelable entity.  The requirement definition may itself include the specific name of the fulfilling entity (explicitly) or provide an abstract type, along with additional filtering characteristics, that a TOSCA orchestrator can use to fulfill the capability at runtime (implicitly).
 */
class RequirementDefinition {

    @get:JsonIgnore
    var id: String? = null
    var description: String? = null
    var capability: String? = null
    var node: String? = null
    var relationship: String? = null
    var occurrences: MutableList<Any>? = null
}

/*
3.6.4 Artifact Type
An Artifact Type is a reusable entity that defines the type of one or more files that are used to define implementation or deployment artifacts that are referenced by nodes or relationships on their operations.
 */
class ArtifactType : EntityType() {

    @get:JsonProperty("mime_type")
    var mimeType: String? = null

    @get:JsonProperty("file_ext")
    var fileExt: MutableList<String>? = null
}

/*
3.6.6 Data Type
A Data Type definition defines the schema for new named datatypes in TOSCA.
 */

class DataType : EntityType() {

    var constraints: MutableList<ConstraintClause>? = null
}

/*
3.6.9 Node Type
A Node Type is a reusable entity that defines the type of one or more Node Templates. As such, a Node Type defines the structure of observable properties via a Properties Definition, the Requirements and Capabilities of the node as well as its supported interfaces.

 */

class NodeType : EntityType() {

    var capabilities: MutableMap<String, CapabilityDefinition>? = null
    var requirements: MutableMap<String, RequirementDefinition>? = null
    var interfaces: MutableMap<String, InterfaceDefinition>? = null
    var artifacts: MutableMap<String, ArtifactDefinition>? = null
}

/*
3.6.8 Requirement Type
A Requirement Type is a reusable entity that describes a kind of requirement that a Node Type can declare to expose.
The TOSCA Simple Profile seeks to simplify the need for declaring specific Requirement Types
from nodes and instead rely upon nodes declaring their features sets using TOSCA Capability Types
along with a named Feature notation.
 */

class RequirementType : EntityType() {

    var requirements: MutableMap<String, RequirementDefinition>? = null
    var capabilities: MutableMap<String, CapabilityDefinition>? = null
    var interfaces: MutableMap<String, InterfaceDefinition>? = null
    var artifacts: MutableMap<String, ArtifactDefinition>? = null
}

/*
3.6.10 Relationship Type
A Relationship Type is a reusable entity that defines the type of one or more relationships between Node Types or Node Templates.
*/

class RelationshipType : EntityType() {

    var interfaces: MutableMap<String, InterfaceDefinition>? = null

    @get:JsonProperty("valid_target_types")
    var validTargetTypes: MutableList<String>? = null
}

/*
3.6.11 Group Type
A Group Type defines logical grouping types for nodes, typically for different management purposes.
Groups can effectively be viewed as logical nodes that are not part of the physical deployment topology
 of an application, yet can have capabilities and the ability to attach policies and interfaces
 that can be applied (depending on the group type) to its member nodes.
 */

class GroupType : EntityType() {

    var members: MutableList<String>? = null
    var requirements: ArrayList<RequirementDefinition>? = null
    var capabilities: MutableMap<String, CapabilityDefinition>? = null
    var interfaces: MutableMap<String, InterfaceDefinition>? = null
}

/*
    3.6.12 Policy Type
    A Policy Type defines a type of requirement that affects or governs an application or service’s
    topology at some stage of its lifecycle, but is not explicitly part of the topology itself
    (i.e., it does not prevent the application or service from being deployed or run if it did not exist).
 */
class PolicyType : EntityType() {

    lateinit var targets: MutableList<String>
}

/*
3.7.1 Capability assignment
A capability assignment allows node template authors to assign values to properties and attributes for a named capability definition that is part of a Node Template’s type definition.
 */
class CapabilityAssignment {

    @get:JsonIgnore
    var id: String? = null
    var attributes: MutableMap<String, JsonNode>? = null
    var properties: MutableMap<String, JsonNode>? = null
}

/*
3.7.4 Relationship Template
A Relationship Template specifies the occurrence of a manageable relationship between node templates as part of an application’s topology model that is defined in a TOSCA Service Template.  A Relationship template is an instance of a specified Relationship Type and can provide customized properties, constraints or operations which override the defaults provided by its Relationship Type and its implementations.
 */
class GroupDefinition {

    @get:JsonIgnore
    var id: String? = null
    lateinit var type: String
    var description: String? = null
    var metadata: MutableMap<String, String>? = null
    var properties: MutableMap<String, JsonNode>? = null
    var members = ArrayList<String>()
    var interfaces: MutableMap<String, InterfaceDefinition>? = null
}

/*
3.7.6 Policy definition
A policy definition defines a policy that can be associated with a TOSCA topology or top-level entity definition (e.g., group definition, node template, etc.).
 */
class PolicyDefinition {

    @get:JsonIgnore
    var id: String? = null
    lateinit var type: String
    var description: String? = null
    var metadata: MutableMap<String, String>? = null
    var properties: MutableMap<String, JsonNode>? = null
    var targets: MutableList<String>? = null
}

/*
3.8 Topology Template definition
This section defines the topology template of a cloud application. The main ingredients of the topology template are node templates representing components of the application and relationship templates representing links between the components. These elements are defined in the nested node_templates section and the nested relationship_templates sections, respectively.  Furthermore, a topology template allows for defining input parameters, output parameters as well as grouping of node templates.
 */
class TopologyTemplate {

    @get:JsonIgnore
    var id: String? = null
    var description: String? = null
    var inputs: MutableMap<String, PropertyDefinition>? = null

    @get:JsonProperty("node_templates")
    var nodeTemplates: MutableMap<String, NodeTemplate>? = null

    @get:JsonProperty("relationship_templates")
    var relationshipTemplates: MutableMap<String, RelationshipTemplate>? = null
    var policies: MutableMap<String, PolicyDefinition>? = null
    var outputs: MutableMap<String, PropertyDefinition>? = null

    @get:JsonProperty("substitution_mappings")
    var substitutionMappings: Any? = null
    var workflows: MutableMap<String, Workflow>? = null
}

class SubstitutionMapping {

    @get:JsonProperty("node_type")
    lateinit var nodeType: String
    lateinit var capabilities: ArrayList<String>
    lateinit var requirements: ArrayList<String>
}

class EntrySchema {

    lateinit var type: String
    var constraints: MutableList<ConstraintClause>? = null
}

class InterfaceAssignment {

    @get:JsonIgnore
    var id: String? = null
    var operations: MutableMap<String, OperationAssignment>? = null
    var inputs: MutableMap<String, JsonNode>? = null
}

/*
3.7.3 Node Template
A Node Template specifies the occurrence of a manageable software component as part of an application’s topology model which is defined in a TOSCA Service Template.  A Node template is an instance of a specified Node Type and can provide customized properties, constraints or operations which override the defaults provided by its Node Type and its implementations.
 */

open class NodeTemplate {

    @get:JsonIgnore
    var id: String? = null
    var description: String? = null
    lateinit var type: String
    var metadata: MutableMap<String, String>? = null
    var directives: MutableList<String>? = null

    // @get:JsonSerialize(using = PropertyDefinitionValueSerializer::class)
    var properties: MutableMap<String, JsonNode>? = null
    var attributes: MutableMap<String, JsonNode>? = null
    var capabilities: MutableMap<String, CapabilityAssignment>? = null
    var requirements: MutableMap<String, RequirementAssignment>? = null
    var interfaces: MutableMap<String, InterfaceAssignment>? = null
    var artifacts: MutableMap<String, ArtifactDefinition>? = null

    @get:JsonProperty("node_filter")
    var nodeFilter: NodeFilterDefinition? = null
    var copy: String? = null
}

class OperationAssignment {

    @get:JsonIgnore
    var id: String? = null
    var description: String? = null
    var implementation: Implementation? = null
    var inputs: MutableMap<String, JsonNode>? = null
    var outputs: MutableMap<String, JsonNode>? = null
}

/*
3.7.4 Relationship Template
A Relationship Template specifies the occurrence of a manageable relationship between node templates as part of an application’s topology model that is defined in a TOSCA Service Template.  A Relationship template is an instance of a specified Relationship Type and can provide customized properties, constraints or operations which override the defaults provided by its Relationship Type and its implementations.
 */

class RelationshipTemplate {

    @get:JsonIgnore
    var id: String? = null
    lateinit var type: String
    var description: String? = null
    var metadata: MutableMap<String, String>? = null
    var properties: MutableMap<String, JsonNode>? = null
    var attributes: MutableMap<String, JsonNode>? = null
    var interfaces: MutableMap<String, InterfaceDefinition>? = null
    var copy: String? = null
}

/*
3.7.2 Requirement assignment
A Requirement assignment allows template authors to provide either concrete names of TOSCA templates or provide abstract selection criteria for providers to use to find matching TOSCA templates that are used to fulfill a named requirement’s declared TOSCA Node Type.
 */

class RequirementAssignment {

    @get:JsonIgnore
    var id: String? = null
    var capability: String? = null
    var node: String? = null

    // Relationship Type or Relationship Template
    var relationship: String? = null
}

class Workflow {

    @get:JsonIgnore
    var id: String? = null
    var description: String? = null
    var steps: MutableMap<String, Step>? = null
    var preconditions: ArrayList<PreConditionDefinition>? = null
    var inputs: MutableMap<String, PropertyDefinition>? = null
    var outputs: MutableMap<String, PropertyDefinition>? = null
}

class ConditionClause {

    var and: ArrayList<MutableMap<String, Any>>? = null
    var or: ArrayList<MutableMap<String, Any>>? = null

    @get:JsonProperty("assert")
    var assertConditions: ArrayList<MutableMap<String, Any>>? = null
}

/*
3.9 Service Template definition
A TOSCA Service Template (YAML) document contains element definitions of building blocks for cloud application, or complete models of cloud applications. This section describes the top-level structural elements (TOSCA keynames) along with their grammars, which are allowed to appear in a TOSCA Service Template document.
 */

@JsonPropertyOrder(
    value = [
        "toscaDefinitionsVersion", "description", "metadata", "imports", "dsl_definitions",
        "topologyTemplate"
    ]
)
class ServiceTemplate : Cloneable {

    @get:JsonIgnore
    var id: String? = null

    @get:JsonProperty("tosca_definitions_version")
    var toscaDefinitionsVersion: String = "controller_blueprint_1_0_0"
    var metadata: MutableMap<String, String>? = null
    var description: String? = null

    @get:JsonProperty("dsl_definitions")
    var dslDefinitions: MutableMap<String, JsonNode>? = null
    var repositories: MutableMap<String, RepositoryDefinition>? = null
    var imports: MutableList<ImportDefinition>? = null

    @get:JsonProperty("artifact_types")
    var artifactTypes: MutableMap<String, ArtifactType>? = null

    @get:JsonProperty("data_types")
    var dataTypes: MutableMap<String, DataType>? = null

    @get:JsonProperty("relationship_types")
    var relationshipTypes: MutableMap<String, RelationshipType>? = null

    @get:JsonProperty("node_types")
    var nodeTypes: MutableMap<String, NodeType>? = null

    @get:JsonProperty("policy_types")
    var policyTypes: MutableMap<String, PolicyType>? = null

    @get:JsonProperty("topology_template")
    var topologyTemplate: TopologyTemplate? = null

    public override fun clone(): ServiceTemplate {
        return super.clone() as ServiceTemplate
    }
}

class ToscaMetaData {

    lateinit var toscaMetaFileVersion: String
    lateinit var csarVersion: String
    lateinit var createdBy: String
    lateinit var entityDefinitions: String
    lateinit var templateName: String
    lateinit var templateVersion: String
    lateinit var templateTags: String
    var templateType: String = BlueprintConstants.BLUEPRINT_TYPE_DEFAULT
}
