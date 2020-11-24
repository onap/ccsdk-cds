.. This work is a derivative of https://wiki.onap.org/display/DW/Modeling+Concepts#Concepts-384293385
.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2020 Deutsche Telekom AG.

.. _southbound_interface:

Southbound Interfaces
---------------------

CDS comes with native python 3.6 support and Ansible AWX (Ansible Tower):
idea is Network Ops are familiar with Python and/or Ansible, and our goal is not to dictate the SBI to use for
their operations. Ansible and Python provide already many, and well adopted,
SBI libraries, hence they could be utilized as needed.

CDS also provide native support for the following libraries:

* NetConf
* REST
* CLI
* SSH
* gRPC (hence gNMI / gNOI should be supported)

CDS also has extensible REST support, meaning any RESTful interface used for network interaction can be used,
such as external VNFM or EMS.