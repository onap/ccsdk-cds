.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

dt-netbox-ip code
=================

.. code-block:: json
   :linenos:

   {
     "version": "1.0.0",
     "description": "This is Netbox IP Data Type",
     "properties": {
       "address": {
         "required": true,
         "type": "string"
       },
       "id": {
         "required": true,
         "type": "integer"
       }
     },
     "derived_from": "tosca.datatypes.Root"
   }
