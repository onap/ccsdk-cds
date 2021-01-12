.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

.. _hello_world_resource_resolution_cba:

How to create a “Hello World” Package with CDS Designer UI? The Resource Resolution Type
========================================================================================

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

In MetaData Tab:

1. Package name (Required), type **"hello_world"**
2. Package version (Required), type **"1.0.0"**
3. Package description (Required), type **"Hello World, the New CBA Package created with CDS Designer UI"**
4. Package Tags (Required), type **"tag1"** then use the **Enter** key on the keyboard

|image2|

Once you enter all fields you will be able to save your package. Click on the **Save** button and continue to define your package.

|image3|

Define Template And Mapping
~~~~~~~~~~~~~~~~~~~~~~~~~~~

In the Template & Mapping Tab:

1. Enter template name **"hello_world_template"**, then go to **Template section**
2. Choose the template type **"Velocity"**
3. Type the Template parameter **"Hello, ${image_name}!"** in the code editor

|image4|

Now, go to the **Manage Mapping section.**

|image5|

Click on the **Use Current Template Instance** button to resolve the value within the template and to auto-map it.

|image6|

Inside the **Mapping table**, change **Dictionary Source** from **default** to **input**

|image7|

Click on the **Finish** button to save the template and close it.

|image8|

After the new template is added to the **Template and Mapping list**, click on the **Save** button to save the package updates.

|image9|

Create An Action
~~~~~~~~~~~~~~~~~

From the Package information box on top, click on the **Designer Mode** button.

|image10|

Click on the **Skip to Designer Canvas** button to go directly to Designer Mode.

|image11|

Now the designer has zero action added. Let's start adding the first Action.

|image12|

Go to the left side of the designer screen and in the **ACTIONS tab**, click on the **+ New Action** button.

|image13|

Now, the first Action **Action1** is added to the **Actions list** and in the **Workflow canvas**.

|image14|

Add Resource Resolution Function To The Action
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

On the left side of the designer screen, Click on the **FUNCTIONS tab** to view all the **Functions List.**

|image15|

**Drag** the function type **"component-resource-resolution"**

|image16|

**Drop** the function to the **"Action1"** Action container.

|image17|

Define Action Attributes
~~~~~~~~~~~~~~~~~~~~~~~~

Click on **Action1** from the ACTIONS tab to open **the ACTION ATTRIBUTES** section on designer screens’ right side.

|image18|

Let's customize the first action's attribute by click on the **+ Create Custom** button to open **Add Custom Attributes** modal window.

|image19|

In the **Add Custom Attributes** **Window**, and the **INPUTS tab** starts to add the first input attribute for **Action1**.

**INPUTS Tab:** Enter the required properties for the inputs’ attribute:

1. Name: **"template-prefix"**
2. Type: **"List"**
3. Required: **"True"**

|image20|

After you add the **template-prefix** input's attribute, click on In the OUTPUT Tab to create the output attribute too.

|image21|

**OUTPUTS Tab:** Enter the required properties for the output’ attribute:

1. Name: **"hello-world-output"**
2. Required: **"True"**
3. Type: **"other"**
4. Type name: **"json"**
5. Value (get_attribute): From the **Functions list**, select **"component-resource-resolution"** that will show all attributes included in this function
6. Select parameter name **"assignment-params"**
7. Click on the **Submit Attributes** button to add input and output attributes to **Actions' Attributes list**
8. Click on the **Close** button to close the modal window and go backto the designer screen.

|image22|

Now, you can see all the added attributes listed in the **ACTION ATTRIBUTES** area.

|image23|

Define Function Attributes
~~~~~~~~~~~~~~~~~~~~~~~~~~

From **ACTIONS** List, Click on the function name **"component-resource-resolution"**.

|image24|

When you click on the **component-resource-resolution** function, the **FUNCTION ATTRIBUTES** section will be open on the right side of the designers' screen.

|image25|

Now, you need to add the values of **Inputs** or **Outputs** required attributes in **the Interfaces** **section**.

-  **artifact-prefix-names**:

