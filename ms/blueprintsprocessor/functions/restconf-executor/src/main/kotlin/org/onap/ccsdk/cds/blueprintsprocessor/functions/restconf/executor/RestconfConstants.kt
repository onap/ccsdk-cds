package org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor

class RestconfConstants {
    companion object {
        const val NODE_ID = "node-id"
        const val FAIL_FAST = "fail-fast"
        const val RESTCONF_CONNECTION_CONFIG = "restconf-connection-config"
        const val MOUNT_PAYLOAD = "mount-payload"
        const val ACTION_INPUT = "action-input"
        const val ACTION_OUTPUT = "action-output"
        const val ACTION_TYPE = "action-type"
        const val ACTION_DATASTORE = "action-datastore"
        const val ACTION_PATH = "action-path"
        const val ACTION_PAYLOAD = "action-payload"
        const val RESTCONF_TOPOLOGY_CONFIG_PATH =
            "/restconf/config/network-topology:network-topology/topology/topology-netconf/node"
        const val RESTCONF_TOPOLOGY_OPER_PATH =
            "/restconf/operational/network-topology:network-topology/topology/topology-netconf/node"
        val HTTP_SUCCESS_RANGE = 200..204
    }
}
