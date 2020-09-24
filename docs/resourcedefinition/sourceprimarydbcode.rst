.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

Source Primary DB Code:
=======================

.. code-block:: json
   :linenos:

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
