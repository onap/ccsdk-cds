.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

.. _hello_world_script_executor_cba:

How to create a “Hello World” Package with CDS Designer UI? The Script Executor Type
====================================================================================

.. toctree::
   :maxdepth: 2

.. note::
    **How to Get Started with CDS Designer UI**

    If you’re new to CDS Designer UI and need to get set up, the following guides may be helpful:

    -  :ref:`running_cds_ui_locally`
    -  :ref:`cds_designer_guide`

.. note::
    **NOTE:**

    In order to see the latest version described below in the tutorial, we will need to use the latest cds-ui-server docker image:
    nexus3.onap.org:10001/onap/ccsdk-cds-ui-server:1.1.0-STAGING-latest


Create New CBA Package
~~~~~~~~~~~~~~~~~~~~~~

In the Package List, click on the **Create Package** button.

|image1|

Define Package MetaData
~~~~~~~~~~~~~~~~~~~~~~~

In METADATA Tab:

1. Package name (Required), type **"Hello-world-package-kotlin"**
2. Package version (Required), type **"1.0.0"**
3. Package description (Required), type **"just description"**
4. Package Tags (Required), type **"kotlin"** then use the **Enter** key on the keyboard
5. In the Custom Key section, add Key name **"template_type"** and
6. For Key Value **"DEFAULT"**

|image2|

Once you enter all fields you will be able to save your package. Click on the **Save** button and continue to define your package.

|image3|

Define Scripts
~~~~~~~~~~~~~~

In the SCRIPTS Tab:

1. Click on the **Create Script** button

|image4|

In the **Create Script File** modal:

|image5|

1. Enter script file name **"Test"**
2. Choose the script type **"Kotlin"**
3. Type or copy and paste the below script in the code editor

.. code-block:: bash

    /*
     \* Copyright © 2020, Orange
     \*
     \* Licensed under the Apache License, Version 2.0 (the "License");
     \* you may not use this file except in compliance with the License.
     \* You may obtain a copy of the License at
     \*
     \* http://www.apache.org/licenses/LICENSE-2.0
     \*
     \* Unless required by applicable law or agreed to in writing, software
     \* distributed under the License is distributed on an "AS IS" BASIS,
     \* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     \* See the License for the specific language governing permissions and
     \* limitations under the License.
    */

    package org.onap.ccsdk.cds.blueprintsprocessor.services.execution.scripts

    import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
    import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
    import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentRemoteScriptExecutor
    import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
    import org.slf4j.LoggerFactory

    open class HelloWorld : AbstractScriptComponentFunction() {
        private val log = LoggerFactory.getLogger(HelloWorld::class.java)!!

        override fun getName(): String {
             return "Check"
        }

        override suspend fun processNB(executionRequest: ExecutionServiceInput) {
              log.info("executing hello world script ")
              val username = getDynamicProperties("username").asText()
              log.info("username : $username")
              //executionRequest.payload.put("Action1-response","hello from $username")
              setAttribute("response-data", "Hello, $username".asJsonPrimitive())
        }

        override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
               log.info("Executing Recovery")
               bluePrintRuntimeService.getBluePrintError().addError("${runtimeException.message}")
         }
    }

4. Click on the **Create Script** button to save the script file

|image6|

Now, you can view and edit your script file.

|image7|

After the new script is added to the **scripts list**, click on the **Save** button to save the package updates.

|image8|

Define DSL Properties
~~~~~~~~~~~~~~~~~~~~~

In the DSL PROPERTIES Tab:

1. Copy and paste the below DSL definition

.. code-block::

    {
        "Action1-properties": {
            "username": {
                "get_input": "username"
            }
        }
    }

|image9|

Then click on the **Save** button to update the package.

|image10|

Create An Action
~~~~~~~~~~~~~~~~~

From the Package information box on top, click on the **Designer Mode** button.

|image11|

Click on the **Skip to Designer Canvas** button to go directly to Designer Mode.

|image12|

Now the designer has zero action added. Let's start adding the first Action.

|image13|

Go to the left side of the designer screen and in the **ACTIONS tab**, click on the **+ New Action** button.

|image14|

Now, the first Action **Action1** is added to the **Actions list** and in the **Workflow canvas**.

|image15|

Add Script Executor Function To The Action
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

On the left side of the designer screen, Click on the **FUNCTIONS tab** to view all the **Functions List.**

|image16|

**Drag** the function type **"component-script-executor"**

|image17|

**Drop** the function to the **"Action1"** Action container.

|image18|

Define Action Attributes
~~~~~~~~~~~~~~~~~~~~~~~~

Click on **Action1** from the ACTIONS tab to open **the ACTION ATTRIBUTES** section on designer screens’ right side.

|image19|

Let's customize the first action's attribute by click on the **+ Create Custom** button to open **Add Custom Attributes** modal window.

|image20|

In the **Add Custom Attributes** **Window**, and the **INPUTS tab** starts to add the first input attribute for **Action1**.
**INPUTS Tab:** Enter the required properties for the inputs’ attribute:

