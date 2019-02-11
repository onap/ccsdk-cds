/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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
package org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.utils

import org.apache.commons.lang3.StringUtils
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.NetconfException
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.data.NetconfAdaptorConstant
import org.slf4j.LoggerFactory
import org.xml.sax.InputSource
import java.io.StringReader
import java.nio.charset.StandardCharsets
import java.util.Optional
import java.util.regex.MatchResult
import java.util.regex.Pattern
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.collections.ArrayList
import kotlin.text.Charsets.UTF_8


class RpcMessageUtils {

    companion object {
        val log = LoggerFactory.getLogger(RpcMessageUtils::class.java)
        // pattern to verify whole Chunked-Message format
        val CHUNKED_FRAMING_PATTERN = Pattern.compile("(\\n#([1-9][0-9]*)\\n(.+))+\\n##\\n", Pattern.DOTALL)
        val CHUNKED_END_REGEX_PATTERN = "\n##\n"
        // pattern to parse each chunk-size in ChunkedMessage chunk
        val CHUNKED_SIZE_PATTERN = Pattern.compile("\\n#([1-9][0-9]*)\\n")
        val CAPABILITY_REGEX_PATTERN = Pattern.compile(RpcConstants.CAPABILITY_REGEX)
        val SESSION_ID_REGEX_PATTERN = Pattern.compile(RpcConstants.SESSION_ID_REGEX)
        val MSGID_STRING_PATTERN = Pattern.compile("${RpcConstants.MESSAGE_ID_STRING}=\"(.*?)\"")
        val NEW_LINE = "\n"

        fun getConfig(messageId: String, configType: String, filterContent: String?): String {
            val request = StringBuilder()

            request.append("<get-config>").append(NEW_LINE)
            request.append(RpcConstants.SOURCE_OPEN).append(NEW_LINE)
            request.append(RpcConstants.OPEN).append(configType).append(RpcConstants.TAG_CLOSE).append(NEW_LINE)
            request.append(RpcConstants.SOURCE_CLOSE).append(NEW_LINE)

            if (filterContent != null) {
                request.append(RpcConstants.SUBTREE_FILTER_OPEN).append(NEW_LINE)
                request.append(filterContent).append(NEW_LINE)
                request.append(RpcConstants.SUBTREE_FILTER_CLOSE).append(NEW_LINE)
            }
            request.append("</get-config>").append(NEW_LINE)

            return doWrappedRpc(messageId, request.toString())
        }

        fun doWrappedRpc(messageId: String, request: String): String {
            val rpc = StringBuilder(RpcConstants.XML_HEADER).append(NEW_LINE)
            rpc.append(RpcConstants.RPC_OPEN)
            rpc.append(RpcConstants.MESSAGE_ID_STRING).append(RpcConstants.EQUAL)
            rpc.append(RpcConstants.QUOTE).append(messageId).append(RpcConstants.QUOTE_SPACE)
            rpc.append(RpcConstants.NETCONF_BASE_NAMESPACE).append(RpcConstants.CLOSE).append(NEW_LINE)
            rpc.append(request)
            rpc.append(RpcConstants.RPC_CLOSE)
            // rpc.append(NEW_LINE).append(END_PATTERN);

            return rpc.toString()
        }

        fun editConfig(messageId: String, configType: String, defaultOperation: String?,
                       newConfiguration: String): String {

            val request = StringBuilder()

            request.append("<edit-config>").append(NEW_LINE)
            request.append(RpcConstants.TARGET_OPEN).append(NEW_LINE)
            request.append(RpcConstants.OPEN).append(configType).append(RpcConstants.TAG_CLOSE).append(NEW_LINE)
            request.append(RpcConstants.TARGET_CLOSE).append(NEW_LINE)

            if (defaultOperation != null) {
                request.append(RpcConstants.DEFAULT_OPERATION_OPEN).append(defaultOperation).append(RpcConstants.DEFAULT_OPERATION_CLOSE)
                request.append(NEW_LINE)
            }

            request.append(RpcConstants.CONFIG_OPEN).append(NEW_LINE)
            request.append(newConfiguration.trim { it <= ' ' }).append(NEW_LINE)
            request.append(RpcConstants.CONFIG_CLOSE).append(NEW_LINE)
            request.append("</edit-config>").append(NEW_LINE)

            return doWrappedRpc(messageId, request.toString())
        }

        fun validate(messageId: String, configType: String): String {
            val request = StringBuilder()

            request.append("<validate>").append(NEW_LINE)
            request.append(RpcConstants.SOURCE_OPEN).append(NEW_LINE)
            request.append(RpcConstants.OPEN).append(configType).append(RpcConstants.TAG_CLOSE).append(NEW_LINE)
            request.append(RpcConstants.SOURCE_CLOSE).append(NEW_LINE)
            request.append("</validate>").append(NEW_LINE)

            return doWrappedRpc(messageId, request.toString())
        }

        fun commit(messageId: String, message: String): String {
            val request = StringBuilder()

            request.append("<commit>").append(NEW_LINE)
            request.append("</commit>").append(NEW_LINE)

            return doWrappedRpc(messageId, request.toString())
        }


        fun unlock(messageId: String, configType: String): String {
            val request = StringBuilder()

            request.append("<unlock>").append(NEW_LINE)
            request.append(RpcConstants.TARGET_OPEN).append(NEW_LINE)
            request.append(RpcConstants.OPEN).append(configType).append(RpcConstants.TAG_CLOSE).append(NEW_LINE)
            request.append(RpcConstants.TARGET_CLOSE).append(NEW_LINE)
            request.append("</unlock>").append(NEW_LINE)

            return doWrappedRpc(messageId, request.toString())
        }

        @Throws(NetconfException::class)
        fun deleteConfig(messageId: String, netconfTargetConfig: String): String {
            if (netconfTargetConfig == NetconfAdaptorConstant.CONFIG_TARGET_RUNNING) {
                log.warn("Target configuration for delete operation can't be \"running\" {}", netconfTargetConfig)
                throw NetconfException("Target configuration for delete operation can't be running")
            }

            val request = StringBuilder()

            request.append("<delete-config>").append(NEW_LINE)
            request.append(RpcConstants.TARGET_OPEN).append(NEW_LINE)
            request.append(RpcConstants.OPEN).append(netconfTargetConfig).append(RpcConstants.TAG_CLOSE).append(NEW_LINE)
            request.append(RpcConstants.TARGET_CLOSE).append(NEW_LINE)
            request.append("</delete-config>").append(NEW_LINE)

            return doWrappedRpc(messageId, request.toString())
        }

        fun discardChanges(messageId: String): String {
            val request = StringBuilder()
            request.append("<discard-changes/>").append(NEW_LINE)
            return doWrappedRpc(messageId, request.toString())
        }

        fun lock(messageId: String, configType: String): String {
            val request = StringBuilder()

            request.append("<lock>").append(NEW_LINE)
            request.append(RpcConstants.TARGET_OPEN).append(NEW_LINE)
            request.append(RpcConstants.OPEN).append(configType).append(RpcConstants.TAG_CLOSE).append(NEW_LINE)
            request.append(RpcConstants.TARGET_CLOSE).append(NEW_LINE)
            request.append("</lock>").append(NEW_LINE)

            return doWrappedRpc(messageId, request.toString())
        }

        fun closeSession(messageId: String, force: Boolean): String {
            val request = StringBuilder()

            if (force) {
                request.append("<kill-session/>").append(NEW_LINE)
            } else {
                request.append("<close-session/>").append(NEW_LINE)
            }

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
                dbf.newDocumentBuilder().parse(InputSource(StringReader(rpcRequest.replace(RpcConstants.END_PATTERN, ""))))
                return true
            } catch (e: Exception) {
                return false
            }

        }

