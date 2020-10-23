#!/bin/bash
# Copyright © 2020 Aarna Networks, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

CDS_BP_POD_NAME=$(kubectl get pods -n onap | grep 'cds-blueprints-processor' | head -n 1 | awk '{print $1}')
CDS_BP_SVC_IP=$(kubectl get svc -n onap | grep 'cds-blueprints-processor-http' | awk '{print $3}')

if [ -z "${CDS_BP_SVC_IP}" ]
  then
     echo "CDS BP Service IP is not found and cannot proceed further"
     exit 1;
fi

# Make the REST API to get the models
curl -v --location --request GET http://${CDS_BP_SVC_IP}:8080/api/v1/blueprint-model \
--header 'Authorization: Basic Y2NzZGthcHBzOmNjc2RrYXBwcw==' | python3 -m json.tool

exit 0
