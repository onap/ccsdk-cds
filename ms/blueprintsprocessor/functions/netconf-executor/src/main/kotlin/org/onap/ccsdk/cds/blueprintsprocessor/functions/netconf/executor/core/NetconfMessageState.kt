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

/**
 * State machine for the Netconf message parser
 */
internal enum class NetconfMessageState {
    NO_MATCHING_PATTERN {
        override fun evaluateChar(c: Char): NetconfMessageState {
            return when (c) {
                ']' -> FIRST_BRACKET
                '\n' -> FIRST_LF
                else -> this
            }
        }
    },
    FIRST_BRACKET {
        override fun evaluateChar(c: Char): NetconfMessageState {
            return when (c) {
                ']' -> SECOND_BRACKET
                else -> NO_MATCHING_PATTERN
            }
        }
    },
    SECOND_BRACKET {
        override fun evaluateChar(c: Char): NetconfMessageState {
            return when (c) {
                '>' -> FIRST_BIGGER
                else -> NO_MATCHING_PATTERN
            }
        }
    },
    FIRST_BIGGER {
        override fun evaluateChar(c: Char): NetconfMessageState {
            return when (c) {
                ']' -> THIRD_BRACKET
                else -> NO_MATCHING_PATTERN
            }
        }
    },
    THIRD_BRACKET {
        override fun evaluateChar(c: Char): NetconfMessageState {
            return when (c) {
                '\n' -> ENDING_BIGGER
                else -> NO_MATCHING_PATTERN
            }
        }
    },
    ENDING_BIGGER {
        override fun evaluateChar(c: Char): NetconfMessageState {
            return when (c) {
                '>' -> END_PATTERN
                else -> NO_MATCHING_PATTERN
            }
        }
    },
    FIRST_LF {
        override fun evaluateChar(c: Char): NetconfMessageState {
            return when (c) {
                '#' -> FIRST_HASH
                ']' -> FIRST_BRACKET
                '\n' -> this
                else -> NO_MATCHING_PATTERN
            }
        }
    },
    FIRST_HASH {
        override fun evaluateChar(c: Char): NetconfMessageState {
            return when (c) {
                '#' -> SECOND_HASH
                else -> NO_MATCHING_PATTERN
            }
        }
    },
    SECOND_HASH {
        override fun evaluateChar(c: Char): NetconfMessageState {
            return when (c) {
                '\n' -> END_CHUNKED_PATTERN
                else -> NO_MATCHING_PATTERN
            }
        }
    },
    END_CHUNKED_PATTERN {
        override fun evaluateChar(c: Char): NetconfMessageState {
            return NO_MATCHING_PATTERN
        }
    },
    END_PATTERN {
        override fun evaluateChar(c: Char): NetconfMessageState {
            return NO_MATCHING_PATTERN
        }
    };

    /**
     * Evaluate next transition state based on current state and the character read
     * @param c character read in
     * @return result of lookup of transition to the next {@link NetconfMessageState}
     */
    internal abstract fun evaluateChar(c: Char): NetconfMessageState
}
