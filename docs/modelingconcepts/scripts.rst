.. This work is a derivative of https://wiki.onap.org/display/DW/Modeling+Concepts#Concepts-703799064
.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2020 Deutsche Telekom AG.

.. _scripts:

Scripts
-------

Library
+++++++++++++++++

NetconfClient
+++++++++++++++++

In order to facilitate NETCONF interaction within scripts, a python NetconfClient binded to our Kotlin implementation is made available.
This NetconfClient can be used when using the component-netconf-executor.

The client can be find here: https://github.com/onap/ccsdk-cds/blob/master/components/scripts/python/ccsdk_netconf/netconfclient.py

ResolutionHelper
+++++++++++++++++

When executing a component executor script, designer might want to perform
resource resolution along with template meshing directly from the script itself.

The helper can be find here:
https://github.com/onap/ccsdk-cds/blob/master/components/scripts/python/ccsdk_netconf/common.py