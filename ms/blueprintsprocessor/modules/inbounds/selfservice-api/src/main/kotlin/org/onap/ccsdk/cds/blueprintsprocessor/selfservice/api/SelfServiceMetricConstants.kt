package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api

object SelfServiceMetricConstants {

    private const val METRICS_PREFIX = "cds.cba"

    private const val PROCESS_PREFIX = "$METRICS_PREFIX.process"

    // TAGS
    const val TAG_BP_NAME = "blueprint_name"
    const val TAG_BP_VERSION = "blueprint_version"
    const val TAG_BP_ACTION = "blueprint_action"
    const val TAG_BP_STATUS = "status"
    const val TAG_BP_OUTCOME = "outcome"

    // COUNTERS
    const val COUNTER_PROCESS = "$PROCESS_PREFIX.counter"

    // TIMERS
    const val TIMER_PROCESS = "$PROCESS_PREFIX.timer"
}
