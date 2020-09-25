.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

Workflow
========
A workflow defines an overall action to be taken on the service, hence is an entry-point for the run-time execution of the CBA package.

A workflow also defines inputs and outputs that will defined the payload contract of the request and response (see Dynamic API)

A workflow can be composed of one or multiple sub-actions to execute.

A CBA package can have as many workflows as needed.


Single action
-------------
The workflow is directly backed by a node_template of type tosca.nodes.Component


Multiple sub-actions
--------------------
The workflow is backed by Directed Graph engine, node_template of type dg-generic, and are imperative workflows.

A DG used as workflow for CDS is composed of multiple execute nodes; each individual execute node refers to a plugin, that is a node_template of type tosca.nodes.Component.

Below the properties of a workflow:


Workflow Example
----------------
.. code-block:: json

   {
     "workflow": {
       "resource-assignment": {                                <- workflow-name
         "inputs": {
           "vnf-id": {                                         <- static inputs
             "required": true,
             "type": "string"
           },
           "resource-assignment-properties": {                 <- dynamic inputs
             "required": true,
             "type": "dt-resource-assignment-properties"
           }
         },
         "steps": {
           "call-resource-assignment": {                       <- step-name
             "description": "Resource Assignment Workflow",
             "target": "resource-assignment-process"           <- node_template targeted by the step
           }
         },
         "outputs": {
           "template-properties": {                            <- output
             "type": "json",                                   <- complex type
             "value": {
               "get_attribute": [                              <- uses expression to retrieve attribute from context
                 "resource-assignment",
                 "assignment-params"
               ]
             }
           }
         }
       }
     }
   }


TOSCA definition: http://docs.oasis-open.org/tosca/TOSCA-Simple-Profile-YAML/v1.2/csd01/TOSCA-Simple-Profile-YAML-v1.2-csd01.html#_Toc494454203
