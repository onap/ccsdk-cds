.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

Resource Source
---------------

Input:
======
Expects the value to be provided as input to the request.


.. code-block:: json
   :linenos:

   {
     "source-input" :
     {
       "description": "This is Input Resource Source Node Type",
       "version": "1.0.0",
       "properties": {},
       "derived_from": "tosca.nodes.ResourceSource"
     }
   }



Default:
========
Expects the value to be defaulted in the model itself.


.. code-block:: json
   :linenos:

   {
     "source-default" :
     {
       "description": "This is Default Resource Source Node Type",
       "version": "1.0.0",
       "properties": {},
       "derived_from": "tosca.nodes.ResourceSource"
     }
   }


sql:
====

Expects the SQL query to be modeled; that SQL query can be parameterized, and the parameters be other resources resolved through other means. If that's the case, this data dictionary definition will have to define key-dependencies along with input-key-mapping.

CDS is currently deployed along the side of SDNC, hence the primary database connection provided by the framework is to SDNC database.

|image0|

.. |image0| image:: media/sqltable.JPG
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

.. |image1| image:: media/optional.JPG
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

.. |image2| image:: media/capabilitytable.JPG
   :width: 7.88889in
   :height: 4.43750in


.. toctree::
   :maxdepth: 1

   sourcecapabilitycode

Complex Type:
=============

Value will be resolved through REST., and output will be a complex type.

Modeling reference: Modeling Concepts#rest

In this example, we're making a POST request to an IPAM system with no payload.

Some ingredients are required to perform the query, in this case, $prefixId. Hence It is provided as an input-key-mapping and defined as a key-dependencies. Please refer to the modeling guideline for more in depth understanding.

As part of this request, the expected response will be as below.

.. toctree::
   :maxdepth: 1

   complexResponse

What is of interest is the address and id fields. For the process to return these two values, we need to create a custom data-type, as bellow

.. toctree::
   :maxdepth: 1

   dt-netbox-ip

The type of the data dictionary will be dt-netbox-ip.

To tell the resolution framework what is of interest in the response, the output-key-mapping section is used. The process will map the output-key-mapping to the defined data-type.

.. toctree::
   :maxdepth: 1

   create_netbox_ip_address