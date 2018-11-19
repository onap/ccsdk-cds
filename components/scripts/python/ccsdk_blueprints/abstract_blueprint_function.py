from org.onap.ccsdk.apps.blueprintsprocessor.services.execution import AbstractComponentFunction

class AbstractPythonComponentFunction(AbstractComponentFunction):

    def process(self, execution_request):
        print "Processing calling.."
        return None

    def recover(self, runtime_exception, execution_request):
        print "Recovering calling.."
        return None
