from netconf_constant import CONFIG_TARGET_RUNNING, CONFIG_TARGET_CANDIDATE, \
  CONFIG_DEFAULT_OPERATION_REPLACE


class NetconfClient:

  def __init__(self, log, component_function, requirement_name):
    self.log = log
    self.component_function = component_function
    netconf_device = self.component_function.initializeNetconfConnection(
        requirement_name)
    self.netconf_rpc_client = netconf_device.netconfRpcService
    self.netconf_session = netconf_device.netconfSession

  def disconnect(self):
    self.netconf_session.disconnect()
    return

  def connect(self):
    self.netconf_session.connect()
    return

  def lock(self, config_target=CONFIG_TARGET_CANDIDATE):
    device_response = self.netconf_rpc_client.lock(config_target)
    return device_response

  def get_config(self, filter="", config_target=CONFIG_TARGET_RUNNING):
    device_response = self.netconf_rpc_client.getConfig(filter, config_target)
    return device_response

  def edit_config(self, message_content, config_target=CONFIG_TARGET_CANDIDATE,
      edit_default_peration=CONFIG_DEFAULT_OPERATION_REPLACE):
    device_response = self.netconf_rpc_client.editConfig(message_content,
                                                         config_target,
                                                         edit_default_peration)
    return device_response

  def commit(self):
    device_response = self.netconf_rpc_client.commit()
    return device_response

  def unlock(self, config_target=CONFIG_TARGET_CANDIDATE):
    device_response = self.netconf_rpc_client.unLock(config_target)
    return device_response

  def validate(self, config_target=CONFIG_TARGET_CANDIDATE):
    device_response = self.netconf_rpc_client.validate(config_target)
    return device_response

  def discard_change(self):
    device_response = self.netconf_rpc_client.discardConfig()
    return device_response
