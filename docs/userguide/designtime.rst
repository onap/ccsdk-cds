.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

Design Time Tools Guide
=======================

Below are the requirements to enable automation for a service within ONAP.

For instantiation, the goal is to be able to automatically resolve all the HEAT/Helm variables, called cloud parameters.

For post-instantiation, the goal is to configure the VNF with initial configuration.

Prerequisite
------------

* Gather the cloud parameters:

Instantiation:
~~~~~~~~~~~~~~

Have the HEAT template along with the HEAT environment file (or) Have the Helm chart along with the Values.yaml file

(CDS supports, but whether SO → Multicloud support for Helm/K8S is different story)


Post-instantiation:
~~~~~~~~~~~~~~~~~~~

Have the configuration template to apply on the VNF.

* XML for NETCONF
* JSON / XML for RESTCONF
* not supported yet - CLI
* JSON for Ansible [not supported yet]
* Identify which template parameters are static and dynamic
* Create and fill-in the a table for all the dynamic values

While doing so, identify the resources using the same process to be resolved; for instance, if two IPs has to be resolved through the same IPAM, the process the resolve the IP is the same.


Services:
---------

.. toctree::
   :maxdepth: 1

   ../CBA/index
   ../resourcedefinition/index
   resourceassignment
