.. This work is a derivative of https://wiki.onap.org/display/DW/Modeling+Concepts
.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2020 Deutsche Telekom AG.

.. _tosca_meta:

Tosca Meta
----------

Tosca meta file captures the model entities that compose the cba package name, version, type and searchable tags.

.. list-table::
   :widths: 20 15 15 50
   :header-rows: 1

   * - Attribute
     - R/C/O
     - Data Type
     - Description
   * - TOSCA-Meta-File-Version
     - Required
     - String
     - The attribute that holds TOSCA-Meta-File-Version. Set to 1.0.0
   * - CSAR-Version
     - Required
     - String
     - The attribute that holds CSAR-version. Set to 1.0
   * - Created-By
     - Required
     - String
     - The user/s that created the CBA
   * - Entry-Definitions
     - Required
     - String
     - The attribute that holds the entry points file PATH to the main cba tosca definition file
       or non tosca script file.
   * - Template-Name
     - Required
     - String
     - The attribute that holds the blueprint name
   * - Template-Version
     - Required
     - String
     - | The attribute that holds the blueprint version
       | **X.Y.Z**
       | X=Major version
       | Y=Minor Version
       | Z=Revision Version
       | X=Ex. 1.0.0
   * - Template-Type
     - Required
     - String
     - | The attribute that holds the blueprint package types.
       | Valid Options:
       * "DEFAULT" – .JSON file consistent of tosca based cba package that describes the package intent.
       * "KOTLIN_DSL" – .KT file consistent of tosca based cba package that describes the package intent
         composed using Domain Specific Language (DSL).
       * "GENERIC_SCRIPT" – Script file consistent of NONE tosca based cba package that describes the package intent
         using DSL Language.
       | If not specified in the tosca.meta file the default is "DEFAULT"
   * - Template-Tags
     - Required
     - String
     - The attribute that holds the blueprint package comma delimited list of Searchable attributes.

**Template Type Reference**

**Default Template Type**

https://git.onap.org/ccsdk/cds/tree/components/model-catalog/blueprint-model/test-blueprint/capability_cli/TOSCA-Metadata/TOSCA.meta

**KOTLIN_DSL Template Type**

https://git.onap.org/ccsdk/cds/tree/components/model-catalog/blueprint-model/test-blueprint/resource-audit/TOSCA-Metadata/TOSCA.meta

**GENERIC_SCRIPT Template Type**

https://git.onap.org/ccsdk/cds/tree/components/model-catalog/blueprint-model/test-blueprint/capability_python/TOSCA-Metadata/TOSCA.meta