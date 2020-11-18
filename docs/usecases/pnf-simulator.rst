.. This work is a derivative of https://wiki.onap.org/display/DW/PNF+Simulator+Day-N+config-assign+and+config-deploy+use+case
.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2020 Deutsche Telekom AG.

PNF Simulator Day-N config-assign/deploy
========================================

Overview
~~~~~~~~~~

This use case shows in a very simple way how the day-n configuration is assigned and deployed to a PNF through CDS.
A Netconf server (docker image `sysrepo/sysrepo-netopeer2`) is used for simulating the PNF.

This use case (POC) solely requires a running CDS and the PNF Simulator running on a VM (Ubuntu is used by the author).
No other module of ONAP is needed.

There are different ways to run CDS and the PNF simulator. This guide will show
different possible options to allow the greatest possible flexibility.

Run CDS (Blueprint Processor)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

CDS can be run in Kubernetes (Minikube, Microk8s) or in an IDE. You can choose your favorite option.
Just the blueprint processor of CDS is needed. If you have desktop access it is recommended to run CDS in an IDE since
it is easy and enables debugging.

* CDS in Microk8s: https://wiki.onap.org/display/DW/Running+CDS+on+Microk8s (RDT link to be added)
* CDS in Minikube: https://wiki.onap.org/display/DW/Running+CDS+in+minikube (RDT link to be added)
* CDS in an IDE:  https://docs.onap.org/projects/onap-ccsdk-cds/en/latest/userguide/running-bp-processor-in-ide.html

Run PNF Simulator and install module
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

There are many different ways to run a Netconf Server to simulate the PNF, in this guide `sysrepo/sysrepo-netopeer2`
docker image is commonly used. The easiest way is to run the out-of-the-box docker container without any
other configuration, modules or scripts. In the ONAP community there are other workflows existing for running the
PNF Simulator. These workflows are also using `sysrepo/sysrepo-netopeer2` docker image. These workflow are also linked
here but they are not tested by the author of this guide.

