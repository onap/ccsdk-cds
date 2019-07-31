# Acceptance Testing Blueprints

## What is BP User Acceptance Tests (UATs)?

UATs aims to fully test the BlueprintsProcessor (BPP) using a blueprint.
The BPP runs in an almost production-like configuration with some minor exceptions:

- It uses an embedded, in-memory, and initially empty H2 database, running in MySQL/MariaDB compatibility mode;
- All external services are mocked.
  
## How it works?

The UATs are declarative, data-driven tests implemented in YAML 1.1 documents.
This YAML files express:

- Sequence of requests to be sent to the BPP for every process;
- The expected BPP responses;
- For every used external service:
  - The `selector` used internally to instantiate the rest client;
  - A variable set of expected requests and corresponding responses.

The UAT engine will perform the following validations:

- The BPP responses;
- The payloads in the external services requests and it's content type.

## Adding your BP to the suite of UATs

To add a new BP to the UAT suite, all you need to do is:
1. Add your blueprint folder under
CDS project's `components/model-catalog/blueprint-model/uat-blueprints` directory;
2. Create a `Tests/uat.yaml` document under your BP folder.

## `uat.yaml` reference

### Skeleton of a basic `uat.yaml`

```yaml
%YAML 1.1
---
processes:
  - name: process1
    request:
      commonHeader: &commonHeader
        originatorId: sdnc
        requestId: "123456-1000"
        subRequestId: sub-123456-1000
      actionIdentifiers: &assign-ai
        blueprintName: configuration_over_restconf
        blueprintVersion: "1.0.0"
        actionName: config-assign
        mode: sync
      payload:
        # ...
    expectedResponse:
      commonHeader: *commonHeader
      actionIdentifiers: *assign-ai
      status:
        code: 200
        eventType: EVENT_COMPONENT_EXECUTED
        errorMessage: null
        message: success
      payload:
        # ...
      stepData:
        name: config-assign
        properties:
          resource-assignment-params:
            # ...
          status: success
  - name: process2
    # ...

external-services:
  - selector: odl
    expectations:
      - request:
          method: GET
          path:
        response:
          status: 200  # optional, 200 is the default value
          body: # optional, default is an empty content
            # ...
      - request:
          method: POST
          path:
          content-type: application/json
          body:
            # JSON request body
        response:
          status: 201
```

### Composite URI paths

In case your YAML document contains many URI path definitions, you'd better keep the duplications
as low as possible in order to ease the document maintenance, and avoid inconsistencies.
 
Since YAML doesn't provide a standard mechanism to concatenate strings,
the UAT engine implements an ad-hoc mechanism based on multi-level lists.
Please note that currently this mechanism is only applied to URI paths.

To exemplify how it works, let's take the case of eliminating duplications when defining multiple OpenDaylight URLs.

You might starting using the following definitions:
```yaml
   nodeId: &nodeId "new-netconf-device"
   # ...
   - request:
     path: &configUri [restconf/config, &nodeIdentifier [network-topology:network-topology/topology/topology-netconf/node, *nodeId]]
   # ...
   - request:
     path: [restconf/operational, *nodeIdentifier]
   # ...
   - request:
     path: [*configUri, &configletResourcePath yang-ext:mount/mynetconf:netconflist]
```

The UAT engine will expand the above multi-level lists, resulting on the following URI paths:
```yaml
   # ...
   - request:
     path: restconf/config/network-topology:network-topology/topology/topology-netconf/node/new-netconf-device
   # ...
   - request:
     path: restconf/operational/network-topology:network-topology/topology/topology-netconf/node/new-netconf-device
   # ...
   - request:
     path: restconf/config/network-topology:network-topology/topology/topology-netconf/node/new-netconf-device/yang-ext:mount/mynetconf:netconflist
``` 

## License

Copyright (C) 2019 Nordix Foundation.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
