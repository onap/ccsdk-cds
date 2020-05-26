#!/bin/sh
#
# Copyright (C) 2019 Bell Canada.
# Modifications Copyright © 2018-2019 AT&T Intellectual Property.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

if [ -z "${APP_PORT}" ]
then
  echo "APP_PORT environment variable is not set, using default."
  export APP_PORT=50052
fi

if [ -z "${ARTIFACT_MANAGER_PORT}" ]
then
  echo "ARTIFACT_MANAGER_PORT environment variable is not set, using default."
  export ARTIFACT_MANAGER_PORT=50053
fi

if [ -z "${AUTH_TOKEN}" ]
then
  echo "AUTH_TOKEN environment variable is not set, using default."
  export AUTH_TOKEN="Basic Y2NzZGthcHBzOmNjc2RrYXBwcw=="
fi

if [ -z "${LOG_FILE}" ]
then
  echo "LOG_FILE environment variable is not set, using default."
  export LOG_FILE="application.log"
fi

if [ -z "${ARTIFACT_MANAGER_SERVER_LOG_FILE}" ]
then
  echo "ARTIFACT_MANAGER_SERVER_LOG_FILE environment variable is not set, using default."
  export ARTIFACT_MANAGER_SERVER_LOG_FILE="artifacts.log"
fi

if [ "${http_proxy}" ]
then
  echo "Setting http_proxy: ${http_proxy}"
fi

if [ "${https_proxy}" ]
then
  echo "Setting https_proxy: ${https_proxy}"
fi

if [ -z "${CONFIGURATION}" ]
then
  echo "CONFIGURATION environment variable is not set, using default."
  export CONFIGURATION="/opt/app/onap/configuration.ini"
fi


cd /opt/app/onap/python/
python server.py