.. tabs::

   .. tab:: sysrepo/sysrepo-netopeer2 (latest)

      Download and run docker container with ``docker run -d --name netopeer2 -p 830:830 -p 6513:6513 sysrepo/sysrepo-netopeer2:latest``

      Enter the container with ``docker exec -it netopeer2 bin/bash``

      Browse to the target location where all YANG modules exist: ``cd /etc/sysrepo/yang``

      Create a simple mock YANG model for a packet generator (:file:`pg.yang`).

      .. code-block:: sh
         :caption: **pg.yang**

         module sample-plugin {

            yang-version 1;
            namespace "urn:opendaylight:params:xml:ns:yang:sample-plugin";
            prefix "sample-plugin";

            description
            "This YANG module defines the generic configuration and
            operational data for sample-plugin in VPP";

            revision "2016-09-18" {
               description "Initial revision of sample-plugin model";
            }

            container sample-plugin {

               uses sample-plugin-params;
               description "Configuration data of sample-plugin in Honeycomb";

               // READ
               // curl -u admin:admin http://localhost:8181/restconf/config/sample-plugin:sample-plugin

               // WRITE
               // curl http://localhost:8181/restconf/operational/sample-plugin:sample-plugin

            }

            grouping sample-plugin-params {
               container pg-streams {
                  list pg-stream {

                     key id;
                     leaf id {
                        type string;
                     }

                     leaf is-enabled {
                        type boolean;
                     }
                  }
               }
            }
         }

      Create the following sample XML data definition for the above model (:file:`pg-data.xml`).
      Later on this will initialise one single PG stream.

      .. code-block:: sh
         :caption: **pg-data.xml**

         <sample-plugin xmlns="urn:opendaylight:params:xml:ns:yang:sample-plugin">
            <pg-streams>
               <pg-stream>
                  <id>1</id>
                  <is-enabled>true</is-enabled>
               </pg-stream>
            </pg-streams>
         </sample-plugin>

      Execute the following command within netopeer docker container to install the pg.yang model

      .. code-block:: sh

         sysrepoctl -v3 -i pg.yang

      .. note::
         This command will just schedule the installation, it will be applied once the server is restarted.

      Stop the container from outside with ``docker stop netopeer2`` and start it again with ``docker start netopeer2``

      Enter the container like it's mentioned above with ``docker exec -it netopeer2 bin/bash``.

      You can check all installed modules with ``sysrepoctl -l``.  `sample-plugin` module should appear with ``I`` flag.

      Execute the following the commands to initialise the Yang model with one pg-stream record.
      We will be using CDS to perform the day-1 and day-2 configuration changes.

      .. code-block:: sh

         netopeer2-cli
         > connect --host localhost --login root
         # passwort is root
         > get --filter-xpath /sample-plugin:*
         # shows existing pg-stream records (empty)
         > edit-config --target running --config=/etc/sysrepo/yang/pg-data.xml
         # initialises Yang model with one pg-stream record
         > get --filter-xpath /sample-plugin:*
         # shows initialised pg-stream

      If the output of the last command is like this, everything went successful:

      .. code-block:: sh

         DATA
         <sample-plugin xmlns="urn:opendaylight:params:xml:ns:yang:sample-plugin">
            <pg-streams>
               <pg-stream>
                  <id>1</id>
                  <is-enabled>true</is-enabled>
               </pg-stream>
            </pg-streams>
         </sample-plugin>


   .. tab:: sysrepo/sysrepo-netopeer2 (legacy)

      Download and run docker container with ``docker run -d --name netopeer2 -p 830:830 -p 6513:6513 sysrepo/sysrepo-netopeer2:legacy``

      Enter the container with ``docker exec -it netopeer2 bin/bash``

      Browse to the target location where all YANG modules exist: ``cd /opt/dev/sysrepo/yang``

      Create a simple mock YANG model for a packet generator (:file:`pg.yang`).

      .. code-block:: sh
         :caption: **pg.yang**

         module sample-plugin {

            yang-version 1;
            namespace "urn:opendaylight:params:xml:ns:yang:sample-plugin";
            prefix "sample-plugin";

            description
            "This YANG module defines the generic configuration and
            operational data for sample-plugin in VPP";

            revision "2016-09-18" {
               description "Initial revision of sample-plugin model";
            }

            container sample-plugin {

               uses sample-plugin-params;
               description "Configuration data of sample-plugin in Honeycomb";

               // READ
               // curl -u admin:admin http://localhost:8181/restconf/config/sample-plugin:sample-plugin

               // WRITE
               // curl http://localhost:8181/restconf/operational/sample-plugin:sample-plugin

            }

            grouping sample-plugin-params {
               container pg-streams {
                  list pg-stream {

                     key id;
                     leaf id {
                        type string;
                     }

                     leaf is-enabled {
                        type boolean;
                     }
                  }
               }
            }
         }

      Create the following sample XML data definition for the above model (:file:`pg-data.xml`).
      Later on this will initialise one single PG (packet-generator) stream.

      .. code-block:: sh
         :caption: **pg-data.xml**

         <sample-plugin xmlns="urn:opendaylight:params:xml:ns:yang:sample-plugin">
            <pg-streams>
               <pg-stream>
                  <id>1</id>
                  <is-enabled>true</is-enabled>
               </pg-stream>
            </pg-streams>
         </sample-plugin>

      Execute the following command within netopeer docker container to install the pg.yang model

      .. code-block:: sh

         sysrepoctl -i -g pg.yang

      You can check all installed modules with ``sysrepoctl -l``. `sample-plugin` module should appear with ``I`` flag.

      In legacy version of `sysrepo/sysrepo-netopeer2` subscribers of a module are required, otherwise they are not
      running and configurations changes are not accepted, see https://github.com/sysrepo/sysrepo/issues/1395. There is
      an predefined application mock up which can be used for that. The usage is described
      here: https://asciinema.org/a/160247. You need to run the following
      commands to start the example application for subscribing to our sample-plugin YANG module.

      .. code-block:: sh

         cd /opt/dev/sysrepo/build/examples
         ./application_example sample-plugin

      Following output should appear:

      .. code-block:: sh

         ========== READING STARTUP CONFIG sample-plugin: ==========

         /sample-plugin:sample-plugin (container)
         /sample-plugin:sample-plugin/pg-streams (container)


         ========== STARTUP CONFIG sample-plugin APPLIED AS RUNNING ==========


      The terminal session needs to be kept open after application has started.

      Open a new terminal and enter the container with ``docker exec -it netopeer2 bin/bash``.
      Execute the following commands in the container to initialise the Yang model with one pg-stream record.
      We will be using CDS to perform the day-1 configuration and day-2 configuration changes.

      .. code-block:: sh

         netopeer2-cli
         > connect --host localhost --login netconf
         # passwort is netconf
         > get --filter-xpath /sample-plugin:*
         # shows existing pg-stream records (empty)
         > edit-config --target running --config=/opt/dev/sysrepo/yang/pg-data.xml
         # initialises Yang model with one pg-stream record
         > get --filter-xpath /sample-plugin:*
         # shows initialised pg-stream

      If the output of the last command is like this, everything went successful:

      .. code-block:: sh

         DATA
         <sample-plugin xmlns="urn:opendaylight:params:xml:ns:yang:sample-plugin">
            <pg-streams>
               <pg-stream>
                  <id>1</id>
                  <is-enabled>true</is-enabled>
               </pg-stream>
            </pg-streams>
         </sample-plugin>

      You can also see that there are additional logs in the subscriber application after editing the configuration of our
      YANG module.

   .. tab:: PNF simulator integration project

      .. warning::
         This method of setting up the PNF simulator is not tested by the author of this guide

      You can refer to `PnP PNF Simulator wiki page <https://wiki.onap.org/display/DW/PnP+PNF+Simulator>`_
      to clone the GIT repo and start the required docker containers. We are interested in the
      `sysrepo/sysrepo-netopeer2` docker container to load a simple YANG similar to vFW Packet Generator.

      Start PNF simulator docker containers. You can consider changing the netopeer image verion to image:
      `sysrepo/sysrepo-netopeer2:iop` in docker-compose.yml file If you find any issues with the default image.

      .. code-block:: sh

         cd $HOME

         git clone https://github.com/onap/integration.git

         Start PNF simulator

         cd ~/integration/test/mocks/pnfsimulator

         ./simulator.sh start

      Verify that you have netopeer docker container are up and running. It will be mapped to host port 830.

      .. code-block:: sh

         docker ps -a | grep netopeer


