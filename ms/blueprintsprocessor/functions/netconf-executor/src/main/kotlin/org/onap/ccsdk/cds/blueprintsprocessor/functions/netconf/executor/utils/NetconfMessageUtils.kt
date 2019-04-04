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

import org.apache.commons.lang3.StringUtils
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfException
import org.slf4j.LoggerFactory
import org.xml.sax.InputSource
import java.io.StringReader
import java.nio.charset.StandardCharsets
import java.util.regex.MatchResult
import java.util.regex.Pattern
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.text.Charsets.UTF_8


class NetconfMessageUtils {

    companion object {
        val log = LoggerFactory.getLogger(NetconfMessageUtils::class.java)

        const val NEW_LINE = "\n"
        const val CHUNKED_END_REGEX_PATTERN = "\n##\n"

        val CAPABILITY_REGEX_PATTERN: Pattern = Pattern.compile(RpcMessageUtils.CAPABILITY_REGEX)
        val SESSION_ID_REGEX_PATTERN: Pattern = Pattern.compile(RpcMessageUtils.SESSION_ID_REGEX)

        private val CHUNKED_FRAMING_PATTERN: Pattern =
            Pattern.compile("(\\n#([1-9][0-9]*)\\n(.+))+\\n##\\n", Pattern.DOTALL)
        private val CHUNKED_SIZE_PATTERN: Pattern = Pattern.compile("\\n#([1-9][0-9]*)\\n")
        private val MSG_ID_STRING_PATTERN = Pattern.compile("${RpcMessageUtils.MESSAGE_ID_STRING}=\"(.*?)\"")

        fun getConfig(messageId: String, configType: String, filterContent: String?): String {
            val request = StringBuilder()

            request.append("<get-config>").append(NEW_LINE)
            request.append(RpcMessageUtils.SOURCE_OPEN).append(NEW_LINE)
            request.append(RpcMessageUtils.OPEN).append(configType).append(RpcMessageUtils.TAG_CLOSE)
                .append(NEW_LINE)
            request.append(RpcMessageUtils.SOURCE_CLOSE).append(NEW_LINE)

            if (!filterContent.isNullOrEmpty()) {
                request.append(RpcMessageUtils.SUBTREE_FILTER_OPEN).append(NEW_LINE)
                request.append(filterContent).append(NEW_LINE)
                request.append(RpcMessageUtils.SUBTREE_FILTER_CLOSE).append(NEW_LINE)
            }
            request.append("</get-config>").append(NEW_LINE)

            return doWrappedRpc(messageId, request.toString())
        }

        fun doWrappedRpc(messageId: String, request: String): String {
            val rpc = StringBuilder(RpcMessageUtils.XML_HEADER).append(NEW_LINE)
            rpc.append(RpcMessageUtils.RPC_OPEN)
            rpc.append(RpcMessageUtils.MESSAGE_ID_STRING).append(RpcMessageUtils.EQUAL)
            rpc.append(RpcMessageUtils.QUOTE).append(messageId).append(RpcMessageUtils.QUOTE_SPACE)
            rpc.append(RpcMessageUtils.NETCONF_BASE_NAMESPACE).append(RpcMessageUtils.CLOSE)
                .append(NEW_LINE)
            rpc.append(request)
            rpc.append(RpcMessageUtils.RPC_CLOSE)
            // rpc.append(NEW_LINE).append(END_PATTERN);

            return rpc.toString()
        }

        fun editConfig(messageId: String, configType: String, defaultOperation: String?,
                       newConfiguration: String): String {

            val request = StringBuilder()

            request.append("<edit-config>").append(NEW_LINE)
            request.append(RpcMessageUtils.TARGET_OPEN).append(NEW_LINE)
            request.append(RpcMessageUtils.OPEN).append(configType).append(RpcMessageUtils.TAG_CLOSE)
                .append(NEW_LINE)
            request.append(RpcMessageUtils.TARGET_CLOSE).append(NEW_LINE)

            if (defaultOperation != null) {
                request.append(RpcMessageUtils.DEFAULT_OPERATION_OPEN).append(defaultOperation)
                    .append(RpcMessageUtils.DEFAULT_OPERATION_CLOSE)
                request.append(NEW_LINE)
            }

            request.append(RpcMessageUtils.CONFIG_OPEN).append(NEW_LINE)
            request.append(newConfiguration.trim { it <= ' ' }).append(NEW_LINE)
            request.append(RpcMessageUtils.CONFIG_CLOSE).append(NEW_LINE)
            request.append("</edit-config>").append(NEW_LINE)

            return doWrappedRpc(messageId, request.toString())
        }

        fun validate(messageId: String, configType: String): String {
            val request = StringBuilder()

            request.append("<validate>").append(NEW_LINE)
            request.append(RpcMessageUtils.SOURCE_OPEN).append(NEW_LINE)
            request.append(RpcMessageUtils.OPEN).append(configType).append(RpcMessageUtils.TAG_CLOSE)
                .append(NEW_LINE)
            request.append(RpcMessageUtils.SOURCE_CLOSE).append(NEW_LINE)
            request.append("</validate>").append(NEW_LINE)

            return doWrappedRpc(messageId, request.toString())
        }

        fun commit(messageId: String, confirmed: Boolean, confirmTimeout: Int, persist: String,
                   persistId: String): String {

            if (!persist.isEmpty() && !persistId.isEmpty()) {
                throw NetconfException("Can't proceed <commit> with both persist($persist) and " +
                        "persistId($persistId) specified. Only one should be specified.")
            }
            if (confirmed && !persistId.isEmpty()) {
                throw NetconfException("Can't proceed <commit> with both confirmed flag and " +
                        "persistId($persistId) specified. Only one should be specified.")
            }

            val request = StringBuilder()
            request.append("<commit>").append(NEW_LINE)
            if (confirmed) {
                request.append("<confirmed/>")
                request.append("<confirm-timeout>$confirmTimeout</confirm-timeout>")
                if (!persist.isEmpty()) {
                    request.append("<persist>$persist</persist>")
                }
            }
            if (!persistId.isEmpty()) {
                request.append("<persist-id>$persistId</persist-id>")
            }
            request.append("</commit>").append(NEW_LINE)

            return doWrappedRpc(messageId, request.toString())
        }

        fun cancelCommit(messageId: String, persistId: String): String {
            val request = StringBuilder()
            request.append("<cancel-commit>").append(NEW_LINE)
            if (!persistId.isEmpty()) {
                request.append("<persist-id>$persistId</persist-id>")
            }
            request.append("</cancel-commit>").append(NEW_LINE)

            return doWrappedRpc(messageId, request.toString())
        }

        fun unlock(messageId: String, configType: String): String {
            val request = StringBuilder()

            request.append("<unlock>").append(NEW_LINE)
            request.append(RpcMessageUtils.TARGET_OPEN).append(NEW_LINE)
            request.append(RpcMessageUtils.OPEN).append(configType).append(RpcMessageUtils.TAG_CLOSE)
                .append(NEW_LINE)
            request.append(RpcMessageUtils.TARGET_CLOSE).append(NEW_LINE)
            request.append("</unlock>").append(NEW_LINE)

            return doWrappedRpc(messageId, request.toString())
        }

        @Throws(NetconfException::class)
        fun deleteConfig(messageId: String, configType: String): String {
            if (configType == NetconfDatastore.RUNNING.datastore) {
                log.warn("Target configuration for delete operation can't be \"running\" {}", configType)
                throw NetconfException("Target configuration for delete operation can't be running")
            }

            val request = StringBuilder()

            request.append("<delete-config>").append(NEW_LINE)
            request.append(RpcMessageUtils.TARGET_OPEN).append(NEW_LINE)
            request.append(RpcMessageUtils.OPEN).append(configType)
                .append(RpcMessageUtils.TAG_CLOSE)
                .append(NEW_LINE)
            request.append(RpcMessageUtils.TARGET_CLOSE).append(NEW_LINE)
            request.append("</delete-config>").append(NEW_LINE)

            return doWrappedRpc(messageId, request.toString())
        }

        fun discardChanges(messageId: String): String {
            return doWrappedRpc(messageId, "<discard-changes/>$NEW_LINE")
        }

        fun lock(messageId: String, configType: String): String {
            val request = StringBuilder()

            request.append("<lock>").append(NEW_LINE)
            request.append(RpcMessageUtils.TARGET_OPEN).append(NEW_LINE)
            request.append(RpcMessageUtils.OPEN).append(configType).append(RpcMessageUtils.TAG_CLOSE)
                .append(NEW_LINE)
            request.append(RpcMessageUtils.TARGET_CLOSE).append(NEW_LINE)
            request.append("</lock>").append(NEW_LINE)

            return doWrappedRpc(messageId, request.toString())
        }

        fun closeSession(messageId: String, force: Boolean): String {
            val request = if(force) "<kill-session/>$NEW_LINE" else "<close-session/>$NEW_LINE"
            return doWrappedRpc(messageId, request.toString())
        }

        fun validateRPCXML(rpcRequest: String): Boolean {
            try {
                if (StringUtils.isBlank(rpcRequest)) {
                    return false
                }
                val dbf = DocumentBuilderFactory.newInstance()
                dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
                dbf.setFeature("http://xml.org/sax/features/external-general-entities", false)
                dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
                dbf.newDocumentBuilder()
                    .parse(InputSource(StringReader(rpcRequest.replace(RpcMessageUtils.END_PATTERN, ""))))
                return true
            } catch (e: Exception) {
                return false
            }
        }

        fun getMsgId(message: String): String {
            val matcher = MSG_ID_STRING_PATTERN.matcher(message)
            if (matcher.find()) {
                return matcher.group(1)
            }
            return when {
                message.contains(RpcMessageUtils.HELLO) -> "-1"
                else -> ""
            }
        }

        fun validateChunkedFraming(reply: String): Boolean {
            val matcher = CHUNKED_FRAMING_PATTERN.matcher(reply)
            if (!matcher.matches()) {
                log.debug("Error Reply: {}", reply)
                return false
            }
            val chunkM = CHUNKED_SIZE_PATTERN.matcher(reply)
            val chunks = ArrayList<MatchResult>()
            var chunkdataStr = ""
            while (chunkM.find()) {
                chunks.add(chunkM.toMatchResult())
                // extract chunk-data (and later) in bytes
                val bytes = Integer.parseInt(chunkM.group(1))
                val chunkdata = reply.substring(chunkM.end()).toByteArray(StandardCharsets.UTF_8)
                if (bytes > chunkdata.size) {
                    log.debug("Error Reply - wrong chunk size {}", reply)
                    return false
                }
                // convert (only) chunk-data part into String
                chunkdataStr = String(chunkdata, 0, bytes, StandardCharsets.UTF_8)
                // skip chunk-data part from next match
                chunkM.region(chunkM.end() + chunkdataStr.length, reply.length)
            }
            if (!CHUNKED_END_REGEX_PATTERN.equals(reply.substring(chunks[chunks.size - 1].end() + chunkdataStr.length))) {
                log.debug("Error Reply: {}", reply)
                return false
            }
            return true
        }

        fun createHelloString(capabilities: List<String>): String {
            //TODO rewrite as templates
            val helloMessage = StringBuilder()
            helloMessage.append(RpcMessageUtils.XML_HEADER).append(NEW_LINE)
            helloMessage.append("<hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">").append(NEW_LINE)
            helloMessage.append("  <capabilities>").append(NEW_LINE)
                capabilities.forEach { cap ->
                    helloMessage.append("    <capability>").append(cap).append("</capability>").append(NEW_LINE)
                }
            helloMessage.append("  </capabilities>").append(NEW_LINE)
            helloMessage.append("</hello>").append(NEW_LINE)
            helloMessage.append(RpcMessageUtils.END_PATTERN)
            return helloMessage.toString()
        }

        fun formatRPCRequest(request: String, messageId: String, deviceCapabilities: Set<String>): String {
            var formattedRequest = formatNetconfMessage(deviceCapabilities, request)
            formattedRequest = formatXmlHeader(formattedRequest)
            formattedRequest = formatRequestMessageId(formattedRequest, messageId)

            return formattedRequest
        }

        /**
         * Validate and format netconf message. - NC1.0 if no EOM sequence present on `message`,
         * append. - NC1.1 chunk-encode given message unless it already is chunk encoded
         *
         * @param deviceCapabilities Set containing Device Capabilities
         * @param message to format
         * @return formatted message
         */
        fun formatNetconfMessage(deviceCapabilities: Set<String>, message: String): String {
            return when {
                deviceCapabilities.contains(RpcMessageUtils.NETCONF_1_1_CAPABILITY) -> formatChunkedMessage(message)
                !message.endsWith(RpcMessageUtils.END_PATTERN) ->  message + NEW_LINE + RpcMessageUtils.END_PATTERN
                else -> message
            }
        }

        /**
         * Validate and format message according to chunked framing mechanism.
         *
         * @param message to format
         * @return formated message
         */
        fun formatChunkedMessage(message: String): String {
            var formattedMessage = message
            if (formattedMessage.endsWith(RpcMessageUtils.END_PATTERN)) {
                // message given had Netconf 1.0 EOM pattern -> remove
                formattedMessage = formattedMessage.substring(0, formattedMessage.length - RpcMessageUtils.END_PATTERN.length)
            }
            if (!formattedMessage.startsWith(RpcMessageUtils.NEW_LINE + RpcMessageUtils.HASH)) {
                // chunk encode message
                formattedMessage = RpcMessageUtils.NEW_LINE + RpcMessageUtils.HASH +
                    formattedMessage.toByteArray(UTF_8).size + RpcMessageUtils.NEW_LINE +
                    formattedMessage + RpcMessageUtils.NEW_LINE + RpcMessageUtils.HASH +
                    RpcMessageUtils.HASH + RpcMessageUtils.NEW_LINE
            }
            return formattedMessage
        }

        /**
         * Ensures xml start directive/declaration appears in the `request`.
         *
         * @param request RPC request message
         * @return XML RPC message
         */
        fun formatXmlHeader(request: String): String {
            return if (!request.contains(RpcMessageUtils.XML_HEADER)) {
                when {
                    request.startsWith(RpcMessageUtils.NEW_LINE + RpcMessageUtils.HASH) -> {
                        val blah = request.split("<".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                        blah + RpcMessageUtils.XML_HEADER + request.substring(blah.length)
                    }
                    else -> RpcMessageUtils.XML_HEADER + "\n" + request
                }
            } else request
        }

        fun formatRequestMessageId(request: String, messageId: String): String {
            val formattedRequest = when {
                request.contains(RpcMessageUtils.MESSAGE_ID_STRING) ->
                    request.replaceFirst((RpcMessageUtils.MESSAGE_ID_STRING + RpcMessageUtils.EQUAL + RpcMessageUtils.NUMBER_BETWEEN_QUOTES_MATCHER).toRegex(),
                        RpcMessageUtils.MESSAGE_ID_STRING + RpcMessageUtils.EQUAL + RpcMessageUtils.QUOTE + messageId + RpcMessageUtils.QUOTE)
                !request.contains(RpcMessageUtils.HELLO) -> request.replaceFirst(
                    RpcMessageUtils.END_OF_RPC_OPEN_TAG.toRegex(),
                    RpcMessageUtils.QUOTE_SPACE + RpcMessageUtils.MESSAGE_ID_STRING + RpcMessageUtils.EQUAL + RpcMessageUtils.QUOTE + messageId + RpcMessageUtils.QUOTE + ">")
                else -> request
            }
            return updateRequestLength(formattedRequest)
        }

        fun updateRequestLength(request: String): String {
            if (request.contains(NEW_LINE + RpcMessageUtils.HASH + RpcMessageUtils.HASH + NEW_LINE)) {
                val oldLen =
                    Integer.parseInt(request.split(RpcMessageUtils.HASH.toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[1].split(
                        NEW_LINE.toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0])
                val rpcWithEnding = request.substring(request.indexOf('<'))
                val firstBlock =
                    request.split(RpcMessageUtils.MSGLEN_REGEX_PATTERN.toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[1].split(
                        (NEW_LINE + RpcMessageUtils.HASH + RpcMessageUtils.HASH + NEW_LINE).toRegex()).dropLastWhile(
                        { it.isEmpty() }).toTypedArray()[0]
                var newLen = 0
                newLen = firstBlock.toByteArray(UTF_8).size
                if (oldLen != newLen) {
                    return NEW_LINE + RpcMessageUtils.HASH + newLen + NEW_LINE + rpcWithEnding
                }
            }
            return request
        }

        fun checkReply(reply: String?): Boolean {
            return if (reply != null) {
                //TODO: this is really not rpc-error function. maybe !rpc-error && (warning || ok) ?
                !reply.contains("rpc-error>") || reply.contains("warning") || reply.contains("ok/>")
            } else false
        }

        /**
         * Extract the Netconf Session ID from the server HELLO message.
         * used by exchangeHelloMessage
         * @return SessionID as {@link String} or null on error.
         */
        fun extractNetconfSessionIdFromHelloResponse(response: String) :String? {
            val sessionIDMatcher = SESSION_ID_REGEX_PATTERN.matcher(response)
            return when {
                sessionIDMatcher.find() -> sessionIDMatcher.group(1)
                else -> null
            }
        }
    }

}