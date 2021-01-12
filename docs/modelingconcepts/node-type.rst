.. This work is a derivative of https://wiki.onap.org/display/DW/Modeling+Concepts#Concepts-703799064
.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2020 Deutsche Telekom AG.

.. _node_type:

Node type
---------

`TOSCA definition
<http://docs.oasis-open.org/tosca/TOSCA-Simple-Profile-YAML/v1.2/csd01/TOSCA-Simple-Profile-YAML-v1.2-csd01.html#_Toc494454215>`_

In CDS, we have mainly two distinct types: components and source. We have some other type as well,
listed in the other section.

.. tabs::

   .. tab:: Component

      **Component:**

      Used to represent a **functionality** along with its **contract**, such as **inputs**, **ouputs**, and **attributes**

      `Here <https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/definition-type/starter-type/node_type/tosca.nodes.Component.json>`_
      is the root component TOSCA node type from which other node type will derive:

      .. code-block:: json
         :caption: **tosca.nodes.Component**

         {
           "description": "This is default Component Node",
           "version": "1.0.0",
           "derived_from": "tosca.nodes.Root"
         }

      **Bellow is a list of supported components**

      .. tabs::

         .. tab:: resource-resolution

            **component-resource-resolution:**

            Used to perform resolution of **resources**.

            Requires as many as artifact-mapping-resource (see :ref:`artifact_type` -> Mapping) AND
            artifact-template-velocity (see :ref:`artifact_type` -> Jinja) as needed.

            **Output result:**

            Will put the resolution result as an **attribute** in the workflow context called **assignment-params**.

            Using the :ref:`undefined <get_attribute expression>`, this attribute can be retrieve to be
            provided as workflow output (see :ref:`workflow`).

            **Specify which template to resolve:**

            Currently, resolution is bounded to a template. To specify which template to use, you
            need to fill in the `artifact-prefix-names` field.

            See :ref:`template` to understand what the artifact prefix name is.

            **Storing the result:**

            To store each resource being resolved, along with their status, and the resolved template, `store-result` should be set to `true`.

            Also, when storing the data, it must be in the context of either a `resource-id` and `resource-type`, or based on a given `resolution-key`


            The concept of resource-id / resource-type, or resolution-key, is to uniquely identify a specific resolution that
            has been performed for a given action. Hence the resolution-key has to be unique for a given blueprint name, blueprint version, action name.

            Through the combination of the fields mentioned previously, one could retrieved what has been resolved. This is useful to manage the life-cycle of the resolved resource, the life-cycle of the template, along with sharing with external systems the outcome of a given resolution.

            The resource-id / resource-type combo is more geared to uniquely identify a resource in AAI, or external system. For example, for a given AAI resource, say a PNF, you can trigger a given CDS action, and then you will be able to manage all the resolved resources bound to this PNF. Even we could have a history of what has been assigned, unassigned for this given AAI resource.

            .. warning:: Important not to confuse and AAI resource (e.g. a topology element,
                         or service related element) with the resources resolved by CDS, which can be seen
                         as parameters required to derived a network configuration.

            **Run the resolution multiple time:**

            If you need to run the same resolution component multiple times, use the field `occurence`.
            This will add the notion of occurrence to the resolution, and if storing the results, resources
            and templates, they will be accessible for each occurrence.

            Occurrence is a number between 1 and N; when retrieving information
            for a given occurrence, the first iteration starts at 1.

            This feature is useful when you need to apply the same configuration accross network elements.

            `Here <https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/definition-type/starter-type/node_type/component-resource-resolution.json>`_
            is the definition:

            .. code-block:: json
              :caption: **component-resource-resolution**

              {
                "description": "This is Resource Assignment Component API",
                "version": "1.0.0",
                "attributes": {
                  "assignment-params": {
                    "required": true,
                    "type": "string"
                  }
                },
                "capabilities": {
                  "component-node": {
                    "type": "tosca.capabilities.Node"
                  }
                },
                "interfaces": {
                  "ResourceResolutionComponent": {
                    "operations": {
                      "process": {
                        "inputs": {
                          "resolution-key": {
                            "description": "Key for service instance related correlation.",
                            "required": false,
                            "type": "string"
                          },
                          "occurrence": {
                            "description": "Number of time to perform the resolution.",
                            "required": false,
                            "default": 1,
                            "type": "integer"
                          },
                          "store-result": {
                            "description": "Whether or not to store the output.",
                            "required": false,
                            "type": "boolean"
                          },
                          "resource-type": {
                            "description": "Request type.",
                            "required": false,
                            "type": "string"
                          },
                          "artifact-prefix-names": {
                            "required": true,
                            "description": "Template , Resource Assignment Artifact Prefix names",
                            "type": "list",
                            "entry_schema": {
                              "type": "string"
                            }
                          },
                          "request-id": {
                            "description": "Request Id, Unique Id for the request.",
                            "required": true,
                            "type": "string"
                          },
                          "resource-id": {
                            "description": "Resource Id.",
                            "required": false,
                            "type": "string"
                          },
                          "action-name": {
                            "description": "Action Name of the process",
                            "required": false,
                            "type": "string"
                          },
                          "dynamic-properties": {
                            "description": "Dynamic Json Content or DSL Json reference.",
                            "required": false,
                            "type": "json"
                          }
                        },
                        "outputs": {
                          "resource-assignment-params": {
                            "required": true,
                            "type": "string"
                          },
                          "status": {
                            "required": true,
                            "type": "string"
                          }
                        }
                      }
                    }
                  }
                },
                "derived_from": "tosca.nodes.Component"
              }

         .. tab:: script-executor

            **component-script-executor:**

            Used to **execute** a script to perform **NETCONF, RESTCONF, SSH commands**
            from within the runtime container of CDS.

            Two type of scripts are supported:

            * Kotlin: offer a way more integrated scripting framework, along
              with a way faster processing capability. See more about Kotlin script: https://github.com/Kotlin/KEEP/blob/master/proposals/scripting-support.md
            * Python: uses Jython which is bound to Python 2.7, end of life Januray 2020.
              See more about Jython: https://www.jython.org/

            The `script-class-reference` field need to reference

            * for kotlin: the package name up to the class. e.g. com.example.Bob
            * for python: it has to be the path from the Scripts folder, e.g. Scripts/python/Bob.py

            `Here <https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/definition-type/starter-type/node_type/component-script-executor.json>`_
            is the definition

            .. _test_test_test:

            .. code-block:: json
              :caption: **component-script-executor**

                {
                  "description": "This is Netconf Transaction Configuration Component API",
                  "version": "1.0.0",
                  "interfaces": {
                    "ComponentScriptExecutor": {
                      "operations": {
                        "process": {
                          "inputs": {
                            "script-type": {
                              "description": "Script type, kotlin type is supported",
                              "required": true,
                              "type": "string",
                              "default": "internal",
                              "constraints": [
                                {
                                  "valid_values": [
                                    "kotlin",
                                    "jython",
                                    "internal"
                                  ]
                                }
                              ]
                            },
                            "script-class-reference": {
                              "description": "Kotlin Script class name with full package or jython script name.",
                              "required": true,
                              "type": "string"
                            },
                            "dynamic-properties": {
                              "description": "Dynamic Json Content or DSL Json reference.",
                              "required": false,
                              "type": "json"
                            }
                          },
                          "outputs": {
                            "response-data": {
                              "description": "Execution Response Data in JSON format.",
                              "required": false,
                              "type": "string"
                            },
                            "status": {
                              "description": "Status of the Component Execution ( success or failure )",
                              "required": true,
                              "type": "string"
                            }
                          }
                        }
                      }
                    }
                  },
                  "derived_from": "tosca.nodes.Component"
                }

         .. tab:: remote-script-executor

            **component-remote-script-executor:**

            Used to **execute** a python script in a dedicated micro-service, providing a Python 3.6 environment.

            **Output result:**

            prepare-environment-logs: will contain the logs for all the pip install of ansible_galaxy setup

            execute-command-logs: will contain the execution logs of the script, that were printed into stdout

            Using the get_attribute expression (see :ref:`expression` -> get_attribute),
            this attribute can be retrieve to be provided as workflow output (see :ref:`workflow`).

            **Params:**

            The `command` field need to reference the path from the Scripts folder of the
            scripts to execute, e.g. Scripts/python/Bob.py

            The `packages` field allow to provide a list of **PIP package** to install in the target environment,
            or a requirements.txt file. Also, it supports **Ansible role**.

            If **requirements.txt** is specified, then it should be **provided** as
            part of the **Environment** folder of the CBA.

            .. code-block:: json
               :caption: **Example**

               "packages": [
                 {
                   "type": "pip",
                   "package": [
                     "requirements.txt"
                   ]
                 },
                 {
                   "type": "ansible_galaxy",
                   "package": [
                     "juniper.junos"
                   ]
                 }
               ]

            The `argument-properties` allows to specified input argument to the script to execute. They should be
            expressed in a DSL, and they will be ordered as specified.

            .. code-block:: json
               :caption: **Example**

               "ansible-argument-properties": {
                 "arg0": "-i",
                 "arg1": "Scripts/ansible/inventory.yaml",
                 "arg2": "--extra-vars",
                 "arg3": {
                   "get_attribute": [
                     "resolve-ansible-vars",
                     "",
                     "assignment-params",
                     "ansible-vars"
                   ]
                 }
               }

            The `dynamic-properties` can be anything that needs to be passed to the
            script that couldn't be passed as an argument, such as JSON object, etc... If used, they will be passed
            in as the last argument of the Python script.

            `Here <https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/definition-type/starter-type/node_type/component-remote-python-executor.json>`_
            is the definition

            .. code-block:: json
               :caption: **component-remote-script-executor**

               {
                 "description": "This is Remote Python Execution Component.",
                 "version": "1.0.0",
                 "attributes": {
                   "prepare-environment-logs": {
                     "required": false,
                     "type": "string"
                   },
                   "execute-command-logs": {
                     "required": false,
                     "type": "list",
                     "entry_schema": {
                       "type": "string"
                     }
                   },
                   "response-data": {
                     "required": false,
                     "type": "json"
                   }
                 },
                 "capabilities": {
                   "component-node": {
                     "type": "tosca.capabilities.Node"
                   }
                 },
                 "interfaces": {
                   "ComponentRemotePythonExecutor": {
                     "operations": {
                       "process": {
                         "inputs": {
                           "endpoint-selector": {
                             "description": "Remote Container or Server selector name.",
                             "required": false,
                             "type": "string",
                             "default": "remote-python"
                           },
                           "dynamic-properties": {
                             "description": "Dynamic Json Content or DSL Json reference.",
                             "required": false,
                             "type": "json"
                           },
                           "argument-properties": {
                             "description": "Argument Json Content or DSL Json reference.",
                             "required": false,
                             "type": "json"
                           },
                           "command": {
                             "description": "Command to execute.",
                             "required": true,
                             "type": "string"
                           },
                           "packages": {
                             "description": "Packages to install based on type.",
                             "required": false,
                             "type" : "list",
                             "entry_schema" : {
                               "type" : "dt-system-packages"
                             }
                           }
                         }
                       }
                     }
                   }
                 },
                 "derived_from": "tosca.nodes.Component"
               }

         .. tab:: remote-ansible-executor

            **component-remote-ansible-executor:**

            Used to **execute** an ansible playbook hosted in AWX/Anisble Tower.

            **Ouput result:**

            ansible-command-status: status of the command

            ansible-command-logs: will contain the execution logs of the playbook

            Using the get_attribute expression, this attribute can be retrieve to be provided as workflow output (see Workflow).

            **Param:**

            TBD

            `Here <https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/definition-type/starter-type/node_type/component-remote-ansible-executor.json>`_
            is the definition

            .. code-block:: json
               :caption: **component-remote-script-executor**

                {
                  "description": "This is Remote Ansible Playbook (AWX) Execution Component.",
                  "version": "1.0.0",
                  "attributes": {
                    "ansible-command-status": {
                      "required": true,
                      "type": "string"
                    },
                    "ansible-command-logs": {
                      "required": true,
                      "type": "string"
                    }
                  },
                  "capabilities": {
                    "component-node": {
                      "type": "tosca.capabilities.Node"
                    }
                  },
                  "interfaces": {
                    "ComponentRemoteAnsibleExecutor": {
                      "operations": {
                        "process": {
                          "inputs": {
                            "job-template-name": {
                              "description": "Primary key or name of the job template to launch new job.",
                              "required": true,
                              "type": "string"
                            },
                            "limit": {
                              "description": "Specify host limit for job template to run.",
                              "required": false,
                              "type": "string"
                            },
                            "inventory": {
                              "description": "Specify inventory for job template to run.",
                              "required": false,
                              "type": "string"
                            },
                            "extra-vars": {
                              "required": false,
                              "type": "json",
                              "description": "json formatted text that contains extra variables to pass on."
                            },
                            "tags": {
                              "description": "Specify tagged actions in the playbook to run.",
                              "required": false,
                              "type": "string"
                            },
                            "skip-tags": {
                              "description": "Specify tagged actions in the playbook to omit.",
                              "required": false,
                              "type": "string"
                            },
                            "endpoint-selector": {
                              "description": "Remote AWX Server selector name.",
                              "required": true,
                              "type": "string"
                            }
                          }
                        }
                      }
                    }
                  },
                  "derived_from": "tosca.nodes.Component"
                }

   .. tab:: Source

      **Source:**

      Used to represent a **type of source** to **resolve** a **resource**, along with the expected **properties**

      Defines the **contract** to resolve a resource.

      `Here <https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/definition-type/starter-type/node_type/tosca.nodes.ResourceSource.json>`_
      is the root component TOSCA node type from which other node type will derive:

      .. code-block::
         :caption: **tosca.nodes.Component**

         {
           "description": "TOSCA base type for Resource Sources",
           "version": "1.0.0",
           "derived_from": "tosca.nodes.Root"
         }

      **Bellow is a list of supported sources**

      .. tabs::

         .. tab:: input

            **Input:**

            Expects the **value to be provided as input** to the request.

            `Here <https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/definition-type/starter-type/node_type/source-input.json>`_
            is the Definition

            .. code-block::
               :caption: **source-input**

               {
                 "description": "This is Input Resource Source Node Type",
                 "version": "1.0.0",
                 "properties": {},
                 "derived_from": "tosca.nodes.ResourceSource"
               }

         .. tab:: default

            **Default:**

            Expects the **value to be defaulted** in the model itself.

            `Here <https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/definition-type/starter-type/node_type/source-default.json>`_
            is the Definition

            .. code-block:: json
               :caption: **source-default**

               {
                 "description": "This is Default Resource Source Node Type",
                 "version": "1.0.0",
                 "properties": {},
                 "derived_from": "tosca.nodes.ResourceSource"
               }

         .. tab:: rest

            **REST**

            Expects the **URI along with the VERB and the payload**, if needed.

            CDS is currently deployed along the side of SDNC, hence the **default** rest
            **connection** provided by the framework is to **SDNC MDSAL**.

            .. list-table::
               :widths: 25 50 25
               :header-rows: 1

               * - Property
                 - Description
                 - Scope
               * - type
                 - Expected output value, only JSON supported for now
                 - Optional
               * - verb
                 - HTTP verb for the request - default value is GET
                 - Optional
               * - payload
                 - Payload to sent
                 - Optional
               * - endpoint-selector
                 - **Specific REST system** to interact with to (see **Dynamic Endpoint**)
                 - Optional
               * - url-path
                 - URI
                 - Mandatory
               * - path
                 - JSON path to the value to fetch from the response
                 - Mandatory
               * - expression-type
                 - Path expression type - default value is JSON_PATH
                 - Optional

            `Here <https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/definition-type/starter-type/node_type/source-rest.json>`_
            is the definition:

            .. code-block:: json
               :caption: **source-rest**

                {
                  "description": "This is Rest Resource Source Node Type",
                  "version": "1.0.0",
                  "properties": {
                    "type": {
                      "required": false,
                      "type": "string",
                      "default": "JSON",
                      "constraints": [
                        {
                          "valid_values": [
                            "JSON"
                          ]
                        }
                      ]
                    },
                    "verb": {
                      "required": false,
                      "type": "string",
                      "default": "GET",
                      "constraints": [
                        {
                          "valid_values": [
                            "GET",
                            "POST",
                            "DELETE",
                            "PUT"
                          ]
                        }
                      ]
                    },
                    "payload": {
                      "required": false,
                      "type": "string",
                      "default": ""
                    },
                    "endpoint-selector": {
                      "required": false,
                      "type": "string"
                    },
                    "url-path": {
                      "required": true,
                      "type": "string"
                    },
                    "path": {
                      "required": true,
                      "type": "string"
                    },
                    "expression-type": {
                      "required": false,
                      "type": "string",
                      "default": "JSON_PATH",
                      "constraints": [
                        {
                          "valid_values": [
                            "JSON_PATH",
                            "JSON_POINTER"
                          ]
                        }
                      ]
                    },
                    "input-key-mapping": {
                      "required": false,
                      "type": "map",
                      "entry_schema": {
                        "type": "string"
                      }
                    },
                    "output-key-mapping": {
                      "required": false,
                      "type": "map",
                      "entry_schema": {
                        "type": "string"
                      }
                    },
                    "key-dependencies": {
                      "required": true,
                      "type": "list",
                      "entry_schema": {
                        "type": "string"
                      }
                    }
                  },
                  "derived_from": "tosca.nodes.ResourceSource"
                }
         .. tab:: sql

            **SQL**

            Expects the **SQL query** to be modeled; that SQL query can be parameterized,
            and the parameters be other resources resolved through other means.
            If that's the case, this data dictionary definition will have to define ``key-dependencies`` along with ``input-key-mapping``.

            CDS is currently deployed along the side of SDNC, hence the **primary** database
            **connection** provided by the framework is to **SDNC database**.

            .. list-table::
               :widths: 25 50 25

               * - Property
                 - Description
                 - Scope
               * - type
                 - Database type, only SQL supported for now
                 - Mandatory
               * - endpoint-selector
                 - Specific Database system to interact with to (see **Dynamic Endpoint**)
                 - Optional
               * - query
                 - Statement to execute
                 - Mandatory


            `Here <https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/definition-type/starter-type/node_type/source-processor-db.json>`_
            is the definition:

            .. code-block:: json
               :caption: **source-db**

                {
                  "description": "This is Database Resource Source Node Type",
                  "version": "1.0.0",
                  "properties": {
                    "type": {
                      "required": true,
                      "type": "string",
                      "constraints": [
                        {
                          "valid_values": [
                            "SQL"
                          ]
                        }
                      ]
                    },
                    "endpoint-selector": {
                      "required": false,
                      "type": "string"
                    },
                    "query": {
                      "required": true,
                      "type": "string"
                    },
                    "input-key-mapping": {
                      "required": false,
                      "type": "map",
                      "entry_schema": {
                        "type": "string"
                      }
                    },
                    "output-key-mapping": {
                      "required": false,
                      "type": "map",
                      "entry_schema": {
                        "type": "string"
                      }
                    },
                    "key-dependencies": {
                      "required": true,
                      "type": "list",
                      "entry_schema": {
                        "type": "string"
                      }
                    }
                  },
                  "derived_from": "tosca.nodes.ResourceSource"
                }

         .. tab:: capability

                  **Capability:**

                  Expects a **script** to be provided.

                  .. list-table::
                     :widths: 25 50 25
                     :header-rows: 1

                     * - Property
                       - Description
                       - Scope
                     * - script-type
                       - The type of the script - default value is Koltin
                       - Optional
                     * - script-class-reference
                       - The name of the class to use to create an instance of the script
                       - Mandatory

            `Here <https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/definition-type/starter-type/node_type/source-capability.json>`_
            is the definition:

            .. code-block:: json
               :caption: **source-capability**

                {
                  "description": "This is Component Resource Source Node Type",
                  "version": "1.0.0",
                  "properties": {
                    "script-type": {
                      "required": true,
                      "type": "string",
                      "default": "kotlin",
                      "constraints": [
                        {
                          "valid_values": [
                            "internal",
                            "kotlin",
                            "jython"
                          ]
                        }
                      ]
                    },
                    "script-class-reference": {
                      "description": "Capability reference name for internal and kotlin, for jython script file path",
                      "required": true,
                      "type": "string"
                    },
                    "key-dependencies": {
                      "description": "Resource Resolution dependency dictionary names.",
                      "required": true,
                      "type": "list",
                      "entry_schema": {
                        "type": "string"
                      }
                    }
                  },
                  "derived_from": "tosca.nodes.ResourceSource"
                }

   .. tab:: Other

      **Other:**

      .. tabs::

         .. tab:: DG

            **dg-generic:**

            Identifies a Directed Graph used as **imperative workflow**.

            .. list-table::
               :widths: 40 40 20
               :header-rows: 1

               * - Property
                 - Description
                 - Scope
               * - dependency-node-templates
                 - The node template the workflow depends on
                 - Required

            `Here <https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/definition-type/starter-type/node_type/dg-generic.json>`_
            is the definition:

            .. code-block:: json
               :caption: **dg-generic**

                {
                  "description": "This is Generic Directed Graph Type",
                  "version": "1.0.0",
                  "properties": {
                    "content": {
                      "required": true,
                      "type": "string"
                    },
                    "dependency-node-templates": {
                      "required": true,
                      "description": "Dependent Step Components NodeTemplate name.",
                      "type": "list",
                      "entry_schema": {
                        "type": "string"
                      }
                    }
                  },
                  "derived_from": "tosca.nodes.DG"
                }

            A node_template of this type always provide one artifact, of type artifact-directed-graph,
            which will be located under the Plans/ folder within the CBA.

            .. code-block:: json
               :caption: **node_template example**

                {
                  "config-deploy-process": {
                    "type": "dg-generic",
                    "properties": {
                      "content": {
                        "get_artifact": [
                          "SELF",
                          "dg-config-deploy-process"
                        ]
                      },
                      "dependency-node-templates": [
                        "nf-account-collection",
                        "execute"
                      ]
                    },
                    "artifacts": {
                      "dg-config-deploy-process": {
                        "type": "artifact-directed-graph",
                        "file": "Plans/CONFIG_ConfigDeploy.xml"
                      }
                    }
                  }
                }

            In the DG bellow, the execute node refers to the node_template.

            .. code-block:: xml
               :caption: **Plans/CONFIG_ConfigDeploy.xml**

               <service-logic
               xmlns='http://www.onap.org/sdnc/svclogic'
               xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
               xsi:schemaLocation='http://www.onap.org/sdnc/svclogic ./svclogic.xsd' module='CONFIG' version='1.0.0'>
                  <method rpc='ConfigDeploy' mode='sync'>
                     <block atomic="true">
                        <execute plugin="nf-account-collection" method="process">
                           <outcome value='failure'>
                              <return status="failure">
                              </return>
                           </outcome>
                           <outcome value='success'>
                              <execute plugin="execute" method="process">
                                 <outcome value='failure'>
                                    <return status="failure">
                                    </return>
                                 </outcome>
                                 <outcome value='success'>
                                    <return status='success'>
                                    </return>
                                 </outcome>
                              </execute>
                           </outcome>
                        </execute>
                     </block>
                  </method>
               </service-logic>

         .. tab:: VNF

            **tosca.nodes.VNF**

            Identifies a VNF, can be used to **correlate** any type of **VNF** related **information**.

            https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/definition-type/starter-type/node_type/tosca.nodes.Vnf.json

            .. code-block:: json
               :caption: **tosca.nodes.vnf**

               {
                 "description": "This is VNF Node Type",
                 "version": "1.0.0",
                 "derived_from": "tosca.nodes.Root"
               }

            **vnf-netconf-device**

            Represents the VNF information to **establish** a **NETCONF communication**.

            https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/definition-type/starter-type/node_type/vnf-netconf-device.json

            .. code-block:: json
               :caption: **vnf-netconf-device**

               {
                 "description": "This is VNF Device with Netconf  Capability",
                 "version": "1.0.0",
                 "capabilities": {
                   "netconf": {
                     "type": "tosca.capabilities.Netconf",
                     "properties": {
                       "login-key": {
                         "required": true,
                         "type": "string",
                         "default": "sdnc"
                       },
                       "login-account": {
                         "required": true,
                         "type": "string",
                         "default": "sdnc-tacacs"
                       },
                       "source": {
                         "required": false,
                         "type": "string",
                         "default": "npm"
                       },
                       "target-ip-address": {
                         "required": true,
                         "type": "string"
                       },
                       "port-number": {
                         "required": true,
                         "type": "integer",
                         "default": 830
                       },
                       "connection-time-out": {
                         "required": false,
                         "type": "integer",
                         "default": 30
                       }
                     }
                   }
                 },
                 "derived_from": "tosca.nodes.Vnf"
               }
