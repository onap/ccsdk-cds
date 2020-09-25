.. This work is a derivative of https://wiki.onap.org/display/DW/Modeling+Concepts#Concepts-1581473264
.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2020 Deutsche Telekom AG.

.. _data_type:

Data type
---------

Represents the **schema** of a specific type of **data**.

Supports both **primitive** and **complex** data types:

.. list-table::
   :widths: 50 50
   :header-rows: 1

   * - Primitive
     - Complex
   * - * string
       * integer
       * float
       * double
       * boolean
       * timestamp
       * null
     - * json
       * list
       * array

For complex data type, an **entry schema** is required, defining the
type of value contained within the complex type, if list or array.

Users can **create** as many **data type** as needed.

.. note::

   **Creating Custom Data Types:**

   To create a custom data-type you can use a POST call to CDS endpoint:
   "<cds-ip>:<cds-port>/api/v1/model-type"

   .. code-block:: python
      :caption: **Payload:**

      {
        "model-name": "<model-name>",
        "derivedFrom": "tosca.datatypes.Root",
        "definitionType": "data_type",
        "definition": {
          "description": "<description>",
          "version": "<version-number: eg 1.0.0>",
          "properties": {<add properties of your custom data type in JSON format>},
          "derived_from": "tosca.datatypes.Root"
        },
        "description": "<description",
        "version": "<version>",
        "tags": "<model-name>,datatypes.Root.data_type",
        "creationDate": "<creation timestamp>",
        "updatedBy": "<name>"
      }

Data type are useful to manipulate data during resource resolution.
They can be used to format the JSON output as needed.

List of existing data type:
`<https://github.com/onap/ccsdk-cds/tree/master/components/model-catalog/definition-type/starter-type/data_type>`_

`TOSCA specification
<http://docs.oasis-open.org/tosca/TOSCA-Simple-Profile-YAML/v1.2/csd01/TOSCA-Simple-Profile-YAML-v1.2-csd01.html#_Toc494454187>`_

**Below is a list of existing data types**

.. tabs::

   .. tab:: resource-assignment

      **datatype-resource-assignment**

      Used to define entries within artifact-mapping-resource
      (see tab Artifact Type -> artifact-mapping-resource)

      That datatype represent a **resource** to be resolved. We also refer
      this as an **instance of a data dictionary** as it's directly linked to
      its definition.

      .. list-table::
         :widths: 50 50
         :header-rows: 1

         * - Property
           - Description
         * - property
           - Defines how the resource looks like (see datatype-property on the right tab)
         * - input-param
           - Whether the resource can be provided as input.
         * - dictionary-name
           - Reference to the name of the data dictionary (see :ref:`data_dictionary`).
         * - dictionary-source
           - Reference the source to use to resolve the resource (see :ref:`resource source`).
         * - dependencies
           - List of dependencies required to resolve this resource.
         * - updated-date
           - Date when mapping was upload.
         * - updated-by
           - Name of the person that updated the mapping.

      `<https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/definition-type/starter-type/data_type/datatype-resource-assignment.json>`_

      .. code-block:: json
         :caption: **datatype-resource-assignment**

        {
          "version": "1.0.0",
          "description": "This is Resource Assignment Data Type",
          "properties": {
            "property": {
              "required": true,
              "type": "datatype-property"
            },
            "input-param": {
              "required": true,
              "type": "boolean"
            },
            "dictionary-name": {
              "required": false,
              "type": "string"
            },
            "dictionary-source": {
              "required": false,
              "type": "string"
            },
            "dependencies": {
              "required": true,
              "type": "list",
              "entry_schema": {
                "type": "string"
              }
            },
            "updated-date": {
              "required": false,
              "type": "string"
            },
            "updated-by": {
              "required": false,
              "type": "string"
            }
          },
          "derived_from": "tosca.datatypes.Root"
        }

   .. tab:: property

      **datatype-property**

      Used to defined the **property** entry of a **resource assignment**.

      .. list-table::
         :widths: 25 75
         :header-rows: 1

         * - Property
           - Description
         * - type
           - Whether it's a primitive type, or a defined data-type
         * - description
           - Description of for the property
         * - required
           - Whether it's required or not
         * - default
           - If there is a default value to provide
         * - entry_schema
           - If the type is a complex one, such as list, define what is the type of element within the list.

      `<https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/definition-type/starter-type/data_type/datatype-property.json>`_

      .. code-block:: json
         :caption: **datatype-property**

         {
           "version": "1.0.0",
           "description": "This is Resource Assignment Data Type",
           "properties": {
             "property": {
               "required": true,
               "type": "datatype-property"
             },
             "input-param": {
               "required": true,
               "type": "boolean"
             },
             "dictionary-name": {
               "required": false,
               "type": "string"
             },
             "dictionary-source": {
               "required": false,
               "type": "string"
             },
             "dependencies": {
               "required": true,
               "type": "list",
               "entry_schema": {
                 "type": "string"
               }
             },
             "updated-date": {
               "required": false,
               "type": "string"
             },
             "updated-by": {
               "required": false,
               "type": "string"
             }
           },
           "derived_from": "tosca.datatypes.Root"
         }