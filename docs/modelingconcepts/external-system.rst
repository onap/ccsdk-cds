.. This work is a derivative of https://wiki.onap.org/display/DW/Modeling+Concepts#Concepts-2026349199
.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2020 Deutsche Telekom AG.

External Systems support
------------------------

Interaction with **external systems** is made **dynamic** and **plug-able**
removing development cycle to support new endpoint.
In order to share the external system information, TOSCA provides a way to create macros using **dsl_definitions**:
Link to TOSCA spec:
`info 1 <http://docs.oasis-open.org/tosca/TOSCA-Simple-Profile-YAML/v1.2/csd01/TOSCA-Simple-Profile-YAML-v1.2-csd01.html#_Toc494454160>`_,
`info 2 <http://docs.oasis-open.org/tosca/TOSCA-Simple-Profile-YAML/v1.2/csd01/TOSCA-Simple-Profile-YAML-v1.2-csd01.html#_Toc494454173>`_.

Use cases:
* Resource resolution using **REST** (see tab Node Type) or **SQL** (see tab Node Type) external systems
* **gRPC** is supported for remote execution
* Any REST endpoint can be dynamically injected as part of the scripting framework.

Here are some examples on how to populate the system information within the package:

.. list-table::
   :widths: 100
   :header-rows: 1

   * - token-auth
   * - .. code-block:: json

        {
          . . .
          "dsl_definitions": {
          "ipam-1": {
            "type": "token-auth",
            "url": "http://netbox-nginx.netprog:8080",
            "token": "Token 0123456789abcdef0123456789abcdef01234567"
          }
        }

.. list-table::
   :widths: 100
   :header-rows: 1

   * - basic-auth
   * - .. code-block:: json

        {
          . . .
          "dsl_definitions": {
            "ipam-1": {
              "type": "basic-auth",
              "url": "http://localhost:8080",
              "username": "bob",
              "password": "marley"
            }
          }
          . . .
        }

.. list-table::
   :widths: 100
   :header-rows: 1

   * - ssl-basic-auth
   * - .. code-block:: json

        {
          . . .
          "dsl_definitions": {
            "ipam-1": {
              "type" : "ssl-basic-auth",
              "url" : "http://localhost:32778",
              "keyStoreInstance": "JKS or PKCS12",
              "sslTrust": "trusture",
              "sslTrustPassword": "trustore password",
              "sslKey": "keystore",
              "sslKeyPassword: "keystore password"
            }
          }
          . . .
        }

.. list-table::
   :widths: 100
   :header-rows: 1

   * - grpc-executor
   * - .. code-block:: json

        {
          . . .
          "dsl_definitions": {
            "remote-executor": {
              "type": "token-auth",
              "host": "cds-command-executor.netprog",
              "port": "50051",
              "token": "Basic Y2NzZGthcHBzOmNjc2RrYXBwcw=="
            }
          }
          . . .
        }

.. list-table::
   :header-rows: 1

   * - maria-db
   * - .. code-block:: json

        {
          . . .
          "dsl_definitions": {
            "netprog-db": {
              "type": "maria-db",
              "url": "jdbc:mysql://10.195.196.123:32050/netprog",
              "username": "netprog",
              "password": "netprog"
            }
          }
          . . .
        }