1. Click on the **Select Templates** button
2. In the modal window that lists all templates you created, click on the **"hello_world_template"** name
3. Click on the **Add Template** button to insert it in **the Artifacts** section and to close the modal window.

|image26|

|image27|

Now, the **hello_world_template** template is listed inside the **Artifacts section.**

|image28|

Click on the **"Save"** button to save the added template.

|image29|

From the page header and inside **the Save** **menu**, click on the **Save** button to save all the changes.

|image30|

Enrich And Deploy The CBA Package
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

From the page header and inside the **Save menu**, click on the **Enrich & Deploy** button.

|image31|

Once the process is done, a confirmation message will appear.

|image32|

Test The CBA package With CDS REST API
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To test the CDS hello_world package we created, we can use the REST API shown below to run the resource resolution workflow in the hello_wold
package, which will resolve the value of the "image_name" resource from the REST Call input, and will send it back to the user in the form of
"Hello, $image_name!".

**CURL Request to RUN CBA Package**

.. code-block:: bash

     curl --location --request POST
     'http://cds-blueprint-processor:8080/api/v1/execution-service/process'\\
     --header 'Content-Type: application/json;charset=UTF-8'\\
     --header 'Accept: application/json;charset=UTF-8,application/json'\\
     --header 'Authorization: BasicY2NzZGthcHBzOmNjc2RrYXBwcw=='\\
     --data-raw '{
         "actionIdentifiers": {
             "mode": "sync",
             "blueprintName": "hello_world",
             "blueprintVersion": "1.0.0",
             "actionName": "Action1"
         },
         "payload": {
             "Action1-request": {
                  "Action1-properties": {
                      "image_name": "Sarah Abouzainah"
                  }
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
                "timestamp": "2020-12-13T11:43:10.993Z",
                "originatorId": "SDNC_DG",
                "requestId": "e5eb1f1e-3386-435d-b290-d49d8af8db4c",
                "subRequestId": "143748f9-3cd5-4910-81c9-a4601ff2ea58",
                "flags": null
            },
            "actionIdentifiers": {
                "blueprintName": "hello_world",
                "blueprintVersion": "1.0.0",
                "actionName": "Action1",
                "mode": "sync"
            },
            "status": {
                "code": 200,
                "eventType": "EVENT_COMPONENT_EXECUTED",
                "timestamp": "2020-12-13T11:43:11.028Z",
                "errorMessage": null,
                "message": "success"
            },
            "payload": {
                "Action1-response": {
                    "hello-world-output": {
                        "hello_world_template": "Hello, Sarah Abouzainah!"
                     }
                 }
            }
      }

Screenshot from POSTMAN showing how to run the hello_world package, and the CDS Response:

|image33|

Next:
-----

    :ref:`Script Executor Type Hello World CBA Package <hello_world_script_executor_cba>`


.. |image1| image:: https://wiki.onap.org/download/attachments/93003036/1.png?version=4&modificationDate=1607534831000&api=v2
   :width: 1000pt
.. |image2| image:: https://wiki.onap.org/download/attachments/93003036/2.png?version=5&modificationDate=1609170583000&api=v2
   :width: 1000pt
.. |image3| image:: https://wiki.onap.org/download/attachments/93003036/3.png?version=4&modificationDate=1609170695000&api=v2
   :width: 1000pt
.. |image4| image:: https://wiki.onap.org/download/attachments/93003036/4.png?version=3&modificationDate=1609170995000&api=v2
   :width: 1000pt
.. |image5| image:: https://wiki.onap.org/download/attachments/93003036/5.png?version=3&modificationDate=1607538358000&api=v2
   :width: 1000pt
.. |image6| image:: https://wiki.onap.org/download/attachments/93003036/6.png?version=2&modificationDate=1607538455000&api=v2
   :width: 1000pt
.. |image7| image:: https://wiki.onap.org/download/attachments/93003036/7.png?version=2&modificationDate=1607538653000&api=v2
   :width: 1000pt
.. |image8| image:: https://wiki.onap.org/download/attachments/93003036/8.png?version=3&modificationDate=1609171068000&api=v2
   :width: 1000pt
