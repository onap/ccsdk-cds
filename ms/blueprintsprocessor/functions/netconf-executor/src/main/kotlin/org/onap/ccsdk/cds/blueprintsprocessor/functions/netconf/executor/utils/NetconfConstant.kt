/*
 * Copyright © 2017-2019 AT&T, Bell Canada
 *
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
 */
package org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.utils

enum class NetconfDatastore(val datastore: String) {
    RUNNING("running"),
    CANDIDATE("candidate");
}

enum class ModifyAction(val action: String) {
    MERGE("merge"),
    REPLACE("replace"),
    NONE("none")
}

object RpcStatus {

    const val SUCCESS = "success"
    const val FAILURE = "failure"
}

object RpcMessageUtils {

    const val OPEN = "<"
    const val CLOSE = ">"
    const val EQUAL = "="

    const val HASH = "#"
    const val HASH_CHAR = '#'

    const val LF_CHAR = '\n'
    const val NEW_LINE = "\n"

    const val QUOTE = "\""
    const val QUOTE_SPACE = "\" "

    const val TAG_CLOSE = "/>"
    const val END_OF_RPC_OPEN_TAG = "\">"
    const val END_PATTERN = "]]>]]>"

    const val HELLO = "hello"
    const val RPC_REPLY = "rpc-reply"
    const val RPC_ERROR = "rpc-error"

    const val RPC_OPEN = "<rpc "
    const val RPC_CLOSE = "</rpc>"
    const val WITH_DEFAULT_OPEN = "<with-defaults "
    const val WITH_DEFAULT_CLOSE = "</with-defaults>"
    const val DEFAULT_OPERATION_OPEN = "<default-operation>"
    const val DEFAULT_OPERATION_CLOSE = "</default-operation>"
    const val SUBTREE_FILTER_OPEN = "<filter type=\"subtree\">"
    const val SUBTREE_FILTER_CLOSE = "</filter>"
    const val TARGET_OPEN = "<target>"
    const val TARGET_CLOSE = "</target>"
    const val SOURCE_OPEN = "<source>"
    const val SOURCE_CLOSE = "</source>"
    const val CONFIG_OPEN = "<config xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
    const val CONFIG_CLOSE = "</config>"
    const val MSGLEN_REGEX_PATTERN = "\n#\\d+\n"

    const val NUMBER_BETWEEN_QUOTES_MATCHER = "\"+([0-9]+)+\""

    const val XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    const val NETCONF_BASE_NAMESPACE = "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\""
    const val NETCONF_WITH_DEFAULTS_NAMESPACE = "xmlns=\"urn:ietf:params:xml:ns:yang:ietf-netconf-with-defaults\""
    const val SUBSCRIPTION_SUBTREE_FILTER_OPEN =
        "<filter xmlns:base10=\"urn:ietf:params:xml:ns:netconf:base:1.0\" base10:type=\"subtree\">"

    const val INTERLEAVE_CAPABILITY_STRING = "urn:ietf:params:netconf:capability:interleave:1.0"

    const val CAPABILITY_REGEX = "capability>\\s*(.*?)\\s*</capability>"

    const val SESSION_ID_REGEX = "session-id>\\s*(.*?)\\s*session-id>"

    const val MESSAGE_ID_STRING = "message-id"

    const val NETCONF_10_CAPABILITY = "urn:ietf:params:netconf:base:1.0"
    const val NETCONF_11_CAPABILITY = "urn:ietf:params:netconf:base:1.1"
}
