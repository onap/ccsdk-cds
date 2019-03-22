.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

Resource Definition 
-------------------
.. toctree::
   :maxdepth: 1

Introduction:
=============
A Resource Definition defines a specifc resource that can be resolved using the bellow supported sources.

A Resource Definition can support multiple sources.

The main goal of Resource Definition is to define generic entity that could be shared accross services.


Resolution sources:
===================

   * Input
   * Default
   * DB
   * REST
   * Capability

Artifacts:
==========

   * artifact-mapping-resource
   * artifact-template-velocity
   * artifact-directed-graph

Node type:
==========
	
   * component-resource-resolution
   * component-jython-executor
   * component-netconf-executor
   * component-restconf-executor

Data type:
==========
   * vnf-netconf-device