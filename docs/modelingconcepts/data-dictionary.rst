.. This work is a derivative of https://wiki.onap.org/display/DW/Modeling+Concepts#Concepts-1386016968
.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2020 Deutsche Telekom AG.

.. _data_dictionary:

Data Dictionary
---------------

A data dictionary **models the how** a specific **resource** can be **resolved**.

A resource is a **variable/parameter** in the context of the service.
It can be anything, but it should not be confused with SDC or Openstack resources.

A data dictionary can have **multiple sources** to handle resolution in different ways.

The main goal of data dictionary is to define **re-usable** entity that could be shared.

**Creation** of data dictionaries is a **standalone** activity, separated from the blueprint design.

As part of modelling a data dictionary entry, the following generic information should be provided:

.. list-table::
   :widths: 25 50 25
   :header-rows: 1

   * - Property
     - Description
     - Scope
   * - updated-by
     - The creator
     - Mandatory
   * - tags
     - Information related
     - Mandatory
   * - sources
     - List of resource source instance (see :ref:`resource source`)
     - Mandatory
   * - property
     - Defines type and description, as nested JSON
     - Mandatory
   * - name
     - Data dictionary name
     - Mandatory

**Bellow are properties that all the resource source can have**

The modeling does allow for **data translation** between external capability
and CDS for both input and output key mapping.

.. list-table::
   :widths: 25 50 25
   :header-rows: 1

   * - Property
     - Description
     - Scope
   * - input-key-mapping
     - map of resources required to perform the request/query. The left hand-side is what is used within
       the query/request, the right hand side refer to a data dictionary instance.
     - Optional
   * - output-key-mapping
     - name of the resource to be resolved mapped to the value resolved by the request/query.
     - Optional
   * - key-dependencies
     - | list of data dictionary instances to be resolved prior the resolution of this specific resource.
       | during run time execution the key dependencies are recursively sorted and resolved
         in batch processing using the `acyclic graph algorithm
         <https://en.wikipedia.org/wiki/Directed_acyclic_graph>`_
     - Optional

**Example:**

``vf-module-model-customization-uuid`` and ``vf-module-label`` are two data dictionaries.
A SQL table, VF_MODULE_MODEL, exist to correlate them.

Here is how input-key-mapping, output-key-mapping and key-dependencies can be used:

.. list-table::
   :widths: 100
   :header-rows: 1

   * - vf-module-label data dictionary
   * - .. code-block:: json

        {
          "name" : "vf-module-label",
          "tags" : "vf-module-label",
          "updated-by" : "adetalhouet",
          "property" : {
            "description" : "vf-module-label",
            "type" : "string"
          },
          "sources" : {
            "primary-db" : {
              "type" : "source-primary-db",
              "properties" : {
                "type" : "SQL",
                "query" : "select sdnctl.VF_MODULE_MODEL.vf_module_label as vf_module_label from sdnctl.VF_MODULE_MODEL where sdnctl.VF_MODULE_MODEL.customization_uuid=:customizationid",
                "input-key-mapping" : {
                  "customizationid" : "vf-module-model-customization-uuid"
                },
                "output-key-mapping" : {
                  "vf-module-label" : "vf_module_label"
                },
                "key-dependencies" : [ "vf-module-model-customization-uuid" ]
              }
            }
          }
        }