.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

CONTROLLER DESIGN STUDIO DOCUMENTATION REPOSITORY
-------------------------------------------------
.. toctree::
   :maxdepth: 1
   
Introduction:
=============
The system is designed to be self service, which means that users, not just programmers, can reconfigure the software system as needed to meet customer requirements. To accomplish this goal, the system is built around models that provide for real-time changes in how the system operates.  Users merely need to change a model to change how a service operates.
Self service is a completely new way of delivering services.  It removes the dependence on code releases and the delays they cause and puts the control of services into the hands of the service providers.  They can change a model and its parameters and create a new service without writing a single line of code.  
This makes SERVICE PROVIDER(S) more responsive to its customers and able to deliver products that more closely match the needs of its customers.

Design tools:
=============
.. toctree::
   :maxdepth: 1
   :glob:

   CBA/index
   datadictionary/index


Architecture:
=============
The Controller Design Studio is composed of two major components: 
	* The GUI (or frontend)
	* The Run Time (or backend)  
The GUI handles direct user input and allows for displaying both design time and run time activities.  For design time, it allows for the creation of controller blueprint, from selecting the DGs to be included, to incorporating the artifact templates, to adding necessary components.  For run time, it allows the user to direct the system to resolve the unresolved elements of the controller blueprint and download the resulting configuration into a VNF.  At a more basic level, it allows for creation of data dictionaries, capabilities catalogs, and controller blueprint, the basic elements that are used to generate a configuration. The essential function of the Controller Design Studio is to create and populate a controller blueprint, create a configuration file from this Controller blueprint, and download this configuration file (configlet) to a VNF/PNF.

Resource assignment:
=====================
Component executor:
-------------------
Workflow:
---------

A workflow defines an overall action to be taken for the service; it can be composed of a set of sub-actions to execute. Currently, workflows are backed by Directed Graph engine.

A CBA can have as many workflow as needed.

Template:
---------

A template is an artifact.

A template is parameterized and each parameter must be defined in a corresponding mapping file.

In order to know which mapping correlate to which template, the file name must start with an artifact-prefix, serving as identifier to the overall template + mapping.

The requirement is as follow:

${artifact-prefix}-template
${artifact-prefix}-mapping

A template can represent anything, such as device config, payload to interact with 3rd party systems, resource-accumulator template, etc...

Mapping:
--------
Defines the contract of each resource to be resolved. Each placeholder in the template must have a corresponding mapping definition.

A mapping is comprised of:

- name
- required / optional
- type (support complex type)
- dictionary-name
- dictionary-source

dependencies:
-------------

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

ResolutionHelper:
=================
When executing a component executor script, designer might want to perform resource resolution along with template meshing directly from the script itself.

The helper can be find here: https://github.com/onap/ccsdk-apps/blob/master/components/scripts/python/ccsdk_netconf/common.py