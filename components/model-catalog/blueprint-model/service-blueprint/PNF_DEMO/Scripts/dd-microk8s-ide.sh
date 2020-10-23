#!/bin/bash
# Copyright © 2020 Aarna Networks, Inc.
# Copyright © 2020 Deutsche Telekom AG.

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


if [ -z "${JSON_FILE}" ]
  then
     echo " dd.json file is not given"
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
  curl -k -v -O "http://localhost:8081/api/v1/dictionary" \
  --header 'Authorization: Basic Y2NzZGthcHBzOmNjc2RrYXBwcw==' \
  --header 'Content-Type: application/json' \
  -d"$d"

  sleep 1

  echo -e "\n*****************************************\n"
  i=$(( $i + 1 ))

done
