.. This work is a derivative of https://wiki.onap.org/display/DW/Modeling+Concepts#Concepts-1386016968
.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2020 Deutsche Telekom AG.

.. _artifact_type:

Artifact Type
-------------------------------------

Represents the **type of a artifact**, used to **identify** the
**implementation** of the functionality supporting this type of artifact.

`TOSCA definition <http://docs.oasis-open.org/tosca/TOSCA-Simple-Profile-YAML/v1.2/csd01/TOSCA-Simple-Profile-YAML-v1.2-csd01.html#_Toc494454213>`_

This node was created, derived from ``tosca.artifacts.Root`` to be the root TOSCA node for all artifact.

.. code-block:: JSON
   :caption: **tosca.artifacts.Implementation**

   {
     "description": "TOSCA base type for implementation artifacts",
     "version": "1.0.0",
     "derived_from": "tosca.artifacts.Root"
   }

**Bellow is a list of supported artifact types**

.. tabs::

   .. tab:: Velocity

      **artifact-template-velocity**

      Represents an Apache Velocity template.

      Apache Velocity allow to insert **logic** (if / else / loops / etc) when processing the output of a template/text.

      File must have **.vtl** extension.

      The **template** can represent anything, such as device config, payload to interact with 3rd party systems,
      :ref:`resource-accumulator template`, etc...

      Often a template will be **parameterized**, and each **parameter**
      must be defined within an mapping file (see 'Mapping' in this tab).

      `Velocity reference document <http://velocity.apache.org/engine/1.7/user-guide.html>`_

      `Here <https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/definition-type/starter-type/artifact_type/artifact-template-velocity.json>`_
      is the TOSCA artifact type:

      .. code-block:: JSON
         :caption: **artifact-template-velocity**

         {
           "description": "TOSCA base type for implementation artifacts",
           "version": "1.0.0",
           "derived_from": "tosca.artifacts.Root"
         }

   .. tab:: Jinja

      **artifact-template-jinja**

      Represents an Jinja template.

      Jinja template allow to insert **logic** (if / else / loops / etc) when processing the output of a template/text.

      File must have **.jinja** extension.

      The **template** can represent **anything**, such as device config,
      payload to interact with 3rd party systems, :ref:`resource-accumulator template`, etc...

      Often a template will be parameterized, and each parameter must be defined within an :ref:`mapping file`.

      `Jinja reference document <https://jinja.palletsprojects.com/en/2.10.x/>`_

      `Here <https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/definition-type/starter-type/artifact_type/artifact-template-jinja.json>`_
      is the TOSCA artifact type:

      .. code-block:: JSON
         :caption: **artifact-template-jinja**

         {
           "description": " Jinja Template used for Configuration",
           "version": "1.0.0",
           "file_ext": [
             "jinja"
           ],
           "derived_from": "tosca.artifacts.Implementation"
         }

   .. tab:: Mapping

      **artifact-mapping-resource**

      This type is meant to represent **mapping** files defining the **contract of each resource** to be resolved.

      Each **parameter** in a template **must** have a corresponding mapping definition,
      modeled using datatype-resource-assignment (see :ref:`data_type`-> resources-asignment).

      Hence the mapping file is meant to be a list of entries defined using datatype-resource-assignment
      (see :ref:`data_type`-> resources-asignment).

      File must have **.json** extension.

      The **template** can represent **anything**, such as device config,
      payload to interact with 3rd party systems, resource-accumulator template, etc...

      `Here <https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/definition-type/starter-type/artifact_type/artifact-mapping-resource.json>`_
      is the TOSCA artifact type:

      .. code-block:: JSON
         :caption: **artifact-mapping-resource**

         {
           "description": "Resource Mapping File used along with Configuration template",
           "version": "1.0.0",
           "file_ext": [
             "json"
           ],
           "derived_from": "tosca.artifacts.Implementation"
         }

      The mapping file basically contains a reference to the data dictionary to use
      to resolve a particular resource.

      The data dictionary defines the HOW and the mapping defines the WHAT.

      **Relation between data dictionary, mapping and template.**

      Below are two examples using color coding to help understand the relationships.

      In orange is the information regarding the template. As mentioned before,
      template is part of the blueprint itself, and for the blueprint to know what template to use,
      the name has to match.

      In green is the relationship between the value resolved within the template,
      and how it's mapped coming from the blueprint.

      In blue is the relationship between a resource mapping to a data dictionary.

      In red is the relationship between the resource name to be resolved and the HEAT environment variables.

      The key takeaway here is that whatever the value is for each color, it has to match all across.
      This means both right and left hand side are equivalent; it's all on the designer to express
      the modeling for the service. That said, best practice is example 1.

      .. image:: ../media/dd_mapping_template_rel.jpg
         :width: 250pt
         :align: center

   .. tab:: Directed Graph

      **artifact-directed-graph**

      Represents a directed graph.

      This is to represent a **workflow**.

      File must have **.xml** extension.

      Here is the list of executors currently supported (see here for explanation and full potential list:
      `Service Logic Interpreter Nodes <https://wiki.onap.org/display/DW/Service+Logic+Interpreter+Nodes>`_

      * execute
      * block
      * return
      * break
      * exit

      `Here <https://github.com/onap/ccsdk-cds/blob/master/components/model-catalog/definition-type/starter-type/artifact_type/artifact-directed-graph.json>`_
      is the TOSCA artifact type:

      .. code-block:: json
         :caption: **artifact-directed-graph**

         {
           "description": "Directed Graph File",
           "version": "1.0.0",
           "file_ext": [
             "json",
             "xml"
           ],
           "derived_from": "tosca.artifacts.Implementation"
         }