        fun getMsgId(message: String): Optional<String> {
            val matcher = MSGID_STRING_PATTERN.matcher(message)
            if (matcher.find()) {
                return Optional.of(matcher.group(1))
            }
            return if (message.contains(RpcConstants.HELLO)) {
                Optional.of((-1).toString())
            } else Optional.empty()
        }

        fun validateChunkedFraming(reply: String): Boolean {
            val matcher = CHUNKED_FRAMING_PATTERN.matcher(reply)
            if (!matcher.matches()) {
                log.debug("Error Reply: {}", reply)
                return false
            }
            var chunkM = CHUNKED_SIZE_PATTERN.matcher(reply)
            var chunks = ArrayList<MatchResult>()
            var chunkdataStr = ""
            while (chunkM.find()) {
                chunks.add(chunkM.toMatchResult())
                // extract chunk-data (and later) in bytes
                val bytes = Integer.parseInt(chunkM.group(1))
                // var chunkdata = reply.substring(chunkM.end()).getBytes(StandardCharsets.UTF_8)
                var chunkdata = reply.substring(chunkM.end()).toByteArray(StandardCharsets.UTF_8)
                if (bytes > chunkdata.size) {
                    log.debug("Error Reply - wrong chunk size {}", reply)
                    return false
                }
                // convert (only) chunk-data part into String

                chunkdataStr = String(chunkdata, 0, bytes, StandardCharsets.UTF_8)
                // skip chunk-data part from next match
                chunkM.region(chunkM.end() + chunkdataStr.length, reply.length)
            }
            if (!CHUNKED_END_REGEX_PATTERN
                            .equals(reply.substring(chunks[chunks.size - 1].end() + chunkdataStr.length))) {
                log.debug("Error Reply: {}", reply)
                return false
            }
            return true
        }


