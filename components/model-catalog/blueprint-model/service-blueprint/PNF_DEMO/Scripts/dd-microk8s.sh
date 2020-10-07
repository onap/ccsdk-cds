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

JSON_FILE=$1

CDS_BP_POD_NAME=$(kubectl get pods -n onap | grep 'cds-blueprints-processor' | head -n 1 | awk '{print $1}')
CDS_BP_SVC_IP=$(kubectl get svc -n onap | grep 'cds-blueprints-processor-http' | awk '{print $3}')

if [ -z "${CDS_BP_SVC_IP}" ] || [ -z "${JSON_FILE}" ]
  then
     echo "CDS BP Service IP is not found OR dd.json file is not given"
     echo "Usage : $0 <Data Dictionary JSON file path>"
     exit 1;
fi

l=`jq '.|length' ${JSON_FILE}`
echo "Found $l Dictionary Definition Entries"
i=0
while [ $i -lt $l ]
do
  echo "i = $i"
  d=`jq ".[$i]" ${JSON_FILE}`
  echo $d
  curl -k -v -O "http://${CDS_BP_SVC_IP}:8080/api/v1/dictionary" \
  --header 'Authorization: Basic Y2NzZGthcHBzOmNjc2RrYXBwcw==' \
  --header 'Content-Type: application/json' \
  -d"$d"

  sleep 1

  echo -e "\n*****************************************\n"
  i=$(( $i + 1 ))

done
