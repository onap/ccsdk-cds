from org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.processor import ResourceAssignmentProcessor
from blueprint_constants import *
from org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.utils import ResourceAssignmentUtils
from org.onap.ccsdk.apps.controllerblueprints.core import BluePrintProcessorException
from java.lang import Exception


class AbstractRAProcessor(ResourceAssignmentProcessor):

    def __init__(self):
        ResourceAssignmentProcessor.__init__(self)
        self.status = PROPERTY_BLUEPRINT_STATUS_SUCCESS
        self.error_message = None
        self.ra_valid = False
        self.value_to_resolve = None

    def process(self, execution_request):
        print "Processing calling from parent..."
        try:
            self.ra_valid = self.validate(execution_request)
            self.value_to_resolve = execution_request.name
        except Exception, e:
            self.status = PROPERTY_BLUEPRINT_STATUS_FAILURE
            self.error_message = "Get Running python scripting Failure :" + e.getMessage()

    def recover(self, runtime_exception, execution_request):
        print "Recovering calling from parent.."
        return None

    def set_resource_data_value(self, execution_request, value):
        try:
            if value is not None:
                ResourceAssignmentUtils.Companion.setResourceDataValue(execution_request, self.raRuntimeService, value)
            else:
                ResourceAssignmentUtils.Companion.setFailedResourceDataValue(execution_request, "Fail to resole value")
        except BluePrintProcessorException, err:
            raise BluePrintProcessorException("Error on resource assignment. Message = " + err.message)

        return None

    @staticmethod
    def validate(ra):
        if ra.name is None or ra.name is None:
            raise Exception("Failed getting value for template key (" + ra.name + ") and " +
                            "dictionary key (" + ra.dictionaryName +
                            ") of type (" + ra.type + ")")
        else:
            pass
        return True
