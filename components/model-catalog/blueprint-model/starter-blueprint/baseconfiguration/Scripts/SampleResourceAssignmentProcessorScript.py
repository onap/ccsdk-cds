from resource_assignment_processor_function import JythonComponentFunction
from blueprint_constants import *


class SampleJythonComponentNode(JythonComponentFunction):

    def process(self, execution_request):
        print "Processing calling.." + PROPERTY_BLUEPRINT_BASE_PATH
        return None

    def recover(self, runtime_exception, execution_request):
        print "Recovering calling.." + PROPERTY_BLUEPRINT_BASE_PATH
        return None