Config-assign and config-deploy in CDS
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

In the following steps config-assignment is done and the config is deployed to the
Netconf server through CDS. Example requests are in the following  Postman collection
:download:`JSON <media/pnf-simulator.postman_collection.json>`. You can also use bash scripting to call the APIs.

.. note::
   The CBA for this PNF Demo gets loaded, enriched and saved in CDS through calling bootstrap. If not done before, call
   Bootstrap API

Password and username for API calls will be `ccsdkapps`.

**Config-Assign:**

The assumption is that we are using the same host to run PNF NETCONF simulator as well as CDS. You will need the
IP Adress of the Netconf server container which can be found out with
``docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' netopeer2``. In the
following example payloads we will use 172.17.0.2.

Call the `process` API (``http://{{host}}:{{port}}/api/v1/execution-service/process``) with POST method to
create day-1 configuration. Use the following payload:

.. code-block:: JSON

   {
      "actionIdentifiers": {
         "mode": "sync",
         "blueprintName": "pnf_netconf",
         "blueprintVersion": "1.0.0",
         "actionName": "config-assign"
      },
      "payload": {
         "config-assign-request": {
               "resolution-key": "day-1",
               "config-assign-properties": {
                  "stream-count": 5
               }
         }
      },
      "commonHeader": {
         "subRequestId": "143748f9-3cd5-4910-81c9-a4601ff2ea58",
         "requestId": "e5eb1f1e-3386-435d-b290-d49d8af8db4c",
         "originatorId": "SDNC_DG"
      }
   }

You can verify the day-1 NETCONF RPC payload looking into CDS DB. You should see the NETCONF RPC with 5
streams (fw_udp_1 TO fw_udp_5). Connect to the DB and run the below statement. You should
see the day-1 configuration as an output.

.. code-block:: sh

   MariaDB [sdnctl]> select * from TEMPLATE_RESOLUTION where resolution_key='day-1' AND artifact_name='netconfrpc';

   <rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1">
      <edit-config>
         <target>
            <running/>
         </target>
         <config>
            <sample-plugin xmlns="urn:opendaylight:params:xml:ns:yang:sample-plugin">
               <pg-streams>
                  <pg-stream>
                     <id>fw_udp_1</id>
                     <is-enabled>true</is-enabled>
                  </pg-stream>
                  <pg-stream>
                     <id>fw_udp_2</id>
                     <is-enabled>true</is-enabled>
                  </pg-stream>
                  <pg-stream>
                     <id>fw_udp_3</id>
                     <is-enabled>true</is-enabled>
                  </pg-stream>
                  <pg-stream>
                     <id>fw_udp_4</id>
                     <is-enabled>true</is-enabled>
                  </pg-stream>
                  <pg-stream>
                     <id>fw_udp_5</id>
                     <is-enabled>true</is-enabled>
                  </pg-stream>
               </pg-streams>
            </sample-plugin>
         </config>
      </edit-config>
   </rpc>

For creating day-2 configuration call the same endpoint and use the following payload:

.. code-block:: JSON

   {
      "actionIdentifiers": {
         "mode": "sync",
         "blueprintName": "pnf_netconf",
         "blueprintVersion": "1.0.0",
         "actionName": "config-assign"
      },
      "payload": {
         "config-assign-request": {
               "resolution-key": "day-2",
               "config-assign-properties": {
                  "stream-count": 10
               }
         }
      },
      "commonHeader": {
         "subRequestId": "143748f9-3cd5-4910-81c9-a4601ff2ea58",
         "requestId": "e5eb1f1e-3386-435d-b290-d49d8af8db4c",
         "originatorId": "SDNC_DG"
      }
   }


.. note::
   Until this step CDS did not interact with the PNF simulator or device. We just created the day-1 and day-2
   configurations and stored it in CDS database

**Config-Deploy:**

Now we will make the CDS REST API calls to push the day-1 and day-2 configuration changes to the PNF simulator.
Call the same endpoint `process` with the following payload:

.. code-block::  JSON

   {
      "actionIdentifiers": {
         "mode": "sync",
         "blueprintName": "pnf_netconf",
         "blueprintVersion": "1.0.0",
         "actionName": "config-deploy"
      },
      "payload": {
         "config-deploy-request": {
            "resolution-key": "day-1",
               "pnf-ipv4-address": "127.17.0.2",
               "netconf-username": "netconf",
               "netconf-password": "netconf"
         }
      },
      "commonHeader": {
         "subRequestId": "143748f9-3cd5-4910-81c9-a4601ff2ea58",
         "requestId": "e5eb1f1e-3386-435d-b290-d49d8af8db4c",
         "originatorId": "SDNC_DG"
      }
   }

Go back to PNF netopeer cli console like mentioned above and verify if you can see 5 streams fw_udp_1 to fw_udp_5 enabled. If the 5 streams
appear in the output as follows, the day-1 configuration got successfully deployed and the use case is successfully done.

.. code-block:: sh

   > get --filter-xpath /sample-plugin:*
   DATA
   <sample-plugin xmlns="urn:opendaylight:params:xml:ns:yang:sample-plugin">
      <pg-streams>
         <pg-stream>
            <id>1</id>
            <is-enabled>true</is-enabled>
         </pg-stream>
         <pg-stream>
            <id>fw_udp_1</id>
            <is-enabled>true</is-enabled>
         </pg-stream>
         <pg-stream>
            <id>fw_udp_2</id>
            <is-enabled>true</is-enabled>
         </pg-stream>
         <pg-stream>
            <id>fw_udp_3</id>
            <is-enabled>true</is-enabled>
         </pg-stream>
         <pg-stream>
            <id>fw_udp_4</id>
            <is-enabled>true</is-enabled>
         </pg-stream>
         <pg-stream>
            <id>fw_udp_5</id>
            <is-enabled>true</is-enabled>
         </pg-stream>
      </pg-streams>
   </sample-plugin>
   >

The same can be done for day-2 config (follow same steps just with day-2 in payload).

.. note::
   Through deployment we did not deploy the PNF, we just modified the PNF. The PNF could also be installed by CDS
   but this is not targeted in this guide.


Creators of this guide
~~~~~~~~~~~~~~~~~~~~~~~

Deutsche Telekom AG

Jakob Krieg (Rocketchat @jakob.Krieg); Eli Halych (Rocketchat @elihalych)

This guide is a derivate from https://wiki.onap.org/display/DW/PNF+Simulator+Day-N+config-assign+and+config-deploy+use+case.