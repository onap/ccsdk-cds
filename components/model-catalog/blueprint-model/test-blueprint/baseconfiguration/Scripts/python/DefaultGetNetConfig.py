import  netconf_constant
from netconfclient import NetconfClient
from java.lang import Exception
from org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor import NetconfComponentFunction

class DefaultGetNetConfig(NetconfComponentFunction):

    def process(self, execution_request):
        try:
            log = globals()[netconf_constant.SERVICE_LOG]
            print(globals())
            #requestId = globals()[netconf_constant.PARAM_REQUEST_ID]
            requestId = '1234'
            nc = NetconfClient(log, self)

            # Create RPC Serivice
            nc.createRPCServiceFromRequirement("netconf-connection")

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
            log.error("Exception in the script {}",err.getMessage())
            status = netconf_constant.STATUS_FAILURE
            errorMessage = "Get Running Config Failure ::"+err.getMessage()

    def  recover(self, runtime_exception, execution_request):
        print "Recovering calling.." + PROPERTY_BLUEPRINT_BASE_PATH
        return None

