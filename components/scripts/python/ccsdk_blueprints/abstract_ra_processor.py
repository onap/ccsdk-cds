from org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor import \
    ResourceAssignmentProcessor
from org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils import \
    ResourceAssignmentUtils
from org.onap.ccsdk.cds.controllerblueprints.core import \
    BlueprintProcessorException


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
        except BlueprintProcessorException, err:
            raise BlueprintProcessorException(
                "Error on resource assignment. Message = " + err.message)
