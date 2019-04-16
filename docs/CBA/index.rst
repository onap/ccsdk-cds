.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

Controller Blueprint Archived Designer Tool(CBA)
------------------------------------------------
.. toctree::
   :maxdepth: 1
   
Introduction:
=============
The Controller Blueprint Archived is the overall service design, fully model-driven, package needed to automate the resolution of resources for instantiation and any config provisioning operation, such as day0, day1 or day2 configuration.

The CBA is .zip file, comprised of the following folder structure, the files may vary:

|image0|

.. |image0| image:: media/image0.jpg
   :width: 7.88889in 
   :height: 4.43750in

Architecture:
=============

|image3|

.. |image3| image:: media/CDS_architecture.jpg
   :height: 4.43750in
   :width: 7.88889in
   
Installation:
=============

Building client html and js files
=================================

	* FROM alpine:3.8 as builder

	* RUN apk add --no-cache npm

	* WORKDIR /opt/cds-ui/client/

	* COPY client/package.json /opt/cds-ui/client/

	* RUN npm install

	* COPY client /opt/cds-ui/client/

	* RUN npm run build


Building and creating server
============================

	* FROM alpine:3.8

	* WORKDIR /opt/cds-ui/

	* RUN apk add --no-cache npm

	* COPY server/package.json /opt/cds-ui/

	* RUN npm install

	* COPY server /opt/cds-ui/
	
	* COPY --from=builder /opt/cds-ui/server/public /opt/cds-ui/public

	* RUN npm run build

	* EXPOSE 3000

	* CMD [ "npm", "start" ]
   
Development:
=============

Pre-requiste:
=============
	Visual Studio code editor
	Git bash
	Node.js & npm
	loopback 4 cli
	

Steps
=====
   To compile CDS code:

   1. Make sure your local Maven settings file ($HOME/.m2/settings.xml) contains
    references to the ONAP repositories and OpenDaylight repositories.
   2. git clone https://(LFID)@gerrit.onap.org/r/a/ccsdk/cds
   3. cd cds ; mvn clean install ; cd ..
   4. Open the cds-ui/client code for development
   
Data Flow:
==========
|image1|

.. |image1| image:: media/image1.jpg
   :width: 7.88889in 
   :height: 4.43750in
   
Functional Decomposition:
=========================
|image2|

.. |image2| image:: media/image2.jpg
   :width: 7.88889in 
   :height: 4.43750in
   
Controller design Studio Presentation:
======================================

Details about CDS Architecture and Design detail, Please click the link.
:download:`CDS_Architecture_Design.pptx`