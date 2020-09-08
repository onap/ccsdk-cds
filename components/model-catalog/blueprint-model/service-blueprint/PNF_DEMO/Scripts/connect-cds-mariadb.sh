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

MARIADB_POD_NAME=$(kubectl get pods -n onap | grep 'dev-cds-db-0' | head -n 1 | awk '{print $1}')
CDS_BP_POD_NAME=$(kubectl get pods -n onap | grep 'cds-blueprints-processor' | head -n 1 | awk '{print $1}')
CDS_BP_SVC_IP=$(kubectl get svc -n onap | grep 'cds-blueprints-processor-http' | awk '{print $3}')

if [ -z "${MARIADB_POD_NAME}" ]
  then
     echo "CDS MariDB POD is not found so cannot proceed further"
     exit 1;
fi

# CDS Mariadb password
MYSQL_PASSWORD=Lase6+CopuBavb
MYSQL_USER=sdnctl
MYSQL_ROOT_PASSWORD=Zumu5%NoyuJagx

echo "Going to connect to CDS MariaDB database sdnctl"
kubectl exec -n onap ${MARIADB_POD_NAME} -it -- mysql -u root -pZumu5%NoyuJagx sdnctl


exit 0
