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

resolution_key=${1}
pnf_ip_address=${2}
pnf_username="netconf"
pnf_password="netconf"


template_file="./templates/pnf-config-deploy.template"
temp_file="/tmp/pnf-config-deploy.json"
cp -f $template_file $temp_file

# Now replace the tokens
sed -i "s|PNF_IP_ADDRESS|${pnf_ip_address}|g" $temp_file
sed -i "s|CONFIG_NAME|${resolution_key}|g" $temp_file
sed -i "s|NETCONF_USERNAME|${pnf_username}|g" $temp_file
sed -i "s|NETCONF_PASSWORD|${pnf_password}|g" $temp_file

# Make the REST API to load the models
curl -v --location --request POST http://localhost:8081/api/v1/execution-service/process \
--header 'Content-Type: application/json;charset=UTF-8' \
--header 'Accept: application/json;charset=UTF-8,application/json' \
--header 'Authorization: Basic Y2NzZGthcHBzOmNjc2RrYXBwcw==' \
--header 'Host: cds-blueprints-processor-http:8080' \
--header 'Content-Type: text/json' \
--data  "@$temp_file" | python3 -m json.tool


exit 0
