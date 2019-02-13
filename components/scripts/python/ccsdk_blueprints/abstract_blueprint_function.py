from org.onap.ccsdk.apps.blueprintsprocessor.services.execution import AbstractComponentFunction


class AbstractPythonComponentFunction(AbstractComponentFunction):

    def __init__(self):
        AbstractComponentFunction.__init__(self)

    def process(self, execution_request):
        print "Processing calling from parent..."
        return None

    def recover(self, runtime_exception, execution_request):
        print "Recovering calling from parent..."
        return None
