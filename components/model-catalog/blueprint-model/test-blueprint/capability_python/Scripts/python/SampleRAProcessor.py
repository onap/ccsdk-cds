from abstract_ra_processor import AbstractRAProcessor
from blueprint_constants import *


class SampleRAProcessor(AbstractRAProcessor):

    def __init__(self):
        AbstractRAProcessor.__init__(self)

    def process(self, resource_assignment):
        print "Processing calling.." + PROPERTY_BLUEPRINT_BASE_PATH
        self.set_resource_data_value(resource_assignment, "")
        return None

    def recover(self, runtime_exception, resource_assignment):
        print "Recovering calling.." + PROPERTY_BLUEPRINT_BASE_PATH
        return None
