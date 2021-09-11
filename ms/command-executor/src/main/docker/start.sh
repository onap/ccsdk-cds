#!/bin/sh

#
# Copyright (C) 2019 Bell Canada.
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
  export APP_PORT=50051
fi

if [ -z "${PROMETHEUS_PORT}" ]
then
  echo "PROMETHEUS_PORT environment variable is not set, using default(10005)."
  export PROMETHEUS_PORT=10005
fi

if [ -z "${PROMETHEUS_METRICS_ENABLED}" ]
then
  echo "PROMETHEUS_METRICS_ENABLED environment variable is not set, using default(false)."
  #enable this feature via charts.
  export PROMETHEUS_METRICS_ENABLED=false
fi

if [ -z "${BASIC_AUTH}" ]
then
  echo "BASIC_AUTH environment variable is not set, using default."
  export BASIC_AUTH="Basic Y2NzZGthcHBzOmNjc2RrYXBwcw=="
fi

if [ "${http_proxy}" ]
then
  echo "Setting http_proxy: ${http_proxy}"
fi

if [ "${https_proxy}" ]
then
  echo "Setting https_proxy: ${https_proxy}"
fi

cd /opt/app/onap/python/
python server.py ${APP_PORT} ${BASIC_AUTH}
