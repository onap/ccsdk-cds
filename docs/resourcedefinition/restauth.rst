.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.


Resource Rest Authentication
----------------------------
.. toctree::
   :maxdepth: 2

token-auth:
~~~~~~~~~~~

.. code-block:: json
   :linenos:

   {
     "dsl_definitions": {
       "dynamic-rest-source": {
         "type" : "token-auth",
         "url" : "http://localhost:32778",
         "token" : "<token>"
       }
     }
   }

basic-auth:
~~~~~~~~~~~

.. code-block:: json
   :linenos:

   {
     "dsl_definitions": {
       "dynamic-rest-source": {
         "type" : "basic-auth",
         "url" : "http://localhost:32778",
         "username" : "<username>",
         "password": "<password>"
       }
     }
   }

ssl-basic-auth:
~~~~~~~~~~~~~~~

.. code-block:: json
   :linenos:

   {
     "dsl_definitions": {
       "dynamic-rest-source": {
         "type" : "ssl-basic-auth",
         "url" : "http://localhost:32778",
         "keyStoreInstance": "JKS or PKCS12",
         "sslTrust": "trusture",
         "sslTrustPassword": "<password>",
         "sslKey": "keystore",
         "sslKeyPassword": "<password>"
       }
     }
   }
