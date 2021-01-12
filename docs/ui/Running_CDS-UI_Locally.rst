
Running CDS-UI Locally
======================

**Table of Contents**

-  `Prerequisites <#running-cds-ui-locally>`__

-  `Check-out Code <#running-cds-ui-locally>`__

-  `Install Node Modules (UI) <#running-cds-ui-locally>`__

-  `Install Node Modules (Server) <#running-cds-ui-locally>`__

-  `Run UI in Development Mode <#running-cds-ui-locally>`__

-  `Run UI Server <#running-cds-ui-locally>`__

-  `Build UI Docker Image <#running-cds-ui-locally>`__

-  `Run UI Docker Image <#running-cds-ui-locally>`__

.. note::

   **How to Get Started with CDS Designer UI**


   If you’re new to CDS Designer UI and need to get set up, the following guides may be helpful:

   -  `Running CDS-UI Locally <https://wiki.onap.org/display/DW/Running+CDS-UI+Locally>`__

   -  `CDS Designer UI <https://wiki.onap.org/display/DW/CDS+Designer+Guide>`__

   -  `How to create a “Hello World” Package with CDS Designer UI? The Resource Resolution Type <https://wiki.onap.org/pages/viewpage.action?pageId=93003036>`__

   -  `How to create a “Hello World” Package with CDS Designer UI? The Script Executor Type <https://wiki.onap.org/pages/viewpage.action?pageId=93006316>`__

Prerequisites
-------------

Node version: >= 8.9

NPM version: >=6.4.1

Check-out code
--------------

.. code-block:: bash

     git clone "https://gerrit.onap.org/r/ccsdk/cds"

Install Node Modules (UI)
-------------------------

From cds-ui/client directory, execute **npm install** to fetch project
dependent Node modules 

Install Node Modules (Server)
-----------------------------

From cds-ui/server directory, execute **npm install** to fetch project
dependent Node modules 

Run UI in Development Mode
--------------------------

From cds-ui/client directory, execute **npm start** to run the Angular
Live Development Server

.. code-block:: bash

      nirvanr01-mac:client nirvanr$ npm start

      > cds-ui@0.0.0 start /Users/nirvanr/dev/git/onap/ccsdk/cds/cds-ui/client

      > ng serve

      ** Angular Live Development Server is listening on localhost:4200, open your browser on http://localhost:4200/ **

Run UI Server
-------------

From cds-ui/client directory, execute **mvn clean compile** then **npm run build** to copy all front-end artifacts to server/public directory

.. code-block:: bash

   nirvanr01-mac:client nirvanr$ npm run build

   > cds-ui@0.0.0 build /Users/nirvanr/dev/git/onap/ccsdk/cds/cds-ui/client

   > ng build

From cds-ui/server directory, execute **npm run start** to build and start the front-end server

.. code-block:: bash

   nirvanr01-mac:server nirvanr$ npm run start

   > cds-ui-server@1.0.0 prestart /Users/nirvanr/dev/git/onap/ccsdk/cds/cds-ui/server

   > npm run build

   > cds-ui-server@1.0.0 build /Users/nirvanr/dev/git/onap/ccsdk/cds/cds-ui/server

   > lb-tsc es2017 --outDir dist

   > cds-ui-server@1.0.0 start /Users/nirvanr/dev/git/onap/ccsdk/cds/cds-ui/server

   > node .

   Server is running at http://127.0.0.1:3000

   Try http://127.0.0.1:3000/ping

Build UI Docker Image
---------------------

From cds-ui/server directory, execute docker **build -t cds-ui .** to build a local CDS-UI Docker image

.. code-block:: bash

   nirvanr01-mac:server nirvanr$ docker build -t cds-ui .

   Sending build context to Docker daemon 96.73MB

   Step 1/11 : FROM node:10-slim

   ---> 914bfdbef6aa

   Step 2/11 : USER node

   ---> Using cache

   ---> 04d66cc13b46

   Step 3/11 : RUN mkdir -p /home/node/app

   ---> Using cache

   ---> c9a44902da43

   Step 4/11 : WORKDIR /home/node/app

   ---> Using cache

   ---> effb2329a39e

   Step 5/11 : COPY --chown=node package*.json ./

   ---> Using cache

   ---> 4ad01897490e

   Step 6/11 : RUN npm install

   ---> Using cache

   ---> 3ee8149b17e2

   Step 7/11 : COPY --chown=node . .

   ---> e1c72f6caa15

   Step 8/11 : RUN npm run build

   ---> Running in 5ec69a1961d0

   > cds-ui-server@1.0.0 build /home/node/app

   > lb-tsc es2017 --outDir dist

   Removing intermediate container 5ec69a1961d0

   ---> ec9fb899e52c

   Step 9/11 : ENV HOST=0.0.0.0 PORT=3000

   ---> Running in 19963303a09c

   Removing intermediate container 19963303a09c

   ---> 6b3b45709e27

   Step 10/11 : EXPOSE ${PORT}

   ---> Running in 78b9833c5050

   Removing intermediate container 78b9833c5050

   ---> 3835c14ad17b

   Step 11/11 : CMD [ "node", "." ]

   ---> Running in 79a98e6242dd

   Removing intermediate container 79a98e6242dd

   ---> c41f6e6ba4de

   Successfully built c41f6e6ba4de

   Successfully tagged cds-ui:latest

Run UI Docker Image
-------------------

Create **docker-compose.yaml** as below. 

Note:

-  Replace <ip> with host/port where controller & processor mS are running.

.. code-block:: bash

   version: '3.3'

   services:

        cds-ui:

            image: cds-ui:latest

            container_name: cds-ui

            ports:

            - "3000:3000"

            restart: always

            environment:

            - HOST=0.0.0.0

            - API_BLUEPRINT_CONTROLLER_HTTP_BASE_URL=http://<ip>:8080/api/v1

            - API_BLUEPRINT_CONTROLLER_HTTP_AUTH_TOKEN=Basic Y2NzZGthcHBzOmNjc2RrYXBwcw==

            - API_BLUEPRINT_PROCESSOR_HTTP_BASE_URL=http://<ip>:8081/api/v1

            - API_BLUEPRINT_PROCESSOR_HTTP_AUTH_TOKEN=Basic Y2NzZGthcHBzOmNjc2RrYXBwcw==

            - API_BLUEPRINT_PROCESSOR_GRPC_HOST=<IP>

            - API_BLUEPRINT_PROCESSOR_GRPC_PORT=9111

            - API_BLUEPRINT_PROCESSOR_GRPC_AUTH_TOKEN=Basic Y2NzZGthcHBzOmNjc2RrYXBwcw==

Execute **docker-compose up cds-ui**

.. code-block:: bash

   nirvanr01-mac:cds nirvanr$ docker-compose up cds-ui

   Creating cds-ui ... done

   Attaching to cds-ui

   cds-ui         | Server is running at http://127.0.0.1:3000

   cds-ui         | Try http://127.0.0.1:3000/ping

**Next:**\ `CDS Designer
UI <https://wiki.onap.org/display/DW/CDS+Designer+Guide>`__
