
.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

.. _cds_designer_guide:

CDS Designer Guide
==================
.. toctree::
   :maxdepth: 4

.. note::
    **How to Get Started with CDS Designer UI**

    If you’re new to CDS Designer UI and need to get set up, the following guides may be helpful:

    -  :ref:`running_cds_ui_locally`

Getting Started
---------------
This is your CDS Designer UI guide. No matter how experienced you are or what you want to achieve,
it should cover everything you need to know — from navigating the interface to making the most of different features.

What is CDS Designer UI?
------------------------
+----------------------------------------------+--------------+
| CDS Designer UI is a framework to automate   |              |
| the **resolution of resources** for          |    |image1|  |
| **instantiation** and any **config**         |              |
| provisioning operation, such as day0, day1,  |              |
| or day2 configuration.                       |              |
|                                              |              |
|                                              |              |
| CDS has both **design-time** and             |              |
| **run-time** activities; during design time, |              |
| **Designer** can **define** what **actions** |              |
| are required for a given service, along with |              |
| anything comprising the action. The design   |              |
| produces a  :ref:`CBA Package <cba>`        |              |
|                                              |              |
|                                              |              |
| Its **content** is driven from a **catalog** |              |
| of **reusable data dictionary** and          |              |
| **component**, delivering a reusable and     |              |
| simplified **self-service** experience.      |              |
|                                              |              |
| CDS modeling is mainly based on **the TOSCA  |              |
| standard**, using JSON as a representation.  |              |
+----------------------------------------------+--------------+

What's new?
-----------
+----------------------+----------------------+----------------------+
| |image2|             | |image3|             | |image4|             |
|                      |                      |                      |
| Create full CBA      | Import old packages  | Create sophisticated |
| packages from        | for edit and         | package workflows in |
| built-in forms       | collaboration        | a no-code graphical  |
| without programming  |                      | designer             |
|                      |                      |                      |
| |image5|             | |image6|             | |image7|             |
|                      |                      |                      |
| Customizable CBA     | Easily create and    | Integration between  |
| Package actions      | manage lists of data | CDS UI and SDC       |
|                      | via interface (Data  | Services             |
|                      | Dictionary,          |                      |
|                      | controller catalog,  |                      |
|                      | and config           |                      |
|                      | management)          |                      |
+----------------------+----------------------+----------------------+

Overview of CDS Interface
-------------------------
Full CDS UI screens are available in `InVision <https://invis.io/PAUI9GLJH3Q>`__

|image8|

1. **CDS main menu:** Access all CDS module list including Packages, Data Dictionary, Controller Catalog, etc.
2. **Profile:** Access user profile information
3. **Module Title:** See the current module name and the total number of items in the module list
4. **Module list:** View all active items in module and tools for search and filtering

CBA Packages
------------

Package List
~~~~~~~~~~~~
It gives you quick access to all and most recent created/edit packages

|image9|

1.  **Module Tabs:** Access All, Deployed, Under Construction, or Archived packages
2.  **Search:** Search for a package by title
3.  **Filter:** Filter packages by package tags
4.  **Package Sort:** Sort packages by recent or alphanumeric (name) or version
5.  **List Pagination:** navigate between package list pages
6.  **Create Package:** Create a new CBA package
7.  **Import Package:** Import other packages that are created previously on CDS Editor or Designer or created by other/current user
8.  **Package box:** It shows a brief detail of the package and gives access to some actions of the package
9.  **Deployed package indicator**
10. **Package name and version**
11. **More menu:** Access a list of actions including Clone, Archive, Download, and Delete
12. **Last modified:** Shows user name and date and time of last modifications made in the package
13. **Package Description**
14. **Package Tags**
15. **Collaborators:** See who's collaborating to edit in the package
16. **Configuration button:** Go directly to package configuration
17. **Designer Mode:** It indicates package mode (Designer, Scripting, and Generic scripting) and by clicking on it, it will load to mode screen

Create a New CBA Package
------------------------

User Flow
~~~~~~~~~

|image10|

Create a New Package
~~~~~~~~~~~~~~~~~~~~
You can create a new CBA Package by creating a new custom package or by import a package file that is already created before.

.. note::
	**Create/Import Package**
	You can’t create/import a CBA package that has the same name and version
	of an existing package. Packages can be in the same name but in
	different version numbers (ex., Package one v1.0.0 & Package one v1.0.1).

**Create a New Custom CBA Package**
From the Packages page, click on the **Create Package** button to navigate to **Package** **Configuration**

|image11|

:ref:`MetaData <tosca_meta>`
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

In **MetaData Tab,** select Package Mode, enter package Name, Version,
Description and other configurations

|image12|

Once you fill in all required inputs, you can save this package by clicking the **Save** button in the Actions menu

