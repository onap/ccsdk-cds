.. This work is a derivative of https://wiki.onap.org/display/DW/Modeling+Concepts
.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2020 Deutsche Telekom AG.

.. _enrichment:

Enrichment
----------

The idea is that the CBA is a self-sufficient package, hence requires
all the various types definition its using.

Reason for this is the types its using might evolve. In order for the
CBA to be bounded to the version it has been using when it has been
designed, these types are embedded in the CBA, so if they change, the
CBA is not affected.

The enrichment process will complete the package by providing all the
definition of types used:

* gather all the node-type used and put them into a :file:`node_types.json` file
* gather all the data-type used and put them into a :file:`data_types.json` file
* gather all the artifact-type used and put them into a :file:`artifact_types.json` file
* gather all the data dictionary definitions used from within the mapping files and put them
  into a :file:`resources_definition_types.json` file

.. warning::
   Before uploading a CBA, it must be enriched. If your package is already enrich,
   you do not need to perform enrichment again.

The enrichment can be run using REST API, and required the **.zip** file as input.
It will return an :file:`enriched-cba.zip` file.

.. code-block:: bash

   curl -X POST \
      'http://{{ip}}:{{cds-designtime}}/api/v1/blueprint-model/enrich' \
      -H 'content-type: multipart/form-data' \
      -F file=@cba.zip

The enrichment process will also, for all resources to be resolved as input and default:

* dynamically gather them under a data-type, named ``dt-${actionName}-properties``
* will add it as a input of the workflow, as follow using this name: ``${actionName}-properties``

Example for workflow named *resource-assignment*:

.. code-block:: JSON
   :caption: **dynamic input**

   {
     "resource-assignment-properties": {
     "required": true,
     "type": "dt-resource-assignment-properties"
   }