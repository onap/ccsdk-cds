package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api

import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ResourceModelIdType
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentMap

@Service
class CDSRequestLockingManager {
    companion object CDSRequestLockingManager {
        private val queues: ConcurrentMap<ResourceModelIdType, ConcurrentLinkedQueue<ExecutionServiceInput>> = ConcurrentHashMap()
    }

    private val log = logger(CDSRequestLockingManager::class)

    // enum to communicate whether the caller of enqueue should keep polling or the execution can start right away...
    // CAN_PROCESS indicates that the incoming CDS request had resourceID/TYPE specified and there were no other elements in the queue with that composite key
    // ENQUEUED indicates that the incoming CDS request had resourceID/TYPE specified and we are already processing a cds request with that composite key
    // NO_QUEUEING_NEEDED indicates that the incoming CDS request did not have resourceID/TYPE specified, hence no queueing is expected by the caller.
    // INVALID_KEY specifies the input CDS request either specified resource-id/type
    enum class EnqueueStatus { CAN_PROCESS, ENQUEUED, NO_QUEUEING_NEEDED, INVALID_KEY }

    // enqueue ExecutionServiceInput (CDS request)
    // CDS requests are enqueued by resource-id/resource-type composite key
    @Synchronized
    fun canExecutionStartOrEnqueued(serviceInput: ExecutionServiceInput): EnqueueStatus {
        val commonHeader = serviceInput.commonHeader
        val resId: String? = commonHeader.resourceModelId
        val resType: String? = commonHeader.resourceModelType
        log.info("canExecutionStartOrEnqueued resID/Type: ($resId/$resType)")

        // check if no resourceID/TYPE specified.
        if (resId == null && resType == null) return EnqueueStatus.NO_QUEUEING_NEEDED
        else if (resId == null && resType != null || resId != null && resType == null) return EnqueueStatus.INVALID_KEY
        val qKey = ResourceModelIdType(resId!!, resType!!) // composite key
        val queueByKey = queues.getOrPut(qKey) {
            log.info("Creating a new queue for resourceModelId/Type ($resId/$resType)")
            ConcurrentLinkedQueue<ExecutionServiceInput>()
        }
        // check if the queue contains any elements.
        val enqueueStatus = if (queueByKey.isEmpty()) EnqueueStatus.CAN_PROCESS else EnqueueStatus.ENQUEUED
        // add current execution service input to the queue, it is the responsibility of the caller to then remove
        log.info("Adding requestId (${serviceInput.commonHeader.requestId}) with resourceModelId/Type ($resId/$resType) to the queue. Queue execution status ($enqueueStatus). #elements in the queue ${queueByKey.size}")
        queueByKey.add(serviceInput)
        return enqueueStatus
    }

    @Synchronized
    // Note: there is an implicit assumption that if an item has gotten into one of the queues, then resourceId/Type are not blank or erroneous.
    // returns true if for a given composite key, the head of the queue matches the specified serviceInput (CDS request)
    fun isCurrentServiceInputFirstInLine(serviceInput: ExecutionServiceInput): Boolean {
        val commonHeader = serviceInput.commonHeader
        val qKey = ResourceModelIdType(commonHeader.resourceModelId!!, commonHeader.resourceModelType!!) // composite key
        val queueByKey = queues[qKey]
        if (queueByKey != null)
            return queueByKey.peek() == serviceInput
        throw BluePrintProcessorException("Expected to get a queue for resourceModelId/Type $qKey but return was null. Concurrency bugs?!")
    }

    fun remove(execServiceInput: ExecutionServiceInput) {
        val commonHeader = execServiceInput.commonHeader
        val reqId = commonHeader.requestId
        val qKey = ResourceModelIdType(commonHeader.resourceModelId!!, commonHeader.resourceModelType!!) // composite key
        log.info("Removing requestId ($reqId) for $qKey")
        queues[qKey]?.let {
            val queueHead = it.peek()
            if (queueHead != execServiceInput) {
                log.error("Error: Expected to find requestId $reqId as the head of the request queue. Will attempt to remove it from the queue anyways...")
                if (it.remove(execServiceInput)) {
                    log.error("...... removed the matching request $reqId with $qKey")
                } else {
                    log.error("...... could not find the requestId $reqId with $qKey")
                    // TODO: maybe throw exception here....
                }
            } else {
                log.info("request ($reqId) for key ($qKey) is the first element in the queue, and is getting removed...")
                it.remove()
            }
        }
    }
}

