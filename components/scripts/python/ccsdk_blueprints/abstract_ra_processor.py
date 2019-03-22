from org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.processor import \
    ResourceAssignmentProcessor
from org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.utils import \
    ResourceAssignmentUtils
from org.onap.ccsdk.apps.controllerblueprints.core import \
    BluePrintProcessorException


class AbstractRAProcessor(ResourceAssignmentProcessor):

    def process(self, resource_assignment):
        print "Processing.."
        return None

    def recover(self, runtime_exception, resource_assignment):
        print "Recovering.."
        return None

    def set_resource_data_value(self, resource_assignment, value):
        try:
            if value is not None:
                ResourceAssignmentUtils.Companion.setResourceDataValue(
                    resource_assignment, self.raRuntimeService, value)
            else:
                ResourceAssignmentUtils.Companion.setFailedResourceDataValue(
                    resource_assignment, "Fail to resolve value")
        except BluePrintProcessorException, err:
            raise BluePrintProcessorException(
                "Error on resource assignment. Message = " + err.message)
