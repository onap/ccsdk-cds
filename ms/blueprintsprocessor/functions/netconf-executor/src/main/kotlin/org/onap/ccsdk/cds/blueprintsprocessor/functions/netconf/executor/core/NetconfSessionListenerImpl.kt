/*
 *  Copyright (C) 2019 Amdocs, Bell Canada
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.core

import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfReceivedEvent
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfSessionListener

/**
 * Implementation of the NetconfSessionListener
 * Encapsulates logic for type of message received and action that NetconfSessionImpl should take.
 * TODO: Is there a better way to extract this out of NetconfSession, I'd like to use the NetconfSession as param,
 * rather than NetconfSessionImpl, but at the same time, addDeviceReply/ErrorReply should not be part of the public
 * interface....
 */

internal class NetconfSessionListenerImpl(private val session: NetconfSessionImpl) : NetconfSessionListener {
    override fun notify(event: NetconfReceivedEvent) {

        when (event.type) {
            NetconfReceivedEvent.Type.DEVICE_UNREGISTERED -> session.disconnect()
            NetconfReceivedEvent.Type.SESSION_CLOSED -> session.disconnect()
            NetconfReceivedEvent.Type.DEVICE_ERROR -> session.addDeviceErrorReply(event.messagePayload)
            NetconfReceivedEvent.Type.DEVICE_REPLY -> session.addDeviceReply(event.messageId, event.messagePayload)
        }
    }
}