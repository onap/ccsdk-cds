import  netconf_constant
from netconfclient import NetconfClient
from java.lang import Exception
from abstract_blueprint_function import AbstractPythonComponentFunction
from org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor import NetconfRpcService
from org.onap.ccsdk.apps.controllerblueprints.core.utils import JacksonUtils
from org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.interfaces import DeviceInfo
from org.onap.ccsdk.apps.controllerblueprints.core.service import BluePrintRuntimeService


class DefaultGetNetConfig(AbstractPythonComponentFunction):
    def process(self, execution_request):
        try:
            log = globals()[netconf_constant.SERVICE_LOG]
            print(globals())
            #requestId = globals()[netconf_constant.PARAM_REQUEST_ID]
            requestId = '1234'

            bluePrintRuntimeService = globals()['bluePrintRuntimeService']

            capabilityProperty = bluePrintRuntimeService.resolveNodeTemplateCapabilityProperties("sample-netconf-device","netconf")

            log.info("capabilityProperty {}",capabilityProperty)
            netconfService = NetconfRpcService()
            nc = NetconfClient(log, netconfService)

            nc.connect(netconfService.getNetconfDeviceInfo(capabilityProperty))
            runningConfigTemplate = "runningconfig-template"

            runningConfigMessageId = "get-config-" + requestId

            deviceResponse = nc.getConfig(messageId=runningConfigMessageId,
                                          filter=runningConfigTemplate)

            log.info("Get Running Config Response {} ", deviceResponse.responseMessage)
            if(deviceResponse !='null') :
                status = deviceResponse.status
                responseData = "{}"
                if (deviceResponse.status != netconf_constant.STATUS_SUCCESS and deviceResponse.errorMessage != 'null'):
                    errorMessage = "Get Running Config Failure ::"+ deviceResponse.errorMessage

        except Exception, err:
            log.info("Exception in the script {}",err.getMessage())
            status = netconf_constant.STATUS_FAILURE
            errorMessage = "Get Running Config Failure ::"+err.getMessage()

    def  recover(self, runtime_exception, execution_request):
        print "Recovering calling.." + PROPERTY_BLUEPRINT_BASE_PATH
        return None

