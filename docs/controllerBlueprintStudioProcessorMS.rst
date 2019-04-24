.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.
.. Copyright (C) 2019 IBM.

Controller Blueprints Studio Processor
======================================

The Controller Blueprint Archive is the overall service design, fully model-driven, intent based package needed for SELF SERVICE provisioning and configuration management automation.

The CBA is .zip file which is saved in Controller Blueprint Database.

Dynamic API:
===========

The nature of the API request and response is meant to be model driven and dynamic. They both share the same definition.

The actionName, under the actionIdentifiers refers to the name of a Workflow (see workflow)

The content of the payload is what is fully dynamic / model driven.

The first top level element will always be either $actionName-request for a request or $actionName-response for a response.

Then the content within this element is fully based on the workflow input and output.


Enrichment:
===========

Helps to generate complete valid CBA file.

  
   