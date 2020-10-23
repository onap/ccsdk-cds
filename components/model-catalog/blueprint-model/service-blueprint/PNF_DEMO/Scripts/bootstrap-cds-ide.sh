#!/bin/bash
# Copyright © 2020 Aarna Networks, Inc.
# Copyright © 2020 Deutsche Telekom AG
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

# Make the REST API to load the models
curl -v --location --request POST http://localhost:8081/api/v1/blueprint-model/bootstrap \
--header 'Authorization: Basic Y2NzZGthcHBzOmNjc2RrYXBwcw==' \
--header 'Content-Type: application/json' \
--data-raw ' {
"loadModelType" : true,
"loadResourceDictionary" : true,
"loadCBA" : true
}'

exit 0