|image13|

**Package Info Box:** It is in top of configurations tabs and it appears after you save a package for the first time

|image14|

You can continue adding package configuration or go directly to the **Designer Mode** screen from Package info box

All changes will be saved when you click on the **Save** button

To close the package configuration and go back to the Package list, navigate to the top left in breadcrumb and click the **CBA Packages**
link or click on the **Packages** link in the Main menu.

:ref:`Template & Mapping <template>`
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

You can create as many templates using

artifact-mapping-resource (:ref:`artifact_type` -> Mapping)
or/and
artifact-template-velocity  (:ref:`artifact_type` -> Velocity)

|image15|

1. **Template name**
2. **Template Section:** Where you include template attributes
3. **Manage Mapping:** Here the automapping process occurs to template attributes to refer to the data dictionary that will be used to resolve a particular resource.

**Template Section**

|image16|

1. **Template Type:** Template is defined by one of three templates (Velocity, Jinja, Kotlin)
2. **Import Template Attributes/Parameters:** You can add attributes by Import attribute list file or by
3. **Insert Template Attributes/Parameters Manually:** You can insert Attributes manually in the code editor. Code editor validates attributes according to the pre-selected template type

**Import Template Attributes**

|image17|

After import attributes, you can add/edit/delete attributes in the code editor.

|image18|

**Manage Mapping Section**

|image19|

1. **Use current Template Instance:** You can use attributes from the Template section
2. **Upload Attributes List:** In case you don’t have existing attributes in Template section or have different attributes, you can upload the attributes list

Once you select the source of attributes, you get a confirmation of success fetching.

|image20|

Then the Mapped Table appears to show the Resource Dictionary reference.

|image21|

When you finish the creation process, you must click on **the Finish button (1)**
to submit the template, or you can clear all data by click on **the Clear button** **(2).**

|image22|

:ref:`Scripts <scripts>`
~~~~~~~~~~~~~~~~~~~~~~~~

*Allowed file type: Kotlin(kt), Python(py), Jython, Ansible*

To add script file/s, you have two options:

1. **Create Script**
2. **Import File**

**Enter file URL:** Script file can be stored in server and you can add this script file by copy and paste file URL in URL input then
**press ENTER** key from the keyboard

|image23|

**Create a Script File**

1. **File Name:** Add the script file name
2. **Script Type:** Choose script type (Kotlin, Jython, Ansible)
3. **Script Editor:** Enter the script file content

|image24|

After you type the script, click on the **Create Script** button to save it

|image25|

By adding script file/s, you can:
1. Edit file: You can edit each script file from the code editor
2. Delete file

|image26|

:ref:`Definitions <data_type>`
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To define a data type that represents the **schema** of a specific type of **data**,
you have to **enrich** the package to automatically generate all definition files:

1. **Enrich Package:** from the package details box, click on the Enrich button

|image27|

Once you successfully enrich the package, all definition files will be listed.

|image28|

By definition file/s, you can Delete file

|image29|

:ref:`External System Authentication Properties <external_system>`
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
In order to populate the system information within the package, you have to provide **dsl_definitions**

|image30|

Topology Template
~~~~~~~~~~~~~~~~~
Here you can **manually** add your package:

1. :ref:`Workflow <workflow>` that define an overall action to be taken on the service
2. :ref:`Node/Component template <node_type>` that is used to represent a **functionality** along with its **contracts**, such as **inputs**, **outputs**, and **attributes**

|image31|


Next
----

.. toctree::
   :maxdepth: 2

    hello_world_resource_resolution_cba
    hello_world_script_executor_cba


.. |image1| image:: https://wiki.onap.org/download/attachments/84650426/CDS%20Logo.png?version=4&modificationDate=1591723580000&api=v2
   :width: 200pt
.. |image2| image:: https://wiki.onap.org/download/thumbnails/84650426/Feature%201.png?version=1&modificationDate=1591032224000&api=v2
   :width: 50pt
.. |image3| image:: https://wiki.onap.org/download/thumbnails/84650426/Feature%202.png?version=1&modificationDate=1591032225000&api=v2
   :width: 47pt
.. |image4| image:: https://wiki.onap.org/download/thumbnails/84650426/Feature%203.png?version=1&modificationDate=1591032226000&api=v2
   :width: 47pt
.. |image5| image:: https://wiki.onap.org/download/thumbnails/84650426/Feature%204.png?version=1&modificationDate=1591032227000&api=v2
   :width: 60pt
.. |image6| image:: https://wiki.onap.org/download/thumbnails/84650426/Feature%205.png?version=1&modificationDate=1591032227000&api=v2
   :width: 50pt