1. Name: **"username"**
2. Type: **"Other"**
3. Attribute type name:  **"dt-resource-assignment-properties"**
4. Required: **"True"**

|image21|

After you add the **username** input's attribute, click on In the OUTPUT Tab to create the output attribute too.

|image22|

**OUTPUTS Tab:** Enter the required properties for the output’ attribute:

1. Name: **"hello-world-output"**
2. Required: **"True"**
3. Type: **"Other"**
4. Type name: **"json"**
5. Value (get_attribute): From the **Functions list**, select **"component-script-executor"** that will show all attributes included in this function
6. Select parameter name **"response-data"**
7. Click on the **Submit Attributes** button to add input and output attributes to **Actions' Attributes list**
8. Click on the **Close** button to close the modal window and go back to the designer screen.

|image23|

Now, you can see all the added attributes listed in the **ACTION ATTRIBUTES** area.

|image24|

Define Function Attributes
~~~~~~~~~~~~~~~~~~~~~~~~~~

From **ACTIONS** List, Click on the function nam **"component-script-executor"**.

|image25|

When you click on the **component-script-executor** function, the **FUNCTION ATTRIBUTES** section will be open on the right side of the designers' screen.
Now, you need to add the values of **Inputs** required attributes in **the Interfaces** **section**.

|image26|

1. **script-type:** **"kotlin"**
2. **script-class-reference: "org.onap.ccsdk.cds.blueprintsprocessor.services.execution.scripts.HelloWorld"**
3. Add optional attribute by click on **Add Optional Attributes** button, add **"dynamic-properties"** then enter the value **"*Action1-properties"**

|image27|

Click on the **Save** button to update the package with the function attributes.

|image28|

From the page header and inside **the Save** **menu**, click on the **Save** button to save all the changes.

|image29|

Enrich And Deploy The CBA Package
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

From the page header and inside the **Save menu**, click on the **Enrich & Deploy** button.

|image30|

Once the process is done, a confirmation message will appear.

|image31|

Test The CBA package With CDS REST API
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To test the CDS hello_world package we created, we can use the REST API shown below to run the **script executor** workflow in the **Hello-world-package-kotlin** package, which will resolve the value of the "username" resource from the REST Call input, and will send it back to the user in the form of "Hello, $username!".

**CURL Request to RUN CBA Package**

.. code-block:: bash

     curl --location --request POST 'http://10.1.1.9:8080/api/v1/execution-service/process' \
     --header 'Content-Type: application/json;charset=UTF-8' \
     --header 'Accept: application/json;charset=UTF-8,application/json' \
     --header 'Authorization: Basic Y2NzZGthcHBzOmNjc2RrYXBwcw==' \
     --header 'Host: cds-blueprints-processor-http:8080' \
     --header 'Cookie: JSESSIONID=7E69BC3F752FD5A3D7D1663FE583ED71' \
     --data-raw '{
                    "actionIdentifiers": {
                        "mode": "sync",
                        "blueprintName": "Hello-world-package-kotlin",
                        "blueprintVersion": "1.0.0",
                        "actionName": "Action1"
                    },
                    "payload": {
                        "Action1-request": {
                            "username":"Orange Egypt"
                        }
                    },
                    "commonHeader": {
                        "subRequestId": "143748f9-3cd5-4910-81c9-a4601ff2ea58",
                        "requestId": "e5eb1f1e-3386-435d-b290-d49d8af8db4c",
                        "originatorId": "SDNC_DG"
                    }
                 }'


**CDS Response showing result of running package**

.. code-block:: bash

    200 OK
        {
            "correlationUUID": null,
            "commonHeader": {
                "timestamp": "2021-01-12T13:22:26.518Z",
                "originatorId": "SDNC_DG",
                "requestId": "e5eb1f1e-3386-435d-b290-d49d8af8db4c",
                "subRequestId": "143748f9-3cd5-4910-81c9-a4601ff2ea58",
                "flags": null
            },
            "actionIdentifiers": {
                "blueprintName": "Hello-world-package-kotlin",
                "blueprintVersion": "1.0.0",
                "actionName": "Action1",
                "mode": "sync"
            },
            "status": {
                "code": 200,
                "eventType": "EVENT_COMPONENT_EXECUTED",
                "timestamp": "2021-01-12T13:22:56.144Z",
                "errorMessage": null,
                "message": "success"
            },
            "payload": {
                "Action1-response": {
                    "hello-world-output": {
                        "hello_world_template": "Hello, Orange Egypt"
                     }
                 }
            }
        }

Screenshot from POSTMAN showing how to run the **Hello_world-package-kotlin** package, and the CDS Response:

|image32|

.. |image1| image:: https://wiki.onap.org/download/attachments/93006316/1.png?version=1&modificationDate=1610364491000&api=v2
   :width: 1000pt
.. |image2| image:: https://wiki.onap.org/download/attachments/93006316/02.png?version=1&modificationDate=1610390913000&api=v2
   :width: 1000pt
