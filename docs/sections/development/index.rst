.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

Development
-------------
.. toctree::
   :maxdepth: 1

Pre-requiste:
-------------
	Visual Studio code editor
	Git bash


Steps
-----
   To compile CDS code:

   1. Make sure your local Maven settings file ($HOME/.m2/settings.xml) contains
    references to the ONAP repositories and OpenDaylight repositories.

   2. git clone https://(LFID)@gerrit.onap.org/r/a/ccsdk/cds

   3. cd cds ; mvn clean install ; cd ..

   4. Open the cds-ui/client code for development

Make sure to create branch for local development

