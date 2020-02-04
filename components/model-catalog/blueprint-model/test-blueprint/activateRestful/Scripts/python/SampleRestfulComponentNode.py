from org.onap.ccsdk.cds.blueprintsprocessor.functions.restful.executor import RestfulCMComponentFunction
from blueprint_constants import *


class SampleRestfulComponentNode(RestfulCMComponentFunction):

    def process(self, execution_request):
        print "Processing calling..." + PROPERTY_BLUEPRINT_BASE_PATH
        return None

    def recover(self, runtime_exception, execution_request):
        print "Recovering calling..." + PROPERTY_BLUEPRINT_BASE_PATH
        return None
