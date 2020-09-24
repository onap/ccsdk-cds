.. This work is a derivative of https://wiki.onap.org/display/DW/Modeling+Concepts
.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2020 Deutsche Telekom AG.

Modeling Concepts
=================

CDS is a framework to automate the **resolution of resources** for
**instantiation** and any **config** provisioning operation, such as
day0, day1 or day2 configuration.

CDS has a both **design time** and **run time** activities; during
design time, **Designer** can **define** what **actions** are required
for a given service, along with anything comprising the action. The
design produce a :ref:`CBA Package<cba>`. Its **content** is driven from a
**catalog** of **reusable data dictionary** and **component**,
delivering a reusable and simplified **self service** experience.

DS modelling is mainly based on `TOSCA
standard, <http://docs.oasis-open.org/tosca/TOSCA-Simple-Profile-YAML/v1.2/csd01/TOSCA-Simple-Profile-YAML-v1.2-csd01.html>`_
using JSON as reprensentation.

Most of the TOSCA modeled entity presented in the bellow documentation
can be found
`here <https://github.com/onap/ccsdk-cds/tree/master/components/model-catalog/definition-type/starter-type>`_.

.. toctree::
   :caption: Table of Contents
   :maxdepth: 1

   cba
   Tosca.Meta <tosca-meta>
   dynamic-payload
   enrichment
   external-system
   expression
   data-dictionary
   data-type
   artifact-type
   node-type
   workflow
   template
   scripts
   southbound-interfaces
   test


