package org.onap.ccsdk.cds.blueprintsprocessor.uat.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.sift.AbstractDiscriminator
import org.onap.ccsdk.cds.blueprintsprocessor.uat.logging.ColorMarker
import org.onap.ccsdk.cds.blueprintsprocessor.uat.logging.LogColor.MDC_COLOR_KEY

class SmartColorDiscriminator : AbstractDiscriminator<ILoggingEvent>() {

    var defaultValue: String = "white"

    override fun getKey(): String {
        return MDC_COLOR_KEY
    }

    fun setKey() {
        throw UnsupportedOperationException("Key not settable. Using $MDC_COLOR_KEY")
    }

    override fun getDiscriminatingValue(e: ILoggingEvent): String =
        (e.marker as? ColorMarker)?.name
            ?: e.mdcPropertyMap?.get(MDC_COLOR_KEY)
            ?: defaultValue
}
