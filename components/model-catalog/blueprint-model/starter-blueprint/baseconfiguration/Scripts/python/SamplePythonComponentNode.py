from abstract_blueprint_function import AbstractPythonComponentFunction
from blueprint_constants import *

class SamplePythonComponentNode(AbstractPythonComponentFunction):

    def process(self, execution_request):
        print "Processing calling.." + PROPERTY_BLUEPRINT_BASE_PATH
        return None

    def recover(self, runtime_exception, execution_request):
        print "Recovering calling.." + PROPERTY_BLUEPRINT_BASE_PATH
        return None
