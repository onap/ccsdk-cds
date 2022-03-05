.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.


Installation Guide
==================

Installation
------------

ONAP is meant to be deployed within a Kubernetes environment.
Hence, the de-facto way to deploy CDS is through Kubernetes.
ONAP also packages Kubernetes manifest as Charts, using Helm.

Prerequisites
-------------

https://docs.onap.org/en/latest/guides/onap-operator/settingup/index.html#installation

Get the chart
-------------

Make sure to checkout the release to use, by replacing $release-tag in bellow command

git clone https://gerrit.onap.org/r/oom
git checkout tags/$release-tag


Customize blueprint-processor kafka messaging config (Optional)
---------------------------------------------------------------
Optionally, cds can use kafka native messaging to execute a blueprint use case.
The blueprint-processor self-service api is the main api for interacting with CDS at runtime.
The self-service-api topics carry actual request and response payloads,
whereas blueprint-processor self-service-api.audit topics will carry redacted payloads
(without sensitive data) for audit purposes.

By default, cds will target the strimzi kafka cluster in ONAP.
The strimzi kafka config is as follows:

# strimzi kafka config

useStrimziKafka: <true|false>

If useStrimziKafka is true, the following also applies:

1. Strimzi will create an associated kafka user and the topics
   defined for Request and Audit elements below.

2. The type must be kafka-scram-plain-text-auth.

3. The bootstrapServers will target the strimzi kafka cluster by default.

The following fields are configurable via the charts values.yaml
(oom/kubernetes/cds/components/cds-blueprints-processor/values.yaml)

.. code-block:: bash

	kafkaRequestConsumer:
	  enabled: false
	  type: kafka-basic-auth
	  groupId: cds-consumer
	  topic: cds.blueprint-processor.self-service-api.request
	  clientId: request-receiver-client-id
	  pollMillSec: 1000
	kafkaRequestProducer:
	  type: kafka-basic-auth
	  clientId: request-producer-client-id
	  topic: cds.blueprint-processor.self-service-api.response
	  enableIdempotence: false
	kafkaAuditRequest:
	  enabled: false
	  type: kafka-basic-auth
	  clientId: audit-request-producer-client-id
	  topic: cds.blueprint-processor.self-service-api.audit.request
	  enableIdempotence: false
	kafkaAuditResponse:
	  type: kafka-basic-auth
	  clientId: audit-response-producer-client-id
	  topic: cds.blueprint-processor.self-service-api.audit.response
	  enableIdempotence: false

Note:
If more fine grained customization is required, this can be done manually
in the application.properties file before making the helm chart.
(oom/kubernetes/cds/components/cds-blueprints-processor/resources/config/application.properties)


Make the chart
--------------

cd oom/kubernetes
make cds

Install CDS
-----------

helm install --name cds cds

Result
------

.. code-block:: bash
   :linenos:

   $ kubectl get all --selector=release=cds
   NAME                                             READY     STATUS    RESTARTS   AGE
   pod/cds-blueprints-processor-54f758d69f-p98c2    0/1       Running   1          2m
   pod/cds-cds-6bd674dc77-4gtdf                     1/1       Running   0          2m
   pod/cds-cds-db-0                                 1/1       Running   0          2m
   pod/cds-controller-blueprints-545bbf98cf-zwjfc   1/1       Running   0          2m

   NAME                            TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)             AGE
   service/blueprints-processor    ClusterIP   10.43.139.9     <none>        8080/TCP,9111/TCP   2m
   service/cds                     NodePort    10.43.254.69    <none>        3000:30397/TCP      2m
   service/cds-db                  ClusterIP   None            <none>        3306/TCP            2m
   service/controller-blueprints   ClusterIP   10.43.207.152   <none>        8080/TCP            2m

   NAME                                        DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
   deployment.apps/cds-blueprints-processor    1         1         1            0           2m
   deployment.apps/cds-cds                     1         1         1            1           2m
   deployment.apps/cds-controller-blueprints   1         1         1            1           2m

   NAME                                                   DESIRED   CURRENT   READY     AGE
   replicaset.apps/cds-blueprints-processor-54f758d69f    1         1         0         2m
   replicaset.apps/cds-cds-6bd674dc77                     1         1         1         2m
   replicaset.apps/cds-controller-blueprints-545bbf98cf   1         1         1         2m

   NAME                          DESIRED   CURRENT   AGE
   statefulset.apps/cds-cds-db   1         1         2m



Running CDS UI:
---------------

:ref:`running_cds_ui_locally`

Client:
~~~~~~~
Install Node.js and angularCLI. Refer https://angular.io/guide/quickstart
npm install in the directory cds/cds-ui/client
npm run build - to build UI module

Loopback Server:
~~~~~~~~~~~~~~~~

npm install in the directory cds/cds-ui/server
npm start should bring you the CDS UI page in your local machine with the link https://127.0.0.1:3000/

