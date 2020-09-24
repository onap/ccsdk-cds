.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

Dbsystemcode
============
.. code-block:: json
   :linenos:

   {
     "dsl_definitions": {
       "dynamic-db-source": {
         "type": "maria-db",
         "url": "jdbc:mysql://localhost:3306/sdnctl",
         "username": "<username>",
         "password": "<password>"
       }
     }
   }
