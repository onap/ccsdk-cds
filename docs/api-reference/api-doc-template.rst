.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2020 Deutsche Telekom AG.

.. This is a template to document new APIs for CDS blueprint processor

.. make use of tabs whenever it fits

Module
====================

Resource 1
------------

General description about the resource.


Method 1 Endpoint 1
~~~~~~~~~~~~~~~~~~~~

<method> ``<path>``
......................

Method 1 Endpoint 1 description

Request
...........

.. code-block:: bash
   :caption: **(sample) request**

   request command

.. can be split into Header and Body description if thats more suitable.
.. If its split, Header requires content-type definition, Body requires example payload

**Request Path Parameters:**

.. list-table::
   :widths: 20 20 20 40
   :header-rows: 1

   * - Parameter
     - Type
     - Required
     - Description
   * - value 1
     - value 2
     - value 3
     - value 4
   * - value 1
     - value 2
     - value 3
     - value 4

**Request Query Parameters:**

.. list-table::
   :widths: 20 20 20 40
   :header-rows: 1

   * - Parameter
     - Type
     - Required
     - Description
   * - value 1
     - value 2
     - value 3
     - value 4
   * - value 1
     - value 2
     - value 3
     - value 4

**Request Body Parameters:**

.. list-table::
   :widths: 20 20 20 40
   :header-rows: 1

   * - Parameter
     - Type
     - Required
     - Description
   * - value 1
     - value 2
     - value 3
     - value 4
   * - value 1
     - value 2
     - value 3
     - value 4

Success Response(s)
......................

HTTP Status 202 OK

Headers:
``Content-Type:application/json``

.. code-block:: json
   :caption: **(sample) response body and/or response schema**

   (sample) response (can be {})

**Success Response Parameters:**

.. list-table::
   :widths: 30 30 40
   :header-rows: 1

   * - Parameter
     - Type
     - Description
   * - value 1
     - value 2
     - value 3
   * - value 1
     - value 2
     - value 3

Error Response(s)
......................

HTTP Status 404 The requested resource could not be found

.. code-block:: json
   :caption: **sample error response**

   error response

**Error Response Parameters:**

.. list-table::
   :widths: 30 30 40
   :header-rows: 1

   * - Parameter
     - Type
     - Description
   * - value 1
     - value 2
     - value 3
   * - value 1
     - value 2
     - value 3

.. or just table for responses with HTTP code, description and schema

Consumes
............

``application/json``

Produces
...........

``application/json``


Functional Description
..............................

What does the API do in detail?

Technical Description
...........................

Called class, methods, other hints.

Related topics
......................

.. toctree::
   :maxdepth: 1

   topic1
   topic2


Method 2 Endpoint 1
~~~~~~~~~~~~~~~~~~~~

<method> ``<path>``
......................

Method 2 Endpoint 1 description

..


Method 1 Endpoint 2 (Subresource):
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

<method> ``<path><subpath>``
..............................


..

Resource 2
--------------------


..

