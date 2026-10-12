.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2026 Deutsche Telekom AG.

.. _cds_database:

Database
========

The Blueprints Processor stores CBAs, model types, resource dictionaries,
resolution results, configuration snapshots and workflow audit records in a
PostgreSQL database (tested with PostgreSQL 13, the version deployed by OOM,
and 17). MariaDB is no longer supported as the CDS database.

The schema is created and updated by Hibernate at start-up
(``blueprintsprocessor.db.hibernateHbm2ddlAuto=update``); there are no
separate SQL scripts to run.

Configuration
-------------

+-------------------------------------------------+-------------------------------------------------+
| Property                                        | Value                                           |
+=================================================+=================================================+
| ``blueprintsprocessor.db.url``                  | ``jdbc:postgresql://<host>:5432/<database>``    |
+-------------------------------------------------+-------------------------------------------------+
| ``blueprintsprocessor.db.username``             | database user                                   |
+-------------------------------------------------+-------------------------------------------------+
| ``blueprintsprocessor.db.password``             | database password                               |
+-------------------------------------------------+-------------------------------------------------+
| ``blueprintsprocessor.db.driverClassName``      | ``org.postgresql.Driver``                       |
+-------------------------------------------------+-------------------------------------------------+
| ``blueprintsprocessor.db.hibernateDialect``     | ``org.hibernate.dialect.PostgreSQLDialect``     |
+-------------------------------------------------+-------------------------------------------------+
| ``blueprintsprocessor.db.hibernateHbm2ddlAuto`` | ``update``                                      |
+-------------------------------------------------+-------------------------------------------------+

processor-db and CBA database connections
-----------------------------------------

The ``processor-db`` resource source and database connections defined inside a
CBA can still target other databases. Supported ``type`` values are
``maria-db``, ``mysql-db``, ``mssql`` and ``postgres-db``. In OOM,
``processor-db`` points at the SDNC MariaDB by default.

.. _cds_database_upgrade:

Upgrading from a MariaDB-based release
--------------------------------------

The new release does not migrate data by itself, and the OOM ``cds`` chart no
longer deploys the CDS MariaDB: upgrading the chart removes it. Take a dump
**before** upgrading, then choose one of the following.

**Option A: re-onboard the CBAs.** Upload or publish the CBAs again after the
upgrade. Stored resource resolutions, configuration snapshots and workflow
audit records are lost.

**Option B: copy the data with pgloader.** This keeps everything.

#. Before upgrading, dump the CDS database. With the default chart values the
   MariaDB pod is ``cds-db-0`` and the password of the ``sdnctl`` user is in
   the ``<release>-cds-db-secret`` secret:

   .. code-block:: bash

      PW=$(kubectl get secret -n onap <release>-cds-db-secret -o jsonpath='{.data.password}' | base64 -d)
      kubectl exec -n onap cds-db-0 -c mariadb -- \
        mariadb-dump -usdnctl -p"$PW" --single-transaction --no-tablespaces sdnctl > cds-dump.sql

#. Upgrade the ``cds`` chart. Wait until the Blueprints Processor is ready:
   it creates the tables in PostgreSQL and loads the initial model types and
   resource dictionaries. Then scale it to 0.
#. Import the dump into a temporary MariaDB that pgloader can reach, for
   example with Docker on the machine running pgloader:

   .. code-block:: bash

      docker network create cds-migration
      docker run -d --name cds-mariadb --network cds-migration -e MARIADB_ROOT_PASSWORD=<password> \
        -e MARIADB_DATABASE=sdnctl -e MARIADB_USER=sdnctl -e MARIADB_PASSWORD=<password> mariadb:11
      docker exec -i cds-mariadb mariadb -usdnctl -p<password> sdnctl < cds-dump.sql

#. Make the PostgreSQL primary reachable, for example with
   ``kubectl port-forward -n onap svc/cds-pg-primary 5432:5432``, and save
   the following command file as ``cds.load``. Run pgloader as the CDS
   database user; no superuser is needed.

   .. code-block:: text

      LOAD DATABASE
           FROM mysql://sdnctl:<password>@<mariadb-host>/sdnctl
           INTO postgresql://sdnctl:<password>@<postgres-host>/sdnctl
      WITH create no tables, data only, truncate
      CAST type datetime to timestamp drop default using zero-dates-to-null
      INCLUDING ONLY TABLE NAMES MATCHING 'BLUEPRINT_MODEL', 'BLUEPRINT_MODEL_CONTENT',
           'BLUEPRINT_WORKFLOW_AUDIT_STATUS', 'ERROR_DOMAINS', 'ERROR_DOMAINS_ERROR_MESSAGES',
           'ERROR_MESSAGES', 'MODEL_TYPE', 'RESOURCE_CONFIG_SNAPSHOT', 'RESOURCE_DICTIONARY',
           'RESOURCE_RESOLUTION', 'TEMPLATE_RESOLUTION';

   ``truncate`` replaces the initial data loaded at start-up with the
   migrated rows. pgloader writes into the PostgreSQL schema named after the
   MySQL database, ``sdnctl``, which is where the Blueprints Processor creates
   its tables on the OOM PostgreSQL. If your PostgreSQL has the CDS tables in
   the ``public`` schema instead, add this line before ``CAST``:

   .. code-block:: text

      ALTER SCHEMA 'sdnctl' RENAME TO 'public'

   Run it, for example with ``docker run --rm --network cds-migration -v
   $PWD/cds.load:/cds.load dimitri/pgloader pgloader /cds.load``.
   pgloader lower-cases the MariaDB table and column names, which matches the
   names the Blueprints Processor creates on PostgreSQL.

#. pgloader does not move the identity sequence of the workflow audit table.
   Reset it as the CDS database user:

   .. code-block:: sql

      SELECT setval(pg_get_serial_sequence('blueprint_workflow_audit_status', 'workflow_audit_id'),
                    coalesce(max(workflow_audit_id), 0) + 1, false)
        FROM blueprint_workflow_audit_status;

#. Compare the row count of every table listed above in both databases, scale
   the Blueprints Processor back up and check that the CBAs are listed by
   ``GET /api/v1/blueprint-model``. Remove the temporary MariaDB afterwards.
