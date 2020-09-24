.. This work is a derivative of https://wiki.onap.org/display/DW/Modeling+Concepts#Concepts-198012600
.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2020 Deutsche Telekom AG.


.. _expression:

Expression
----------

TOSCA provides for a set of functions to reference elements within the template or to retrieve runtime values.

**Below is a list of supported expressions**

.. tabs::

   .. tab:: get_input

      **get_input**

      The **get_input** function is used to retrieve the values of properties declared
      within the inputs section of a TOSCA Service Template.

      Within CDS, this is mainly Workflow inputs.

      `TOSCA specification
      <http://docs.oasis-open.org/tosca/TOSCA-Simple-Profile-YAML/v1.2/csd01/TOSCA-Simple-Profile-YAML-v1.2-csd01.html#_Toc494454178>`_

      **Example:**

      `<https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/blueprint-model/test-blueprint/golden/Definitions/golden-blueprint.json#L210>`_

      .. code-block:: json

         "resolution-key": {
            "get_input": "resolution-key"
         }

   .. tab:: get_property

      **get_property**

      The **get_property** function is used to retrieve property values between modelable
      entities defined in the same service template.

      `TOSCA specification
      <http://docs.oasis-open.org/tosca/TOSCA-Simple-Profile-YAML/v1.2/csd01/TOSCA-Simple-Profile-YAML-v1.2-csd01.html#_Toc494454178>`_

      **Example:**

      .. code-block:: json

         "get_property": ["SELF", "property-name"]

   .. tab:: get_attribute

      **get_attribute**

      The **get_attribute** function is used to retrieve the values of named attributes declared
      by the referenced node or relationship template name.

      `TOSCA specification
      <http://docs.oasis-open.org/tosca/TOSCA-Simple-Profile-YAML/v1.2/csd01/TOSCA-Simple-Profile-YAML-v1.2-csd01.html#_Toc494454178>`_

      **Example:**

      `<https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/blueprint-model/test-blueprint/golden/Definitions/golden-blueprint.json#L64-L67>`_

      .. code-block:: json

         "get_attribute": [
            "resource-assignment",
            "assignment-params"
         ]

   .. tab:: get_operation_output

      **get_operation_output**

      The **get_operation_output** function is used to retrieve the values of variables
      exposed / exported from an interface operation.

      `TOSCA specification
      <http://docs.oasis-open.org/tosca/TOSCA-Simple-Profile-YAML/v1.2/csd01/TOSCA-Simple-Profile-YAML-v1.2-csd01.html#_Toc494454180>`_

      **Example:**

      .. code-block:: json

         "get_operation_output": ["SELF", "interface-name", "operation-name", "output-property-name"]

   .. tab:: get_artifact

      **get_artifact**

      The **get_artifact** function is used to retrieve artifact location between modelable
      entities defined in the same service template.

      `TOSCA specification
      <http://docs.oasis-open.org/tosca/TOSCA-Simple-Profile-YAML/v1.2/csd01/TOSCA-Simple-Profile-YAML-v1.2-csd01.html#_Toc494454182>`_

      **Example:**

      .. code-block:: json

         "get_artifact" : ["SELF", "artifact-template", "location", true]