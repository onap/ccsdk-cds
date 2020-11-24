.. This work is a derivative of https://wiki.onap.org/display/DW/Modeling+Concepts
.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2020 Deutsche Telekom AG.

.. _dynamic_payload:

Dynamic Payload
---------------

One of the most important API provided by the run time is to execute a CBA Package.

The nature of this API **request** and **response** is **model
driven** and **dynamic**.

Here is how the a **generic request** and **response** look like.

.. list-table::
   :widths: 50 50
   :header-rows: 1

   * - request
     - response
   * - .. code-block:: json

        {
          "commonHeader": {
            "originatorId": "",
            "requestId": "",
            "subRequestId": ""
          },
          "actionIdentifiers": {
            "blueprintName": "",
            "blueprintVersion": "",
            "actionName": "",
            "mode": ""
          },
          "payload": {
            "$actionName-request": {
              "$actionName-properties": {
              }
            }
          }
        }

     - .. code-block:: json

        {
          "commonHeader": {
            "originatorId": "",
            "requestId": "",
            "subRequestId": ""
          },
          "actionIdentifiers": {
            "blueprintName": "",
            "blueprintVersion": "",
            "actionName": "",
            "mode": ""
          },
          "payload": {
            "$actionName-response": {
            }
          }
        }

The ``actionName``, under the ``actionIdentifiers`` refers to the name of a
Workflow (see :ref:`workflow`)

The content of the ``payload`` is what is fully dynamic / model driven.

The first **top level element** will always be either
``$actionName-request`` for a request or ``$actionName-response`` for a response.

Then the **content within this element** is fully based on the
**workflow** **inputs** and **outputs**.

During the :ref:`enrichment` CDS will aggregate all the resources
defined to be resolved as input (see :ref:`node_type` -> Source -> Input), within mapping definition files
(see :ref:`artifact_type` -> Mapping), as data-type, that will then be use as type
of an  input called ``$actionName-properties``.