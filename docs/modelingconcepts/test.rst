.. This work is a derivative of https://wiki.onap.org/display/DW/Modeling+Concepts#Concepts-2603186
.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2020 Deutsche Telekom AG.

.. _tests:

Tests
-----

The **tests** folder contains the **uat.yaml** file for execution the cba actions for sunny day and rainy day
scenario using mock data. The process to generate the uat file is documented TBD. The file can be dragged
and drop to the Tests folder after the test for all actions are executed.

NOTE: You need to activate the "uat" Spring Boot profile in order to enable the spy/verify endpoints.
They are disabled by default because the mocks created at runtime can potentially cause collateral problems in production.
You can either pass an option to JVM (``-Dspring.profiles.active=uat``) or set and export an
environment variable (``export spring_profiles_active=uat``).

A quick outline of the UAT generation process follows:

1. Create a minimum :file:`uat.yaml` containing only the NB requests to be sent to the BlueprintsProcessor (BPP) service;
2. Submit the blueprint CBA and this draft :file:`uat.yaml` to BPP in a single HTTP POST call:

   ``curl -u ccsdkapps:ccsdkapps -F cba=@<path to your CBA file> -F uat=@<path to the
   draft uat.yaml> http://localhost:8080/api/v1/uat/spy``
3. If your environment is properly setup, at the end this service will generate the complete :file:`uat.yaml`;
4. Revise the generate file, eventually removing superfluous message fields;
5. Include this file in your CBA under :file:`Tests/uat.yaml`;
6. Submit the candidate CBA + UAT to be validated by BPP, that now will create runtime mocks to simulate
   all SB collaborators, by running:

   ``$ curl -u ccsdkapps:ccsdkapps -F cba=@<path to your CBA file> http://localhost:8080/api/v1/uat/verify``
7. Once validated, your CBA enhanced with its corresponding UAT is eligible
   to be integrated into the CDS project, under the folder :file:`components/model-catalog/blueprint-model/uat-blueprints`.

Reference link for sample generated uat.yaml file for pnf plug & play use case:
`uat.yaml file <https://gerrit.onap.org/r/gitweb?p=ccsdk/cds.git;a=tree;f=components/model-catalog/blueprint-model/uat-blueprints/pnf_config/Tests;h=230d506720c4a1066784c1fe9e0ba0206bbb13cf;hb=refs/heads/master>`_.

As UAT is part of unit testing, it runs in jenkins job
`ccsdk-cds-master-verify-java <https://jenkins.onap.org/job/ccsdk-cds-master-verify-java/>`_
whenever a new commit/patch pushed on gerrit in ccsdk/cds repo.