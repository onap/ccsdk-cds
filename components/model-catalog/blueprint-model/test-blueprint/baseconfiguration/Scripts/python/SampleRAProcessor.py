from abstract_ra_processor import AbstractRAProcessor
from blueprint_constants import *


class SampleRAProcessor(AbstractRAProcessor):

    def __init__(self):
        AbstractRAProcessor.__init__(self)

    def process(self, execution_request):

        AbstractRAProcessor.process(self, execution_request)
        print "Processing calling..." + PROPERTY_BLUEPRINT_BASE_PATH
        if self.ra_valid is True:
            value = self.resolve_values_script(execution_request, self.value_to_resolve)
            self.set_resource_data_value(execution_request, value)
        else:
            raise BlueprintProcessorException("Error on resource assignment. Message = " + self.error_message)
        return None

    def recover(self, runtime_exception, execution_request):
        AbstractRAProcessor.recover(self, runtime_exception, execution_request)
        print "Recovering calling..." + PROPERTY_BLUEPRINT_BASE_PATH
        return None

    def resolve_values_script(self, execution_request, value_to_resolve):
        # TODO : DO business logic here
        print "Resolve value for " + value_to_resolve + " here..."
        return "test_python"