        fun createHelloString(capabilities: List<String>): String {
            val hellobuffer = StringBuilder()
            hellobuffer.append(RpcConstants.XML_HEADER).append(NEW_LINE)
            hellobuffer.append("<hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">").append(NEW_LINE)
            hellobuffer.append("  <capabilities>").append(NEW_LINE)
            if (capabilities.isNotEmpty()) {
                capabilities.forEach { cap -> hellobuffer.append("    <capability>").append(cap).append("</capability>").append(NEW_LINE) }
            }
            hellobuffer.append("  </capabilities>").append(NEW_LINE)
            hellobuffer.append("</hello>").append(NEW_LINE)
            hellobuffer.append(RpcConstants.END_PATTERN)
            return hellobuffer.toString()
        }
        fun formatRPCRequest(request: String, messageId: String, deviceCapabilities: Set<String>): String {
            var request = request
            request = RpcMessageUtils.formatNetconfMessage(deviceCapabilities, request)
            request = RpcMessageUtils.formatXmlHeader(request)
            request = RpcMessageUtils.formatRequestMessageId(request, messageId)

            return request
        }

        /**
         * Validate and format netconf message. - NC1.0 if no EOM sequence present on `message`,
         * append. - NC1.1 chunk-encode given message unless it already is chunk encoded
         *
         * @param deviceCapabilities Set containing Device Capabilities
         * @param message to format
         * @return formated message
         */
        fun formatNetconfMessage(deviceCapabilities: Set<String>, message: String): String {
            var message = message
            if (deviceCapabilities.contains(RpcConstants.NETCONF_11_CAPABILITY)) {
                message = formatChunkedMessage(message)
            } else if (!message.endsWith(RpcConstants.END_PATTERN)) {
                message = message + NEW_LINE + RpcConstants.END_PATTERN
            }
            return message
        }

        /**
         * Validate and format message according to chunked framing mechanism.
         *
         * @param message to format
         * @return formated message
         */
        fun formatChunkedMessage(message: String): String {
            var message = message
            if (message.endsWith(RpcConstants.END_PATTERN)) {
                // message given had Netconf 1.0 EOM pattern -> remove
                message = message.substring(0, message.length - RpcConstants.END_PATTERN.length)
            }
            if (!message.startsWith(RpcConstants.NEW_LINE + RpcConstants.HASH)) {
                // chunk encode message
                //message = (RpcConstants.NEW_LINE + RpcConstants.HASH + message.getBytes(UTF_8).size + RpcConstants.NEW_LINE + message +RpcConstants. NEW_LINE + RpcConstants.HASH + RpcConstants.HASH
                 //       + RpcConstants.NEW_LINE)
                message = (RpcConstants.NEW_LINE + RpcConstants.HASH + message.toByteArray(UTF_8).size + RpcConstants.NEW_LINE + message +RpcConstants. NEW_LINE + RpcConstants.HASH + RpcConstants.HASH
                      + RpcConstants.NEW_LINE)
            }
            return message
        }

        /**
         * Ensures xml start directive/declaration appears in the `request`.
         *
         * @param request RPC request message
         * @return XML RPC message
         */
        fun formatXmlHeader(request: String): String {
            var request = request
            if (!request.contains(RpcConstants.XML_HEADER)) {
                if (request.startsWith(RpcConstants.NEW_LINE + RpcConstants.HASH)) {
                    request = request.split("<".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] + RpcConstants.XML_HEADER + request.substring(request.split("<".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].length)
                } else {
                    request = RpcConstants.XML_HEADER + "\n" + request
                }
            }
            return request
        }

        fun formatRequestMessageId(request: String, messageId: String): String {
            var request = request
            if (request.contains(RpcConstants.MESSAGE_ID_STRING)) {
                request = request.replaceFirst((RpcConstants.MESSAGE_ID_STRING + RpcConstants.EQUAL + RpcConstants.NUMBER_BETWEEN_QUOTES_MATCHER).toRegex(), RpcConstants.MESSAGE_ID_STRING +RpcConstants. EQUAL + RpcConstants.QUOTE + messageId + RpcConstants.QUOTE)
            } else if (!request.contains(RpcConstants.MESSAGE_ID_STRING) && !request.contains(RpcConstants.HELLO)) {
                request = request.replaceFirst(RpcConstants.END_OF_RPC_OPEN_TAG.toRegex(), RpcConstants.QUOTE_SPACE + RpcConstants.MESSAGE_ID_STRING + RpcConstants.EQUAL + RpcConstants.QUOTE + messageId + RpcConstants.QUOTE + ">")
            }
            return updateRequestLength(request)
        }

        fun updateRequestLength(request: String): String {
            if (request.contains(NEW_LINE + RpcConstants.HASH + RpcConstants.HASH + NEW_LINE)) {
                val oldLen = Integer.parseInt(request.split(RpcConstants.HASH.toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[1].split(NEW_LINE.toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0])
                val rpcWithEnding = request.substring(request.indexOf('<'))
                val firstBlock = request.split(RpcConstants.MSGLEN_REGEX_PATTERN.toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[1].split((NEW_LINE + RpcConstants.HASH +RpcConstants. HASH + NEW_LINE).toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0]
                var newLen = 0
                newLen = firstBlock.toByteArray(UTF_8).size
                if (oldLen != newLen) {
                    return NEW_LINE + RpcConstants.HASH + newLen + NEW_LINE + rpcWithEnding
                }
            }
            return request
        }

        fun checkReply(reply: String?): Boolean {
            return if (reply != null) {
                !reply.contains("<rpc-error>") || reply.contains("warning") || reply.contains("<ok/>")
            } else false
        }


    }

}