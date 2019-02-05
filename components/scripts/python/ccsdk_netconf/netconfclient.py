from netconf_constant import CONFIG_TARGET_RUNNING, CONFIG_DEFAULT_OPERATION_REPLACE



class NetconfClient:

    def __init__(self, log, nc):
        self.log = log
        self.nc = nc

    def disconnect(self):
        self.nc.disconnect()
        return

    def connect(self,deviceInfo):
        self.nc.connect(deviceInfo)
        return

    def lock(self, messageId, configTarget, messageTimeout=30):
        deviceResponse = self.nc.lock(messageId, configTarget, messageTimeout)
        return deviceResponse

    def getConfig(self, messageId, filter, configTarget=CONFIG_TARGET_RUNNING, messageTimeout=30):
        self.log.info("in the ncclient getConfig {}",messageId)
        self.log.info("in the ncclient getConfig {}",filter)
        deviceResponse = self.nc.getConfig(messageId, filter, configTarget, messageTimeout)
        return deviceResponse
    
    def editConfig(self, messageId, messageContent, reConnect=False, wait=0, lock=False,
                   configTarget=CONFIG_TARGET_RUNNING, editDefaultOperation=CONFIG_DEFAULT_OPERATION_REPLACE, 
                   deleteConfig= False, validate= False, commit=False, discardChanges =True, unlock=False, 
                   preRestartWait=0, postRestartWait=0, messageTimeout=30):
        deviceResponse = self.nc.editConfig(messageId, messageContent, reConnect, wait, lock, configTarget,
                                            editDefaultOperation, deleteConfig, validate, commit, discardChanges, unlock,
                                            preRestartWait, postRestartWait, messageTimeout)
        return deviceResponse

    def commit(self, messageId, message, discardChanges =True, messageTimeout=30):
        deviceResponse = self.nc.commit(messageId, message, discardChanges, messageTimeout)
        return deviceResponse

    def unLock(self, messageId, configTarget, messageTimeout=30):
        deviceResponse = self.nc.unLock(messageId, configTarget, messageTimeout)
        return deviceResponse

    def discardChanges(self, messageId, messageTimeout=30):
        deviceResponse = self.nc.discardChanges(messageId, messageTimeout)
        return deviceResponse

    def close(self, messageId, force=False, messageTimeout=30):
        deviceResponse = self.nc.close(messageId, force, messageTimeout)
        return deviceResponse

    def rpc(self, request, messageId, messageTimeout=30):
        deviceResponse = self.nc.rpc(request, messageId, messageTimeout)
        return deviceResponse
