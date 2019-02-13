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

  def lock(self, message_id, config_target=CONFIG_TARGET_CANDIDATE,
      message_timeout=30):
    device_response = self.netconf_rpc_client.lock(message_id, config_target,
                                                   message_timeout)
    return device_response

  def get_config(self, message_id, filter="",
      config_target=CONFIG_TARGET_RUNNING, message_timeout=30):
    self.log.info("in the ncclient getConfig {}", message_id)
    device_response = self.netconf_rpc_client.getConfig(message_id, filter,
                                                        config_target,
                                                        message_timeout)
    return device_response

  def edit_config(self, message_id, message_content, lock=False,
      config_target=CONFIG_TARGET_CANDIDATE,
      edit_default_peration=CONFIG_DEFAULT_OPERATION_REPLACE,
      deleteConfig=False, validate=False, commit=False, discard_change=False,
      unlock=False, message_timeout=30):
    device_response = self.netconf_rpc_client.editConfig(message_id,
                                                         message_content, lock,
                                                         config_target,
                                                         edit_default_peration,
                                                         deleteConfig, validate,
                                                         commit, discard_change,
                                                         unlock,
                                                         message_timeout)
    return device_response

  def commit(self, message_id, discard_change=True,
      message_timeout=30):
    device_response = self.netconf_rpc_client.commit(message_id, discard_change,
                                                     message_timeout)
    return device_response

  def unlock(self, message_id, config_target=CONFIG_TARGET_CANDIDATE,
      message_timeout=30):
    device_response = self.netconf_rpc_client.unLock(message_id, config_target,
                                                     message_timeout)
    return device_response

  def validate(self, message_id, config_target=CONFIG_TARGET_CANDIDATE,
      message_timeout=30):
    device_response = self.netconf_rpc_client.validate(message_id,
                                                       config_target,
                                                       message_timeout)
    return device_response

  def discard_change(self, message_id, message_timeout=30):
    device_response = self.netconf_rpc_client.discardConfig(message_id,
                                                            message_timeout)
    return device_response