.. |image7| image:: https://wiki.onap.org/download/thumbnails/84650426/Feature%206.png?version=1&modificationDate=1591032228000&api=v2
   :width: 30pt
.. |image8| image:: https://wiki.onap.org/download/attachments/84650426/Interface.jpg?version=2&modificationDate=1609155664000&api=v2
   :width: 1000pt
.. |image9| image:: https://wiki.onap.org/download/attachments/84650426/Package%20List.jpg?version=3&modificationDate=1609322704000&api=v2
   :width: 1000pt
.. |image10| image:: https://wiki.onap.org/download/attachments/84650426/Create%20Package%20User%20flow.jpg?version=2&modificationDate=1609156805000&api=v2
   :width: 1000pt
.. |image11| image:: https://wiki.onap.org/download/attachments/84650426/Create%20Package.jpg?version=2&modificationDate=1609157022000&api=v2
   :width: 1000pt
.. |image12| image:: https://wiki.onap.org/download/attachments/84650426/Package%20Configuration%20-%20MetaData.jpg?version=2&modificationDate=1609157275000&api=v2
   :width: 1000pt
.. |image13| image:: https://wiki.onap.org/download/attachments/84650426/Package%20Configuration%20-%20Action%20Menu.jpg?version=2&modificationDate=1609157671000&api=v2
   :width: 1000pt
.. |image14| image:: https://wiki.onap.org/download/attachments/84650426/Package%20Configuration%20-%20Info%20Box.jpg?version=2&modificationDate=1609158391000&api=v2
   :width: 1000pt
.. |image15| image:: https://wiki.onap.org/download/attachments/84650426/Temp%20%26%20Mapp%201.jpg?version=2&modificationDate=1609158797000&api=v2
   :width: 1000pt
.. |image16| image:: https://wiki.onap.org/download/attachments/84650426/Temp%20%26%20Mapp%202.jpg?version=2&modificationDate=1609158980000&api=v2
   :width: 1000pt
.. |image17| image:: https://wiki.onap.org/download/attachments/84650426/Temp%20%26%20Mapp%203.jpg?version=2&modificationDate=1609159240000&api=v2
   :width: 500pt
.. |image18| image:: https://wiki.onap.org/download/attachments/84650426/Temp%20%26%20Mapp%204.jpg?version=1&modificationDate=1609159448000&api=v2
   :width: 1000pt
.. |image19| image:: https://wiki.onap.org/download/attachments/84650426/Temp%20%26%20Mapp%205.jpg?version=1&modificationDate=1609159694000&api=v2
   :width: 1000pt
.. |image20| image:: https://wiki.onap.org/download/attachments/84650426/Temp%20%26%20Mapp%206.jpg?version=2&modificationDate=1609159862000&api=v2
   :width: 1000pt
.. |image21| image:: https://wiki.onap.org/download/attachments/84650426/Temp%20%26%20Mapp%207.jpg?version=3&modificationDate=1609162172000&api=v2
   :width: 1000pt
.. |image22| image:: https://wiki.onap.org/download/attachments/84650426/Temp%20%26%20Mapp%208.jpg?version=2&modificationDate=1609162135000&api=v2
   :width: 1000pt
.. |image23| image:: https://wiki.onap.org/download/attachments/84650426/Scripts%201.jpg?version=2&modificationDate=1609163160000&api=v2
   :width: 1000pt
.. |image24| image:: https://wiki.onap.org/download/attachments/84650426/Scripts%202.jpg?version=2&modificationDate=1609164336000&api=v2
   :width: 1000pt
.. |image25| image:: https://wiki.onap.org/download/attachments/84650426/Scripts%203.jpg?version=2&modificationDate=1609164337000&api=v2
   :width: 1000pt
.. |image26| image:: https://wiki.onap.org/download/attachments/84650426/Scripts%204.jpg?version=1&modificationDate=1609164989000&api=v2
   :width: 1000pt
.. |image27| image:: https://wiki.onap.org/download/attachments/84650426/Definitions%201.jpg?version=2&modificationDate=1609167774000&api=v2
   :width: 1000pt
.. |image28| image:: https://wiki.onap.org/download/attachments/84650426/Definitions%202.jpg?version=2&modificationDate=1609168384000&api=v2
   :width: 1000pt
.. |image29| image:: https://wiki.onap.org/download/attachments/84650426/Definitions%203.jpg?version=2&modificationDate=1609168385000&api=v2
   :width: 1000pt
.. |image30| image:: https://wiki.onap.org/download/attachments/84650426/DSL.jpg?version=1&modificationDate=1609168557000&api=v2
   :width: 1000pt
.. |image31| image:: https://wiki.onap.org/download/attachments/84650426/Topology%20Template.jpg?version=1&modificationDate=1609169308000&api=v2
   :width: 1000pt
