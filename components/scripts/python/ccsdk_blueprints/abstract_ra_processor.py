from org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.processor import ResourceAssignmentProcessor


class AbstractRAProcessor(ResourceAssignmentProcessor):

    def process(self, execution_request):
        print "Processing calling.."
        return None

    def recover(self, runtime_exception, execution_request):
        print "Recovering calling.."
        return None
