.. This work is licensed under a Creative Commons Attribution 4.0
   International License.
.. http://creativecommons.org/licenses/by/4.0
.. (c) ONAP Project and its contributors
.. _cds_bp_configuration:

Blueprints Processor Configuration
==================================

The blueprints processor is a Spring Boot application. Its defaults live in
``ms/blueprintsprocessor/application/src/main/resources/application.properties``.
In an OOM deployment the chart mounts its own copy of that file
(``oom/kubernetes/cds/components/cds-blueprints-processor/resources/config/application.properties``).

Any property can be set in that file, as a JVM system property
(``-D<name>=<value>``), or as an environment variable using Spring Boot
relaxed binding: upper-case the name, replace ``.`` with ``_`` and drop
``-``. For example ``blueprintsprocessor.grpcPort`` becomes
``BLUEPRINTSPROCESSOR_GRPCPORT``.

This page lists the properties that tune runtime limits. It is not a full
list of every property the blueprints processor reads.

gRPC server
-----------

.. list-table::
   :header-rows: 1
   :widths: 40 15 45

   * - Property
     - Default
     - Description
   * - ``blueprintsprocessor.grpcEnable``
     - ``true``
     - Start the gRPC server for the execution and blueprint management
       services.
   * - ``blueprintsprocessor.grpcPort``
     - ``9111``
     - Port of the gRPC server.
   * - ``blueprintsprocessor.grpcMaxInboundMessageSize``
     - ``4194304``
     - Largest inbound gRPC message the server accepts, in bytes (4 MiB).
       Larger messages are rejected with status ``RESOURCE_EXHAUSTED``.
       Raise it when clients upload large CBAs or send large execution
       payloads over gRPC.

The limit applies only to the gRPC server of the blueprints processor. The
gRPC clients it uses towards the py-executor and other remote services are
not affected.

JSON parsing limits
-------------------

Jackson rejects JSON documents that exceed its stream read constraints, for
example a resolved value or a base64 encoded artifact longer than the
maximum string length. The ``jackson.stream-read-constraints.*`` properties
override those limits for every Jackson parser created in the blueprints
processor. Only the properties that are set change; the others keep the
Jackson default. When none is set, Jackson's defaults are left untouched.

.. list-table::
   :header-rows: 1
   :widths: 50 15 35

   * - Property
     - Jackson default
     - Description
   * - ``jackson.stream-read-constraints.max-string-length``
     - ``20000000``
     - Longest string value, in characters.
   * - ``jackson.stream-read-constraints.max-number-length``
     - ``1000``
     - Longest number literal, in characters.
   * - ``jackson.stream-read-constraints.max-name-length``
     - ``50000``
     - Longest property name, in characters.
   * - ``jackson.stream-read-constraints.max-nesting-depth``
     - ``1000``
     - Deepest nesting of objects and arrays.
   * - ``jackson.stream-read-constraints.max-document-length``
     - ``-1``
     - Largest document, in bytes or characters. ``-1`` means unlimited.
   * - ``jackson.stream-read-constraints.max-token-count``
     - ``-1``
     - Most tokens in one document. ``-1`` means unlimited.

The defaults shown are those of the Jackson version the blueprints processor
ships with (2.20).

Example, allowing string values of up to 50 million characters:

.. code-block:: properties

   jackson.stream-read-constraints.max-string-length=50000000

or, as an environment variable:

.. code-block:: bash

   JACKSON_STREAMREADCONSTRAINTS_MAXSTRINGLENGTH=50000000