.. |image9| image:: https://wiki.onap.org/download/attachments/93003036/9.png?version=3&modificationDate=1609171129000&api=v2
   :width: 1000pt
.. |image10| image:: https://wiki.onap.org/download/attachments/93003036/10.png?version=3&modificationDate=1609171172000&api=v2
   :width: 1000pt
.. |image11| image:: https://wiki.onap.org/download/attachments/93003036/11.png?version=2&modificationDate=1607540629000&api=v2
   :width: 1000pt
.. |image12| image:: https://wiki.onap.org/download/attachments/93003036/12.png?version=2&modificationDate=1607540920000&api=v2
   :width: 1000pt
.. |image13| image:: https://wiki.onap.org/download/attachments/93003036/13.png?version=3&modificationDate=1607542672000&api=v2
   :width: 200pt
.. |image14| image:: https://wiki.onap.org/download/attachments/93003036/14.png?version=2&modificationDate=1607541858000&api=v2
   :width: 800pt
.. |image15| image:: https://wiki.onap.org/download/attachments/93003036/15.png?version=2&modificationDate=1607542785000&api=v2
   :width: 300pt
.. |image16| image:: https://wiki.onap.org/download/attachments/93003036/16.png?version=3&modificationDate=1607543088000&api=v2
   :width: 700pt
.. |image17| image:: https://wiki.onap.org/download/attachments/93003036/17.png?version=2&modificationDate=1607543299000&api=v2
   :width: 700pt
.. |image18| image:: https://wiki.onap.org/download/attachments/93003036/18.png?version=2&modificationDate=1607543587000&api=v2
   :width: 300pt
.. |image19| image:: https://wiki.onap.org/download/attachments/93003036/19.png?version=3&modificationDate=1607543849000&api=v2
   :width: 300pt
.. |image20| image:: https://wiki.onap.org/download/attachments/93003036/20.png?version=2&modificationDate=1607544576000&api=v2
   :width: 700pt
.. |image21| image:: https://wiki.onap.org/download/attachments/93003036/21.png?version=2&modificationDate=1607544745000&api=v2
   :width: 700pt
.. |image22| image:: https://wiki.onap.org/download/attachments/93003036/22.png?version=2&modificationDate=1607545959000&api=v2
   :width: 800pt
.. |image23| image:: https://wiki.onap.org/download/attachments/93003036/23.png?version=2&modificationDate=1607546223000&api=v2
   :width: 300pt
.. |image24| image:: https://wiki.onap.org/download/attachments/93003036/24.png?version=2&modificationDate=1607548321000&api=v2
   :width: 300pt
.. |image25| image:: https://wiki.onap.org/download/attachments/93003036/25.png?version=2&modificationDate=1607550168000&api=v2
   :width: 300pt
.. |image26| image:: https://wiki.onap.org/download/attachments/93003036/26.png?version=2&modificationDate=1607551324000&api=v2
   :width: 340pt
.. |image27| image:: https://wiki.onap.org/download/attachments/93003036/27.png?version=3&modificationDate=1607551567000&api=v2
   :width: 800pt
.. |image28| image:: https://wiki.onap.org/download/attachments/93003036/28.png?version=2&modificationDate=1607551732000&api=v2
   :width: 300pt
.. |image29| image:: https://wiki.onap.org/download/attachments/93003036/29.png?version=3&modificationDate=1607553177000&api=v2
   :width: 300pt
.. |image30| image:: https://wiki.onap.org/download/attachments/93003036/30.png?version=2&modificationDate=1607552712000&api=v2
   :width: 1000pt
.. |image31| image:: https://wiki.onap.org/download/attachments/93003036/32.png?version=3&modificationDate=1607554129000&api=v2
   :width: 1000pt
.. |image32| image:: https://wiki.onap.org/download/attachments/93003036/33.png?version=1&modificationDate=1607554073000&api=v2
   :width: 1000pt
.. |image33| image:: https://wiki.onap.org/download/attachments/93003036/34.png?version=1&modificationDate=1607608398000&api=v2
   :width: 1000pt
