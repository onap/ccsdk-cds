/*
* ============LICENSE_START=======================================================
* ONAP : CDS
* ================================================================================
* Copyright (C) 2020 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/

import { DictionaryPage } from './model/dictionary.model';

export function getBluePrintPageMock(): DictionaryPage {
    return {
      "content":[
       {
         "name": "vpg_int_pktgen_private_ip_0",
         "tags": "vpg_int_pktgen_private_ip_0",
         "data_type": "string",
         "description": "vpg_int_pktgen_private_ip_0",
         "entry_schema": "string",
         "updatedBy": "Singal, Kapil <ks220y@att.com>",
         "createdDate":"",
         "definition": {
           "tags": "vpg_int_pktgen_private_ip_0",
           "name": "vpg_int_pktgen_private_ip_0",
           "property": {
             "description": "vpg_int_pktgen_private_ip_0",
             "type": "string"
           },
           "updated-by": "Singal, Kapil <ks220y@att.com>",
           "sources": {
             "input": {
               "type": "source-input"
             },
             "default": {
               "type": "source-default",
               "properties": {}
             },
             "sdnc": {
               "type": "source-rest",
               "properties": {
                 "verb": "GET",
                 "type": "JSON",
                 "url-path": "/restconf/config/GENERIC-RESOURCE-API:services/service/$service-instance-id/service-data/vnfs/vnf/$vnf-id/vnf-data/vnf-topology/vnf-parameters-data/param/vpg_int_pktgen_private_ip_0",
                 "path": "/param/0/value",
                 "input-key-mapping": {
                   "service-instance-id": "service-instance-id",
                   "vnf-id": "vnf-id"
                 },
                 "output-key-mapping": {
                   "vpg_int_pktgen_private_ip_0": "value"
                 },
                 "key-dependencies": [
                   "service-instance-id",
                   "vnf-id"
                 ]
               }
             }
           }
         }
       },
       {
         "name": "active-streams",
         "tags": "active-streams",
         "data_type": "string",
         "description": "active-streams",
         "entry_schema": "string",
         "updatedBy": "Singal, Kapil <ks220y@att.com>",
       "createdDate":"",
         "definition": {
           "tags": "active-streams",
           "name": "active-streams",
           "property": {
             "description": "active-streams",
             "type": "string"
           },
           "updated-by": "MALAKOV, YURIY <yuriy.malakov@att.com>",
           "sources": {
             "input": {
               "type": "source-input"
             },
             "default": {
               "type": "source-default",
               "properties": {}
             }
           }
         }
       },
       {
         "tags": "vpg_int_private1_ip_0",
         "name": "vpg_int_private1_ip_0",
         "data_type": "string",
         "description": "vpg_int_private1_ip_0",
         "entry_schema": "string",
         "updatedBy": "Singal, Kapil <ks220y@att.com>",
       "createdDate":"",
         "definition": {
           "tags": "vpg_int_private1_ip_0",
           "name": "vpg_int_private1_ip_0",
           "property": {
             "description": "vpg_int_private1_ip_0",
             "type": "string"
           },
           "updated-by": "Singal, Kapil <ks220y@att.com>",
           "sources": {
             "input": {
               "type": "source-input"
             },
             "default": {
               "type": "source-default",
               "properties": {}
             },
             "sdnc": {
               "type": "source-rest",
               "properties": {
                 "verb": "GET",
                 "type": "JSON",
                 "url-path": "/restconf/config/GENERIC-RESOURCE-API:services/service/$service-instance-id/service-data/vnfs/vnf/$vnf-id/vnf-data/vnf-topology/vnf-parameters-data/param/vpg_int_private1_ip_0",
                 "path": "/param/0/value",
                 "input-key-mapping": {
                   "service-instance-id": "service-instance-id",
                   "vnf-id": "vnf-id"
                 },
                 "output-key-mapping": {
                   "vpg_int_private1_ip_0": "value"
                 },
                 "key-dependencies": [
                   "service-instance-id",
                   "vnf-id"
                 ]
               }
             }
           }
         }
       },
       {
         "name": "put-active-streams",
         "tags": "put-active-streams",
         "data_type": "string",
         "description": "put-active-streams",
         "entry_schema": "string",
       "createdDate":"",
         "definition": {
           "tags": "put-active-streams",
           "name": "put-active-streams",
           "property": {
             "description": "put-active-streams",
             "type": "string"
           },
           "updated-by": "Singal, Kapil <ks220y@att.com>",
           "sources": {
             "sdnc": {
               "type": "source-rest",
               "properties": {
                 "verb": "PUT",
                 "type": "JSON",
                 "url-path": "$vpg_onap_private_ip_0:8183/restconf/config/stream-count:stream-count/streams",
                 "path": "/param/0/value",
                 "input-key-mapping": {
                   "vpg_onap_private_ip_0": "vpg_onap_private_ip_0",
                   "active-streams": "active-streams"
                 },
                 "output-key-mapping": {
             
                 },
                 "key-dependencies": [
                   "vpg_onap_private_ip_0",
                   "active-streams"
                 ],
                 
                 "payload": "{\"streams\": {\"active-streams\": $active-streams}}"
               }
             },
             "input": {
               "type": "source-input"
             },
             "default": {
               "type": "source-default",
               "properties": {}
             }
           }
         },
         "updatedBy": "Singal, Kapil <ks220y@att.com>"
       },
       {
         "name": "vpg_onap_private_ip_0",
         "tags": "vpg_onap_private_ip_0",
         "data_type": "string",
         "description": "vpg_onap_private_ip_0",
         "entry_schema": "string",
         "updatedBy": "MALAKOV, YURIY <yuriy.malakov@att.com>",
       "createdDate":"",
         "definition": {
           "tags": "vpg_onap_private_ip_0",
           "name": "vpg_onap_private_ip_0",
           "property": {
             "description": "vpg_onap_private_ip_0",
             "type": "string"
           },
           "updated-by": "MALAKOV, YURIY <yuriy.malakov@att.com>",
           "sources": {
             "sdnc": {
               "type": "source-rest",
               "properties": {
                 "type": "JSON",
                 "verb": "GET",
                 "url-path": "/restconf/config/GENERIC-RESOURCE-API:services/service/$service-instance-id/service-data/vnfs/vnf/$vnf-id/vnf-data/vnf-topology/vnf-parameters-data/param/vpg_onap_private_ip_0",
                 "path": "/param/0/value",
                 "input-key-mapping": {
                   "service-instance-id": "service-instance-id",
                   "vnf-id": "vnf-id"
                 },
                 "output-key-mapping": {
                   "vpg_onap_private_ip_0": "value"
                 },
                 "key-dependencies": [
                   "service-instance-id",
                   "vnf-id"
                 ]
               }
             },
             "input": {
               "type": "source-input"
             },
             "default": {
               "type": "source-default",
               "properties": {}
             }
           }
         }
       },
       {
         "tags": "create-md-sal-vnf-param",
         "name": "create-md-sal-vnf-param",
         "data_type": "string",
         "description": "create-md-sal-vnf-param",
         "entry_schema": "string",
         "updatedBy": "Singal, Kapil <ks220y@att.com>",
       "createdDate":"",
         "definition": {
           "tags": "create-md-sal-vnf-param",
           "name": "create-md-sal-vnf-param",
           "property": {
             "description": "create-md-sal-vnf-param",
             "type": "string"
           },
           "updated-by": "Yuriy Malakov",
           "sources": {
             "sdnc": {
               "type": "source-rest",
               "properties": {
                 "type": "JSON",
                 "verb": "PUT",
                 "url-path": "/restconf/config/GENERIC-RESOURCE-API:services/service/$service-instance-id/service-data/vnfs/vnf/$vnf-id/vnf-data/vnf-topology/vnf-parameters-data/param/vdns_vf_module_id",
                 "path": "",
                 "payload": "{\n\"GENERIC-RESOURCE-API:param\": [\n{\n\"GENERIC-RESOURCE-API:name\": \"vdns_vf_module_id\",\n\"GENERIC-RESOURCE-API:value\": \"$vf-module-id\"\n}\n]\n}",
                 "input-key-mapping": {
                   "service-instance-id": "service-instance-id",
                   "vnf-id": "vnf-id",
                   "vf-module-id": "vf-module-id"
                 },
                 "output-key-mapping": {},
                 "key-dependencies": [
                   "vf-module-id",
                   "service-instance-id",
                   "vnf-id"
                 ]
               }
             },
             "aai-data": {
               "type": "source-rest",
               "properties": {
                 "verb": "PATCH",
                 "type": "JSON",
                 "url-path": "/aai/v14/network/generic-vnfs/generic-vnf/$vnf-id/nm-profile-name",
                 "payload": "{\"nm-profile-name\":\"$vf-module-id\"}",
                 "path": "",
                 "input-key-mapping": {
                   "vnf-id": "vnf-id"
                 },
                 "output-key-mapping": {},
                 "key-dependencies": [
                   "vnf-id"
                 ]
               }
             }
           }
         }
       }
     ],
      "pageable":{
         "sort":{
            "sorted":true,
            "unsorted":false,
            "empty":false
         },
         "offset":0,
         "pageSize":2,
         "pageNumber":0,
         "paged":true,
         "unpaged":false
      },
      "last":false,
      "totalElements":5,
      "totalPages":1,
      "first":true,
      "empty":false
   };
}

