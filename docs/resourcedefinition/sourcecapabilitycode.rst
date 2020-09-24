.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

Source Capability Code
======================

.. code-block:: json
   :linenos:

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
        "instance-dependencies": {
          "required": false,
          "description": "Instance dependency Names to Inject to Kotlin / Jython Script.",
          "type": "list",
          "entry_schema": {
            "type": "string"
          }
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
