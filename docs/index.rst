.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

CONTROLLER DESIGN STUDIO (CDS) 
------------------------------
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

MicroServices:
==============
.. toctree::
   :maxdepth: 1
   :glob:
    
	controllerBlueprintStudioProcessor
	bluePrintsProcessor

Architecture:
=============
The Controller Design Studio is composed of two major components: 
	* The GUI (or frontend)
	* The Run Time (or backend)  
The GUI handles direct user input and allows for displaying both design time and run time activities.  For design time, it allows for the creation of controller blueprint, from selecting the DGs to be included, to incorporating the artifact templates, to adding necessary components.  For run time, it allows the user to direct the system to resolve the unresolved elements of the controller blueprint and download the resulting configuration into a VNF.  At a more basic level, it allows for creation of data dictionaries, capabilities catalogs, and controller blueprint, the basic elements that are used to generate a configuration. The essential function of the Controller Design Studio is to create and populate a controller blueprint, create a configuration file from this Controller blueprint, and download this configuration file (configlet) to a VNF/PNF.

Resource assignment:
=====================
.. toctree::
   :maxdepth: 1
   :glob:
   
   resourceassignment

ResolutionHelper:
=================
When executing a component executor script, designer might want to perform resource resolution along with template meshing directly from the script itself.

The helper can be find here: https://github.com/onap/ccsdk-apps/blob/master/components/scripts/python/ccsdk_netconf/common.py