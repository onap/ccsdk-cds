.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2020 Deutsche Telekom AG.

Blueprint Processor API Reference
==================================

Introduction
--------------

This section shows all resources and endpoints which CDS BP processor currently provides through a swagger file
which is automatically created during CDS build process by Swagger Maven Plugin. A corresponding Postman collection is 
also included. Endpoints can also be described using this template
:download:`api-doc-template.rst <api-doc-template.rst>` but this is not the prefered way to describe the CDS API.

Authentification
-----------------

Use Basic athentification with `ccsdkapps` as a username and password, in Header ``Authorization: Basic Y2NzZGthcHBzOmNjc2RrYXBwcw==``.

Download
------------

Here is the automatically created swagger file for CDS Blueprint Processor API:
:download:`cds-bp-processor-api-swagger.json <media/cds-bp-processor-api-swagger.json>`
:download:`cds-bp-processor-api-swagger.yaml <media/cds-bp-processor-api-swagger.yaml>`

You can find a postman collection including sample requests for all endpoints here: 
:download:`bp-processor.postman_collection.json <media/bp-processor.postman_collection.json>`.
Please keep the Postman Collection up-to-date for new endpoints.

General Setup
--------------

All endpoints are accessable under ``http://{{host}}:{{port}}/api/v1/``. Host and port depends on your CDS BP processor
deployment.


List all endpoints
-------------------

Lists all available endpoints from blueprints processor API.


Request
~~~~~~~~~~

GET ``http://{{host}}:{{port}}/actuator/mappings``
....................................................

Lists all endpoints from blueprints processor.

.. code-block:: curl
   :caption: **request**

   curl --location --request GET 'http://localhost:8081/actuator/mappings' \
   --header 'Authorization: Basic Y2NzZGthcHBzOmNjc2RrYXBwcw=='


Success Response
~~~~~~~~~~~~~~~~~

HTTP Status 202 OK

.. code-block:: json
   :caption: **sample response body**

   {
      "contexts": {
         "application": {
               "mappings": {
                  "dispatcherHandlers": {
                     "webHandler": [

                           ...

                           {
                              "predicate": "{GET /api/v1/blueprint-model, produces [application/json]}",
                              "handler": "org.onap.ccsdk.cds.blueprintsprocessor.designer.api.BlueprintModelController#allBlueprintModel()",
                              "details": {
                                 "handlerMethod": {
                                       "className": "org.onap.ccsdk.cds.blueprintsprocessor.designer.api.BlueprintModelController",
                                       "name": "allBlueprintModel",
                                       "descriptor": "()Ljava/util/List;"
                                 },
                                 "handlerFunction": null,
                                 "requestMappingConditions": {
                                       "consumes": [],
                                       "headers": [],
                                       "methods": [
                                          "GET"
                                       ],
                                       "params": [],
                                       "patterns": [
                                          "/api/v1/blueprint-model"
                                       ],
                                       "produces": [
                                          {
                                             "mediaType": "application/json",
                                             "negated": false
                                          }
                                       ]
                                 }
                              }
                           },
                           {
                              "predicate": "{GET /api/v1/blueprint-model/meta-data/{keyword}, produces [application/json]}",
                              "handler": "org.onap.ccsdk.cds.blueprintsprocessor.designer.api.BlueprintModelController#allBlueprintModelMetaData(String, Continuation)",
                              "details": {
                                 "handlerMethod": {
                                       "className": "org.onap.ccsdk.cds.blueprintsprocessor.designer.api.BlueprintModelController",
                                       "name": "allBlueprintModelMetaData",
                                       "descriptor": "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;"
                                 },
                                 "handlerFunction": null,
                                 "requestMappingConditions": {
                                       "consumes": [],
                                       "headers": [],
                                       "methods": [
                                          "GET"
                                       ],
                                       "params": [],
                                       "patterns": [
                                          "/api/v1/blueprint-model/meta-data/{keyword}"
                                       ],
                                       "produces": [
                                          {
                                             "mediaType": "application/json",
                                             "negated": false
                                          }
                                       ]
                                 }
                              }
                           }

                           ...

                     ]
                  }
               },
               "parentId": null
         }
      }
   }


API Reference
--------------

.. warning::
   In the used Sphinx plugin `sphinxcontrib-swaggerdoc` some information of the swagger file is not
   rendered completely, e.g. the request body. Use your favorite Swagger Editor and paste the swagger file
   to get a complete view of the API reference, e.g. on https://editor.swagger.io/.

.. swaggerv2doc:: media/cds-bp-processor-api-swagger.json