from abstract_blueprint_function import AbstractPythonComponentFunction
from blueprint_constants import *


class SampleBlueprintComponent(AbstractPythonComponentFunction):

    def __init__(self):
        AbstractPythonComponentFunction.__init__(self)

    def process(self, execution_request):
        super(SamplePythonComponentNode, self).process(execution_request)
        print "Processing calling..." + PROPERTY_BLUEPRINT_BASE_PATH
        return None

    def recover(self, runtime_exception, execution_request):
        super(SamplePythonComponentNode, self).recover(runtime_exception, execution_request)
        print "Recovering calling..." + PROPERTY_BLUEPRINT_BASE_PATH
        return None
