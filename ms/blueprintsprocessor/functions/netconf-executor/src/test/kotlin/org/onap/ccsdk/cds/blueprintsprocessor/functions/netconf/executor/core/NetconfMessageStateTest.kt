/*
 * Copyright © 2019 Bell Canada
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.core

import org.junit.Test
import kotlin.test.assertEquals
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.core.NetconfDeviceCommunicator.NetconfMessageState
class NetconfMessageStateTest {

    private val charList: List<Char> = Char.MIN_VALUE.toInt().rangeTo(Char.MAX_VALUE.toInt())
        .map { it -> it.toChar() }

    @Test
    fun `NO_MATCHING_PATTERN transitions`() {
        assertEquals(NetconfMessageState.FIRST_BRACKET,
            NetconfMessageState.NO_MATCHING_PATTERN.evaluateChar(']'))
        assertEquals(NetconfMessageState.FIRST_LF,
            NetconfMessageState.NO_MATCHING_PATTERN.evaluateChar('\n'))

        charList.minus(listOf(']','\n')).forEach {
            assertEquals(NetconfMessageState.NO_MATCHING_PATTERN,
                NetconfMessageState.NO_MATCHING_PATTERN.evaluateChar(it))
        }
    }

    @Test
    fun `FIRST_BRACKET transitions`() {
        assertEquals(NetconfMessageState.SECOND_BRACKET,
            NetconfMessageState.FIRST_BRACKET.evaluateChar(']'))

        charList.minus( ']').forEach {
            assertEquals(NetconfMessageState.NO_MATCHING_PATTERN,
                NetconfMessageState.FIRST_BRACKET.evaluateChar(it))
        }
    }

    @Test
    fun `SECOND_BRACKET transitions`() {
        assertEquals(NetconfMessageState.FIRST_BIGGER,
            NetconfMessageState.SECOND_BRACKET.evaluateChar('>'))

        charList.minus('>').forEach {
            assertEquals(NetconfMessageState.NO_MATCHING_PATTERN,
                NetconfMessageState.SECOND_BRACKET.evaluateChar(it))
        }
    }

    @Test
    fun `FIRST_BIGGER transitions`() {
        assertEquals(NetconfMessageState.THIRD_BRACKET,
            NetconfMessageState.FIRST_BIGGER.evaluateChar(']'))

        charList.minus(']').forEach {
            assertEquals(NetconfMessageState.NO_MATCHING_PATTERN,
                NetconfMessageState.FIRST_BIGGER.evaluateChar(it))
        }
    }

    @Test
    fun `THIRD_BRACKET transitions`() {
        assertEquals(NetconfMessageState.ENDING_BIGGER,
            NetconfMessageState.THIRD_BRACKET.evaluateChar(']'))

        charList.minus(']').forEach {
            assertEquals(NetconfMessageState.NO_MATCHING_PATTERN,
                NetconfMessageState.THIRD_BRACKET.evaluateChar(it))
        }
    }

    @Test
    fun `ENDING_BIGGER transitions`() {
        assertEquals(NetconfMessageState.END_PATTERN,
            NetconfMessageState.ENDING_BIGGER.evaluateChar('>'))

        charList.minus('>').forEach {
            assertEquals(NetconfMessageState.NO_MATCHING_PATTERN,
                NetconfMessageState.ENDING_BIGGER.evaluateChar(it))
        }
    }

    @Test
    fun `FIRST_LF transitions`() {
        assertEquals(NetconfMessageState.FIRST_HASH,
            NetconfMessageState.FIRST_LF.evaluateChar('#'))
        assertEquals(NetconfMessageState.FIRST_BRACKET,
            NetconfMessageState.FIRST_LF.evaluateChar(']'))
        assertEquals(NetconfMessageState.FIRST_LF,
            NetconfMessageState.FIRST_LF.evaluateChar('\n'))
        charList.minus(listOf('#', ']', '\n')).forEach {
            assertEquals(NetconfMessageState.NO_MATCHING_PATTERN,
                NetconfMessageState.FIRST_LF.evaluateChar(it))
        }
    }

    @Test
    fun `FIRST_HASH transitions`() {
        assertEquals(NetconfMessageState.SECOND_HASH,
            NetconfMessageState.FIRST_HASH.evaluateChar('#'))
        charList.minus('#').forEach {
            assertEquals(NetconfMessageState.NO_MATCHING_PATTERN,
                NetconfMessageState.FIRST_HASH.evaluateChar(it))
        }
    }

    @Test
    fun `SECOND_HASH transitions`() {
        assertEquals(NetconfMessageState.END_CHUNKED_PATTERN,
            NetconfMessageState.SECOND_HASH.evaluateChar('\n'))

        charList.minus( '\n').forEach {
            assertEquals(NetconfMessageState.NO_MATCHING_PATTERN,
                NetconfMessageState.SECOND_HASH.evaluateChar(it))
        }
    }

    @Test
    fun `END_CHUNKED_PATTERN transitions`() {
        charList.forEach {
            assertEquals(NetconfMessageState.NO_MATCHING_PATTERN,
                NetconfMessageState.END_CHUNKED_PATTERN.evaluateChar(it))
        }
    }

    @Test
    fun `END_PATTERN transitions`() {
        charList.forEach {
            assertEquals(NetconfMessageState.NO_MATCHING_PATTERN,
                NetconfMessageState.END_PATTERN.evaluateChar(it))
        }
    }

}