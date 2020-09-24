.. This work is a derivative of https://wiki.onap.org/display/DW/Modeling+Concepts#Concepts-1256902502
.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2020 Deutsche Telekom AG.

.. _template:

Template
--------

A template is an **artifact**, and uses artifact-mapping-resource (see :ref:`artifact_type` -> Mapping)
and artifact-template-velocity (see :ref:`artifact_type` -> Velocity).

A template is **parameterized** and each parameter must be defined in a corresponding **mapping file**.

In order to know which mapping correlates to which template, the file name must start with an ``artifact-prefix``,
serving as identifier to the overall template + mapping.

The **requirement** is as follows:

``${artifact-prefix}-template``

``${artifact-prefix}-mapping``