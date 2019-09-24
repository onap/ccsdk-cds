package org.onap.ccsdk.cds.controllerblueprints.processing.api

import kotlin.Unit
import kotlin.jvm.JvmName
import kotlinx.coroutines.channels.SendChannel

@JvmName("sendExecutionServiceInput")
suspend inline fun SendChannel<ExecutionServiceInput>.send(block:
        ExecutionServiceInput.Builder.() -> Unit) {
    val request = ExecutionServiceInput.newBuilder()
        .apply(block)
        .build()
    send(request)
}
