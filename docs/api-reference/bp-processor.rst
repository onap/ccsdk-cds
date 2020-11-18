.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2020 Deutsche Telekom AG.

Blueprint Processor API Reference
==================================

Introduction
--------------

This section shows all resources and endpoints which CDS BP processor currently provides with sample requests/responses,
parameter description and other information. If there is a new API and you want do document it, you can use this template
:download:`rst <api-doc-template.rst>`.

Authentification
-----------------

Use Basic athentification with `ccsdkapps` as a username and password, in Header ``Authorization: Basic Y2NzZGthcHBzOmNjc2RrYXBwcw==``.

Download
------------

You can find a postman collection including sample requests here: :download:`JSON <media/bp-processor.postman_collection.json>`

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

Blueprint Model Catalog API
----------------------------

Blueprint-model resource contains all Controller Blueprints Archive (CBA) packages which are available in CDS.
With blueprint-model API you can manage your CBAs.


List all blueprint models
~~~~~~~~~~~~~~~~~~~~~~~~~~~


GET ``/blueprint-model``
..............................

Lists all blueprint models which are saved in CDS.

Request
...........

.. code-block:: curl
   :caption: **request**

   curl --location --request GET 'http://{{ip_adress}}:{{port}}/api/v1/blueprint-model' \
   --header 'Authorization: Basic Y2NzZGthcHBzOmNjc2RrYXBwcw=='

Produces
...........

``application/json``


Success Response
......................

HTTP Status 200 OK

.. code-block:: json
   :caption: **example response**

   [
    {
        "blueprintModel": {
            "id": "109e725d-5145-4f70-a2e7-ee6640e2fb5f",
            "artifactUUId": null,
            "artifactType": "SDNC_MODEL",
            "artifactVersion": "1.0.0",
            "artifactDescription": "",
            "internalVersion": null,
            "createdDate": "2020-11-09T19:00:20.000Z",
            "artifactName": "vLB_CDS_RESTCONF",
            "published": "Y",
            "updatedBy": "DanielEmmarts>",
            "tags": "vLB-CDS"
        }
    },
    {
        "blueprintModel": {
            "id": "5cce3804-09eb-473d-b513-81f8547a7240",
            "artifactUUId": null,
            "artifactType": "SDNC_MODEL",
            "artifactVersion": "1.0.0",
            "artifactDescription": "",
            "internalVersion": null,
            "createdDate": "2020-11-09T19:00:20.000Z",
            "artifactName": "vLB_CDS",
            "published": "Y",
            "updatedBy": "TomKennedy>",
            "tags": "vLB_CDS"
        }
    }
   ]

Technical Description
...........................

Loads all Blueprint Models which are saved in the CDS database in table `BLUEPRINT_MODEL`. Unpublished and unproceeded
Blueprint Models are also included.
Called class/method: ``org.onap.ccsdk.cds.blueprintsprocessor.designer.api.BlueprintModelController#allBlueprintModel()``.
