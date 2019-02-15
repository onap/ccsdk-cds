from abstract_blueprint_function import AbstractPythonComponentFunction
from blueprint_constants import *


class SamplePythonComponentNode(AbstractPythonComponentFunction):

    def __init__(self):
        AbstractPythonComponentFunction.__init__(self)

    def process(self, execution_request):
        AbstractPythonComponentFunction.process(self, execution_request)
        print "Processing calling..." + PROPERTY_BLUEPRINT_BASE_PATH
        return None

    def recover(self, runtime_exception, execution_request):
        AbstractPythonComponentFunction.recover(self, runtime_exception, execution_request)
        print "Recovering calling..." + PROPERTY_BLUEPRINT_BASE_PATH
        return None
