.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

Resource Definition 
-------------------
.. toctree::
   :maxdepth: 1

Introduction:
=============
A data dictionary models the how a specific resource can be resolved.

A resource is a variable/parameter in the context of the service. It can be anything, but it should not be confused with SDC or Openstack resources.

A data dictionary can have multiple sources to handle resolution in different ways.

The main goal of data dictionary is to define re-usable entity that could be shared.

Creation of data dictionaries is a standalone activity, separated from the blueprint design.


As part of modelling a data dictionary entry, the following generic information should be provided:

|image0|

.. |image0| image:: media/image0.jpg
   :width: 7.88889in 
   :height: 4.43750in

Bellow are properties that all the resource source have will have

The modeling does allow for data translation between external capability and CDS for both input and output key mapping.

|image1|

.. |image1| image:: media/image0.jpg
   :width: 7.88889in 
   :height: 4.43750in

Example:
========

vf-module-model-customization-uuid and vf-module-label are two data dictionaries. A SQL table, VF_MODULE_MODEL, exist to correlate them.

Here is how input-key-mapping, output-key-mapping and key-dependencies can be used:

vf-module-label data dictionary  

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


Resource source:
================

Defines the contract to resolve a resource.

A resource source is modeled, following http://docs.oasis-open.org/tosca/TOSCA-Simple-Profile-YAML/v1.0/csprd01/TOSCA-Simple-Profile-YAML-v1.0-csprd01.html#DEFN_ENTITY_NODE_TYPE, and derives from the https://wiki.onap.org/display/DW/Modeling+Concepts#ModelingConcepts-NodeResourceSource