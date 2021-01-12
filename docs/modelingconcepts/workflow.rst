.. This work is a derivative of https://wiki.onap.org/display/DW/Modeling+Concepts#Concepts-100023263
.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2020 Deutsche Telekom AG.

.. _workflow:

Workflow
--------

.. note::

   **Workflow Scope within CDS Framework**

   The workflow is within the scope of the micro provisioning and configuration
   management in **controller domain** and does NOT account for the MACRO service orchestration workflow which is covered by the SO Project.

A workflow defines an overall action to be taken on the service, hence is an
entry-point for the run-time execution of the :ref:`CBA Package <cba>`.

A workflow also defines **inputs** and **outputs** that will defined the **payload contract**
of the **request** and **response** (see :ref:`dynamic_payload`)

A workflow can be **composed** of one or multiple **sub-actions** to execute.

A CBA package can have as **many workflows** as needed.

.. _workflow_single_action:

Single action
**************

The workflow is directly backed by a component (see :ref:`node_type` -> Component).

In the example bellow, the target of the workflow's steps resource-assignment is ``resource-assignment``
which actually is the name of the ``node_template`` defined after, of type ``component-resource-resolution``.

`Link to example
<https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/blueprint-model/test-blueprint/golden/Definitions/golden-blueprint.json#L40-L71>`_


.. code-block:: json
   :caption: **Example**

      . . .
      "topology_template": {
        "workflows": {
          "resource-assignment": {
            "steps": {
              "resource-assignment": {
                "description": "Resource Assign Workflow",
                "target": "resource-assignment"
              }
            }
          },
          "inputs": {
            "resource-assignment-properties": {
              "description": "Dynamic PropertyDefinition for workflow(resource-assignment).",
              "required": true,
              "type": "dt-resource-assignment-properties"
            }
          },
          "outputs": {
            "meshed-template": {
              "type": "json",
              "value": {
                "get_attribute": [
                  "resource-assignment",
                  "assignment-params"
                ]
              }
            }
          }
        },
        "node_templates": {
          "resource-assignment": {
            "type": "component-resource-resolution",
            "interfaces": {
              "ResourceResolutionComponent": {
                "operations": {
                  "process": {
                    "inputs": {
                      "artifact-prefix-names": [
                        "vf-module-1"
                      ]
                    }
                  }
                }
              }
            },
            "artifacts": {
              "vf-module-1-template": {
                "type": "artifact-template-velocity",
                "file": "Templates/vf-module-1-template.vtl"
              },
              "vf-module-1-mapping": {
                "type": "artifact-mapping-resource",
                "file": "Templates/vf-module-1-mapping.json"
              }
            }
          }
        }
      }
      . . .

.. _workflow_multiple_actions:

Multiple sub-actions
**********************

The workflow is backed by a Directed Graph engine, dg-generic (see :ref:`node_type` -> DG,
and is an **imperative** workflow.

A DG used as workflow for CDS is composed of multiple execute nodes; each individual
execute node refers to an modelled Component (see :ref:`node_type` -> Component) instance.

In the example above, you can see the target of the workflow's steps execute-script is
``execute-remote-ansible-process``, which is a node_template of type ``dg_generic``

`Link of example
<https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/blueprint-model/test-blueprint/remote_scripts/Definitions/remote_scripts.json#L184-L204>`_

.. code-block:: json
   :caption: **workflow plan example**

    . . .
    "topology_template": {
      "workflows": {
        "execute-remote-ansible": {
          "steps": {
            "execute-script": {
              "description": "Execute Remote Ansible Script",
              "target": "execute-remote-ansible-process"
            }
          }
        },
        "inputs": {
          "ip": {
            "required": false,
            "type": "string"
          },
          "username": {
            "required": false,
            "type": "string"
          },
          "password": {
            "required": false,
            "type": "string"
          },
          "execute-remote-ansible-properties": {
            "description": "Dynamic PropertyDefinition for workflow(execute-remote-ansible).",
            "required": true,
            "type": "dt-execute-remote-ansible-properties"
          }
        },
        "outputs": {
          "ansible-variable-resolution": {
            "type": "json",
            "value": {
              "get_attribute": [
                "resolve-ansible-vars",
                "assignment-params"
              ]
            }
          },
          "prepare-environment-logs": {
            "type": "string",
            "value": {
              "get_attribute": [
                "execute-remote-ansible",
                "prepare-environment-logs"
              ]
            }
          },
          "execute-command-logs": {
            "type": "string",
            "value": {
              "get_attribute": [
                "execute-remote-ansible",
                "execute-command-logs"
              ]
            }
          }
        },
        "node_templates": {
          "execute-remote-ansible-process": {
            "type": "dg-generic",
            "properties": {
              "content": {
                "get_artifact": [
                  "SELF",
                  "dg-execute-remote-ansible-process"
                ]
              },
              "dependency-node-templates": [
                "resolve-ansible-vars",
                "execute-remote-ansible"
              ]
            },
            "artifacts": {
              "dg-execute-remote-ansible-process": {
                "type": "artifact-directed-graph",
                "file": "Plans/CONFIG_ExecAnsiblePlaybook.xml"
              }
            }
          }
        }
      }
    }

Properties of a workflow
**************************

.. list-table::
   :widths: 25 75
   :header-rows: 1

   * - Property
     - Description
   * - workflow-name
     - Defines the name of the action that can be triggered by external system
   * - inputs
     - | They are two types of inputs, the dynamic ones, and the static one.
       |

       .. tabs::

          .. tab:: static

             Specified at workflow level

             * can be inputs for the Component(s), see the inputs section of the component of interest.
             * represent inputs to derived custom logic within scripting

             These will end up under ``${actionName}-request`` section of the payload (see Dynamic API)

          .. tab:: dynamic

             Represent the resources defined as input (see :ref:`node_type` -> Source -> Input)
             within mapping definition files (see :ref:`artifact_type` -> Mapping).

             The **enrichment process** will (see :ref:`enrichment`)

             * dynamically gather all of them under a data-type, named ``dt-${actionName}-properties``
             * will add it as a input of the workflow, as follow using this name: ``${actionName}-properties``

             Example for workflow named `resource-assignment`:

             .. code-block:: json
                :caption: **dynamic input**

                "resource-assignment-properties": {
                  "required": true,
                  "type": "dt-resource-assignment-properties"
                }

   * - outputs
     - | Defines the outputs of the execution; there can be as many output as needed.
       | Depending on the Component (see :ref:`node_type` -> Component) of use, some attribute might be retrievable.

       .. list-table::
            :widths: 50 50
            :header-rows: 1

            * - type
              - value
            * - data type (complex / primitive)
              - value resolvable using :ref:`expression` -> get_attribute
   * - steps
     - | Defines the actual step to execute as part of the workflow
       |
       .. list-table::
          :widths: 25 25 50
          :header-rows: 1

          * - step-name
            - description
            - target
          * - name of the step
            - step description
            - | a node_template implementing on of the supported Node Type (see :ref:`node_type` -> DG),
                either a Component or a DG
              | (see :ref:`workflow_single_action` or :ref:`workflow_multiple_actions`)

Example:

.. code-block:: json
   :caption: **workflow example**

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

`TOSCA definition <http://docs.oasis-open.org/tosca/TOSCA-Simple-Profile-YAML/v1.2/csd01/TOSCA-Simple-Profile-YAML-v1.2-csd01.html#_Toc494454203>`_