.. |image3| image:: https://wiki.onap.org/download/attachments/93006316/03.png?version=1&modificationDate=1610390934000&api=v2
   :width: 1000pt
.. |image4| image:: https://wiki.onap.org/download/attachments/93006316/04.png?version=1&modificationDate=1610391083000&api=v2
   :width: 1000pt
.. |image5| image:: https://wiki.onap.org/download/attachments/93006316/05.png?version=1&modificationDate=1610391137000&api=v2
   :width: 1000pt
.. |image6| image:: https://wiki.onap.org/download/attachments/93006316/06.png?version=1&modificationDate=1610391364000&api=v2
   :width: 1000pt
.. |image7| image:: https://wiki.onap.org/download/attachments/93006316/07.png?version=1&modificationDate=1610391427000&api=v2
   :width: 1000pt
.. |image8| image:: https://wiki.onap.org/download/attachments/93006316/08.png?version=1&modificationDate=1610391642000&api=v2
   :width: 1000pt
.. |image9| image:: https://wiki.onap.org/download/attachments/93006316/09.png?version=1&modificationDate=1610391749000&api=v2
   :width: 1000pt
.. |image10| image:: https://wiki.onap.org/download/attachments/93006316/10.png?version=2&modificationDate=1610391971000&api=v2
   :width: 1000pt
.. |image11| image:: https://wiki.onap.org/download/attachments/84650426/Create%20Package.jpg?version=1&modificationDate=1591034193000&api=v2
   :width: 1000pt
.. |image12| image:: https://wiki.onap.org/download/attachments/93006316/11.png?version=1&modificationDate=1610364492000&api=v2
   :width: 1000pt
.. |image13| image:: https://wiki.onap.org/download/attachments/93006316/12.png?version=2&modificationDate=1610392150000&api=v2
   :width: 300pt
.. |image14| image:: https://wiki.onap.org/download/attachments/93006316/13.png?version=2&modificationDate=1610392171000&api=v2
   :width: 800pt
.. |image15| image:: https://wiki.onap.org/download/attachments/93006316/14.png?version=2&modificationDate=1610392192000&api=v2
   :width: 300pt
.. |image16| image:: https://wiki.onap.org/download/attachments/93006316/15.png?version=2&modificationDate=1610392224000&api=v2
   :width: 800pt
.. |image17| image:: https://wiki.onap.org/download/attachments/93006316/16.png?version=2&modificationDate=1610392392000&api=v2
   :width: 800pt
.. |image18| image:: https://wiki.onap.org/download/attachments/93006316/17.png?version=3&modificationDate=1610392589000&api=v2
   :width: 300pt
.. |image19| image:: https://wiki.onap.org/download/attachments/93006316/18.png?version=2&modificationDate=1610392609000&api=v2
   :width: 300pt
.. |image20| image:: https://wiki.onap.org/download/attachments/93006316/19.png?version=1&modificationDate=1610364492000&api=v2
   :width: 700pt
.. |image21| image:: https://wiki.onap.org/download/attachments/93006316/20.png?version=2&modificationDate=1610392718000&api=v2
   :width: 700pt
.. |image22| image:: https://wiki.onap.org/download/attachments/93006316/21.png?version=2&modificationDate=1610392773000&api=v2
   :width: 800pt
.. |image23| image:: https://wiki.onap.org/download/attachments/93006316/22.png?version=2&modificationDate=1610392886000&api=v2
   :width: 300pt
.. |image24| image:: https://wiki.onap.org/download/attachments/93006316/23.png?version=2&modificationDate=1610392915000&api=v2
   :width: 300pt
.. |image25| image:: https://wiki.onap.org/download/attachments/93006316/24.png?version=2&modificationDate=1610392939000&api=v2
   :width: 300pt
.. |image26| image:: https://wiki.onap.org/download/attachments/93006316/25.png?version=3&modificationDate=1610393699000&api=v2
   :width: 378pt
.. |image27| image:: https://wiki.onap.org/download/attachments/93006316/26.png?version=4&modificationDate=1610393629000&api=v2
   :width: 300pt
.. |image28| image:: https://wiki.onap.org/download/attachments/93006316/26.png?version=4&modificationDate=1610393629000&api=v2
   :width: 1000pt
.. |image29| image:: https://wiki.onap.org/download/attachments/93006316/28.png?version=4&modificationDate=1610394025000&api=v2
   :width: 1000pt
.. |image30| image:: https://wiki.onap.org/download/attachments/93006316/29.png?version=3&modificationDate=1610394097000&api=v2
   :width: 1000pt
.. |image31| image:: https://wiki.onap.org/download/attachments/93006316/29.png?version=3&modificationDate=1610394097000&api=v2
   :width: 1000pt
.. |image32| image:: https://wiki.onap.org/download/attachments/93006316/31.png?version=1&modificationDate=1610459101000&api=v2
   :width: 1000pt