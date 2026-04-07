.. This work is licensed under a Creative Commons Attribution 4.0
   International License.
.. http://creativecommons.org/licenses/by/4.0
.. (c) ONAP Project and its contributors
.. _release_notes:

******************
CDS Release Notes
******************


Abstract
========

This document provides the release notes for the 1.10.0 release of the
Controller Design Studio (CDS).

Summary
=======

The CDS 1.10.0 release adds OpenTelemetry instrumentation across all services,
improves CBA lifecycle management in the blueprintsprocessor, and delivers
extensive CDS Designer UI enhancements and bug fixes.


Release Data
============

+-------------------------+-------------------------------------------+
| **Project**             | CCSDK-CDS                                 |
|                         |                                           |
+-------------------------+-------------------------------------------+
| **Docker images**       | See :ref:`cds_dockercontainers` section   |
+-------------------------+-------------------------------------------+
| **Release designation** | 1.10.0                                    |
|                         |                                           |
+-------------------------+-------------------------------------------+


New features
------------

* **OpenTelemetry instrumentation** — Added OpenTelemetry (OTel) Java agent for
  Java/Kotlin services (blueprintsprocessor, sdclistener) and Python
  instrumentation for py-executor and command-executor.  Tracing configuration
  is provided through the Helm chart.
  (Jira `CCSDK-4150 <https://jira.onap.org/browse/CCSDK-4150>`_)

* **CBA lifecycle management improvements** — The blueprintsprocessor now
  manages and updates the local CBA copy during workflow execution,
  prevents workflow execution when a CBA is absent from the database,
  cleans up stale local copies, and creates an ID file for deployed
  blueprints.
  (Jira `CCSDK-4141 <https://jira.onap.org/browse/CCSDK-4141>`_)

* **REST client response header capture** — Added support for capturing
  response headers in ``WebClientResponse`` within the ``RestClient``
  implementation.
  (Jira `CCSDK-4141 <https://jira.onap.org/browse/CCSDK-4141>`_)

* **Workflow execution page** — Added a new workflow execution page to the
  CDS Designer UI.

* **Deployed tab** — Added a dedicated deployed-packages tab to the CDS
  Designer UI.

**CDS Designer UI improvements**

* Designer mode improvements (two iterations) including better layout and
  interaction patterns.
* Fixed view-function-source button in designer mode.
* Supported saving blueprints without workflows.
* Made package description optional during package creation.
* Disabled the deploy button until all required fields are populated.
* Fixed inability to save blueprints.
* Fixed the download button in resource dictionary creation.
* Prevented saving a dictionary entry with insufficient data.
* Fixed the broken resource dictionary page.
* Fixed actions-pane responsivity issues.
* Fixed screen-reader and colour-contrast accessibility issues.
* Fixed TypeScript build errors.
* Added README files for each cds-ui sub-project.

**Bug fixes**

* Fixed tracing configuration for py-executor.
* Fixed conflicting Maven dependencies.
* Fixed Spring Boot migration issues in the blueprintsprocessor parent POM.
* Re-enabled previously disabled tests.

**Known Issues**

There are no known outstanding issues for CDS 1.10.0.


Deliverables
------------

Software Deliverables
~~~~~~~~~~~~~~~~~~~~~

.. _cds_dockercontainers:

Docker Containers
`````````````````

The following table lists the docker containers comprising the CDS 1.10.0
release.  Each of these is available on the ONAP nexus3 site
(https://nexus3.onap.org) and can be downloaded with the following command::

   docker pull nexus3.onap.org:10001/{image-name}:{version}


+-------------------------------+----------------------------------------------+---------+
| Image name                    | Description                                  | Version |
+===============================+==============================================+=========+
| onap/ccsdk-blueprintsprocessor| Blueprint processor runtime                  | 1.10.0  |
+-------------------------------+----------------------------------------------+---------+
| onap/ccsdk-commandexecutor    | Command executor                             | 1.10.0  |
+-------------------------------+----------------------------------------------+---------+
| onap/ccsdk-py-executor        | Python script executor                       | 1.10.0  |
+-------------------------------+----------------------------------------------+---------+
| onap/ccsdk-sdclistener        | SDC distribution listener                    | 1.10.0  |
+-------------------------------+----------------------------------------------+---------+
| onap/ccsdk-cds-ui-server      | CDS Designer UI server                       | 1.10.0  |
+-------------------------------+----------------------------------------------+---------+


Documentation Deliverables
~~~~~~~~~~~~~~~~~~~~~~~~~~

* Replaced ``blockdiag`` / ``seqdiag`` diagrams with Mermaid.
* Modernised the Sphinx docs build configuration.
* Fixed broken image links in the CDS designer guide.
* Removed the broken ``sphinxcontrib-swaggerdoc`` module.

Build and Dependency Management
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* Consolidated Maven dependency versions at the CDS aggregator POM level.
* Cleaned up unused and duplicate dependencies.
* Improved Python dependency management for py-executor and command-executor.
* Adjusted the ``artifactId`` in the root POM for SonarQube reporting.
* Added Dependabot configuration for automated dependency updates.


Known Limitations, Issues and Workarounds
=========================================

System Limitations
------------------

No system limitations noted.


Known Vulnerabilities
---------------------

Any known vulnerabilities for ONAP are tracked in the `ONAP Jira`_.


Workarounds
-----------

Not applicable.


Security Notes
--------------

Known Security Issues
~~~~~~~~~~~~~~~~~~~~~

There are no known outstanding security issues related to CDS 1.10.0.


Test Results
============

* Added Playwright end-to-end tests for cds-ui.
* Increased py-executor unit-test coverage.
* Increased command-executor unit-test coverage.


References
==========

For more information on the ONAP CDS project, please see:

#. `ONAP Home Page`_
#. `ONAP Documentation`_
#. `ONAP Release Downloads`_
#. `ONAP Wiki Page`_


.. _`ONAP Home Page`: https://www.onap.org
.. _`ONAP Wiki Page`: https://wiki.onap.org
.. _`ONAP Documentation`: https://docs.onap.org
.. _`ONAP Release Downloads`: https://git.onap.org
.. _`ONAP Jira`: https://jira.onap.org
