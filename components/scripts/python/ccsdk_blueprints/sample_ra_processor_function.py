from abstract_ra_processor import AbstractRAProcessor
from blueprint_constants import *


class SampleRAProcessorFunction(AbstractRAProcessor):

    def __init__(self):
        AbstractRAProcessor.__init__(self)

    def process(self, execution_request):
        AbstractRAProcessor.process(self, execution_request)
        print "Processing calling.." + PROPERTY_BLUEPRINT_BASE_PATH
        return None

    def recover(self, runtime_exception, execution_request):
        AbstractRAProcessor.recover(self, runtime_exception, execution_request)
        print "Recovering calling.." + PROPERTY_BLUEPRINT_BASE_PATH
        return None
