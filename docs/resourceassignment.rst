.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

Resource Assignment 
-------------------
.. toctree::
   :maxdepth: 1
   
   
Component executor:
===================
Workflow:
=========

A workflow defines an overall action to be taken for the service; it can be composed of a set of sub-actions to execute. Currently, workflows are backed by Directed Graph engine.

A CBA can have as many workflow as needed.

Template:
=========

A template is an artifact.

A template is parameterized and each parameter must be defined in a corresponding mapping file.

In order to know which mapping correlate to which template, the file name must start with an artifact-prefix, serving as identifier to the overall template + mapping.

The requirement is as follow:

${artifact-prefix}-template
${artifact-prefix}-mapping

A template can represent anything, such as device config, payload to interact with 3rd party systems, resource-accumulator template, etc...

Mapping:
========
Defines the contract of each resource to be resolved. Each placeholder in the template must have a corresponding mapping definition.

A mapping is comprised of:

- name
- required / optional
- type (support complex type)
- dictionary-name
- dictionary-source

Dependencies:
=============

This allows to make sure given resources get resolved prior the resolution of the resources defining the dependency.
The dictionary fields reference to a specific data dictionary.

Resource accumulator:
=====================

In order to resolve HEAT environment variables, resource accumulator templates are being in used for Dublin.

These templates are specific to the pre-instantiation scenario, and relies on GR-API within SDNC.

It is composed of the following sections:

resource-accumulator-resolved-data: defines all the resources that can be resolved directly from the context. It expresses a direct mapping between the name of the resource and its value.

capability-data: defines what capability to use to create a specific resource, along with the ingredients required to invoke the capability and the output mapping.

- Scripts
- Library
- NetconfClient

In order to facilitate NETCONF interaction within scripts, a python NetconfClient binded to our Kotlin implementation is made available. This NetconfClient can be used when using the netconf-component-executor.

The client can be find here: https://github.com/onap/ccsdk-apps/blob/master/components/scripts/python/ccsdk_netconf/netconfclient.py