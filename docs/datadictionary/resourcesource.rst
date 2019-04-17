.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

Resource Source 
---------------

Input:
======
Expects the value to be provided as input to the request.

source-input:

{
  "description": "This is Input Resource Source Node Type",
  "version": "1.0.0",
  "properties": {},
  "derived_from": "tosca.nodes.ResourceSource"
}


Default:
========
Expects the value to be defaulted in the model itself.

source-default:

{
  "description": "This is Default Resource Source Node Type",
  "version": "1.0.0",
  "properties": {},
  "derived_from": "tosca.nodes.ResourceSource"
}


sql:
====

Expects the SQL query to be modeled; that SQL query can be parameterized, and the parameters be other resources resolved through other means. If that's the case, this data dictionary definition will have to define key-dependencies along with input-key-mapping.

CDS is currently deployed along the side of SDNC, hence the primary database connection provided by the framework is to SDNC database.

|image0|

.. |image0| image:: sqltable.jpg
   :width: 7.88889in 
   :height: 4.43750in

.. toctree::
   :maxdepth: 1  

	sourceprimarydbcode
	
Connection to a specific database can be expressed through the endpoint-selector property, which refers to a macro defining the information about the database the connect to. Understand TOSCA Macro in the context of CDS.

.. toctree::
   :maxdepth: 1 

	dbsystemcode


REST:
=====

Expects the URI along with the VERB and the payload, if needed.

CDS is currently deployed along the side of SDNC, hence the default rest connection provided by the framework is to SDNC MDSAL.

|image1|

.. |image1| image:: resttable.jpg
   :width: 7.88889in 
   :height: 4.43750in

.. toctree::
   :maxdepth: 1
   
   restsourcecode

Connection to a specific REST system can be expressed through the endpoint-selector property, which refers to a macro defining the information about the REST system the connect to. Understand TOSCA Macro in the context of CDS.

Few ways are available to authenticate to the REST system:

	* token-auth
	* basic-auth
	* ssl-basic-auth
	
For source code of Authentication click below link:

.. toctree::
   :maxdepth: 1
   
   restauth

Capability:
===========

Expects a script to be provided.

|image2|

.. |image2| image:: capabilitytable.jpg
   :width: 7.88889in 
   :height: 4.43750in
   
   
.. toctree::
   :maxdepth: 1   

	sourcecapabilitycode
