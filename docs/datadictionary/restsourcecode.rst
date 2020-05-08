.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

Rest Source Code:
=================

.. code-block:: json
   :linenos:
   
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
                "GET", "POST", "DELETE", "PUT"
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
