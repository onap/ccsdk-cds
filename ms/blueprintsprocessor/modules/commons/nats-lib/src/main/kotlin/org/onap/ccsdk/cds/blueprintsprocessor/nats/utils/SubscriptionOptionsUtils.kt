/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.nats.utils

import io.nats.streaming.SubscriptionOptions

object SubscriptionOptionsUtils {

    /** Subscribe with a durable [name] and client can re subscribe with  durable [name] */
    fun durable(name: String): SubscriptionOptions {
        return SubscriptionOptions.Builder().durableName(name).build()
    }

    /** Subscribe with manual ack mode and a max in-flight [limit] */
    fun manualAckWithRateLimit(limit: Int): SubscriptionOptions {
        return SubscriptionOptions.Builder().manualAcks().maxInFlight(limit).build()
    }
}
