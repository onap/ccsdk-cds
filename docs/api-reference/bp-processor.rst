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

You can find a sample workflow tutorial :ref:`below <workflow-tutorial>` which will show how to use the endpoints
in the right order. This will give you a better understanding of the CDS Blueprint Processor API.

Getting Started
-----------------

If you cant access a running CDS Blueprint Processor yet, you can choose one of the below options to run it. 
Afterwards you can start trying out the API.

* CDS in Microk8s: https://wiki.onap.org/display/DW/Running+CDS+on+Microk8s (RDT link to be added)
* CDS in Minikube: https://wiki.onap.org/display/DW/Running+CDS+in+minikube (RDT link to be added)
* CDS in an IDE:  :ref:`Running BP Processor Microservice in an IDE <running_bp_processor_in_ide>`

Authorization
-----------------

Use Basic authorization with `ccsdkapps` as a username and password, in Header ``Authorization: Basic Y2NzZGthcHBzOmNjc2RrYXBwcw==``.

Download
------------

Here is the automatically created swagger file for CDS Blueprint Processor API:
:download:`cds-bp-processor-api-swagger.json <media/cds-bp-processor-api-swagger.json>`

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



.. _workflow-tutorial:

Workflow Tutorial
------------------

Introduction
~~~~~~~~~~~~~

This section will show a basic workflow how to proceed a CBA. For this we will follow
the :ref:`PNF Simulator use case <pnf_simulator_use_case>` guide. We will use the same CBA but since this CBA is loaded during
bootstrap per default we will first delete it and afterwards manually enrich and save it in CDS.
The referred use case shows how the day-n configuration is assigned and deployed to a PNF through CDS.
You don't necessarily need a netconf server (which will act as an PNF Simulator) running to get a understanding about
this workflow tutorial. Just take care that without a set up netconf server the day-n configuration deployment will fail
in the last step.

Use the Postman Collection from the referred use case to get sample requests for the following steps:
:download:`json <../usecases/media/pnf-simulator.postman_collection.json>`.

The CBA which we are using is downloadable here :download:`zip <media/workflow-tutorial-cba.zip>`. Hint: this CBA is
also included in the CDS source code for bootstrapping.

Set up CDS
~~~~~~~~~~

If not done before, run `Bootrap` request which will call Bootstrap API of CDS (``POST /api/v1/blueprint-model/bootstrap``)
to load all the CDS default model artifacts into CDS. You should get HTTP status 200 for the below command.

Call `Get Blueprints` request to get all blueprint models which are saved in CDS. This will call the ``GET /api/v1/blueprint-model``
endpoint. You will see the blueprint model ``"artifactName": "pnf_netconf"`` which is loaded by calling bootstrap since Guilin release.
Since we manually want to load the CBA delete the desired CBA from CDS first through calling the delete endpoint
``DELETE /api/v1/blueprint-model/name/{name}/version/{version}``. If you call `Get Blueprints` again you can see that the
``pnf_netconf`` CBA is missing now.

Because the CBA contains a custom data dictionary we need to push the custom entries to CDS first through calling `Data Dictionary` request.
Actually the custom entries are also already loaded through bootstrap but just pretend they are not present in CDS so far.

.. note::
   For every data dictionary entry CDS API needs to be called seperately. The postman collection contains a loop to
   go through all custom entries and call data dictionary endpoint seperately. To execute this loop,
   open `Runner` in Postman and run `Data Dictionary` request like it is shown in the picture below.

   |imageDDPostmanRunner|


Enrichment
~~~~~~~~~~~~

Enrich the blueprint through executing the `Enrich Blueprint` request. Take care to provide the CBA file which you
can download here :download:`zip <media/workflow-tutorial-cba.zip>` in the request body. After the request got executed
download the response body like shown in the picture below, this will be your enriched CBA file.

|saveResponseImage|


Deploy/Save the Blueprint
~~~~~~~~~~~~~~~~~~~~~~~~~~

Run `Save Blueprint` request to save/deploy the Blueprint into the CDS database. Take care to provide the enriched
CBA file which you downloaded earlier in the request body.

After that you should see the new model ``"artifactName": "pnf_netconf"`` by calling `Get Blueprints` request.

An alternative would be to use ``POST /api/v1/blueprint-model/publish`` endpoint, which would also validate the CBA.
For doing enrichment and saving the CBA in a single call ``POST /api/v1/blueprint-model/enrichandpublish`` could also be used.

Config-Assign / Config-Deploy
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

From now on you can continue with the :ref:`PNF Simulator use case <pnf_simulator_use_case_config_assign_deploy>` from section
`Config-assign and config-deploy` to finish the workflow tutorial. The provided Postman collection already contains all
the needed requests also for this part so you don't need to create the calls and payloads manually.
Take care that the last step will fail if you don't have a netconf server set up.


.. |imageDDPostmanRunner| image:: media/dd-postman-runner.png
   :width: 500pt

.. |saveResponseImage| image:: media/save-response-postman.png
   :width: 500pt