from abstract_ra_processor import AbstractRAProcessor
from blueprint_constants import *


class SampleRAProcessor(AbstractRAProcessor):

    def process(self, execution_request):
        print "Processing calling.." + PROPERTY_BLUEPRINT_BASE_PATH
        return None

    def recover(self, runtime_exception, execution_request):
        print "Recovering calling.." + PROPERTY_BLUEPRINT_BASE_PATH
        return None
