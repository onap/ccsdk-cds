class BluePrintRuntimeService:

    def __init__(self, bps):
        self.bps = bps

    def resolveNodeTemplateArtifact(self, node_template_name, artifact_name):
        return self.bps.resolveNodeTemplateArtifact(node_template_name, artifact_name)

    def setNodeTemplateAttributeValue(self, nodeTemplateName, attributeName, value):
        self.bps.setNodeTemplateAttributeValue(nodeTemplateName, attributeName, value)
        return None

    def setNodeTemplatePropertyValue(self, nodeTemplateName, propertyName, value):
        self.bps.setNodeTemplatePropertyValue(nodeTemplateName, propertyName, value)
        return None

    def put_resolution_store(self, ra_name, value):
        self.bps.putResolutionStore(ra_name, value)
        return None

    def put_dictionary_store(self, ra_dictionary_name, value):
        self.bps.putResolutionStore(ra_dictionary_name, value)
        return None

    def get_node_template_attribute_value(self, node_template_name, attribute_name):
        return self.bps.getNodeTemplateAttributeValue(node_template_name, attribute_name)

    def get_node_template_property_value(self, node_template_name, property_name):
        return self.bps.getNodeTemplatePropertyValue(node_template_name, property_name)

    def get_json_node_from_resolution_store(self, key):
        return self.bps.getJsonNodeFromResolutionStore(key)

    def get_string_from_resolution_store(self, key):
        return self.bps.getStringFromResolutionStore(key)

    def get_boolean_from_resolution_store(self, key):
        return self.bps.getBooleanFromResolutionStore(key)

    def get_int_from_resolution_store(self, key):
        return self.bps.getIntFromResolutionStore(key)

    def get_double_from_resolution_store(self, key):
        return self.bps.getDoubleFromResolutionStore(key)

    def get_json_node_from_dictionary_store(self, key):
        return self.bps.getJsonNodeFromDictionaryStore(key)

    def get_string_from_dictionary_store(self, key):
        return self.bps.getStringFromDictionaryStore(key)

    def get_boolean_from_dictionary_store(self, key):
        return self.bps.getBooleanFromDictionaryStore(key)

    def get_int_from_dictionary_store(self, key):
        return self.bps.getIntFromDictionaryStore(key)

    def get_double_from_dictionary_store(self, key):
        return self.bps.getDoubleFromDictionaryStore(key)