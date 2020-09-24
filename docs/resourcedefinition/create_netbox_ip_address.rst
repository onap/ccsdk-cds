.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

create_netbox_ip_address code
=============================

.. code-block:: json

  {
    "tags" : "oam-local-ipv4-address",
    "name" : "create_netbox_ip",
    "property" : {
      "description" : "netbox ip",
      "type" : "dt-netbox-ip"
    },
    "updated-by" : "adetalhouet",
    "sources" : {
      "config-data" : {
        "type" : "source-rest",
        "properties" : {
          "type" : "JSON",
          "verb" : "POST",
          "endpoint-selector" : "ipam-1",
          "url-path" : "/api/ipam/prefixes/$prefixId/available-ips/",
          "path" : "",
          "input-key-mapping" : {
            "prefixId" : "prefix-id"
          },
          "output-key-mapping" : {
            "address" : "address",
            "id" : "id"
          },
          "key-dependencies" : [ "prefix-id" ]
        }
      }
    }
  }
