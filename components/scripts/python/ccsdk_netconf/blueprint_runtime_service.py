class BluePrintRuntimeService:

    def __init__(self, bps):
        self.bps = bps

    def get_input_value(self, key):
        """Get input value of the current node template execution

        :param key:
        :return: JsonNode
        """
        return self.bps.getInputValue(key)

    def resolve_dsl_expression(self, dsl_property_name):
        return self.bps.resolveDSLExpression(dsl_property_name)

    def set_node_template_property_value(self, node_template_name, property_name, value):
        self.bps.setNodeTemplatePropertyValue(node_template_name, property_name, value)
        return None

    def set_node_template_attribute_value(self, node_template_name, attribute_name, value):
        self.bps.setNodeTemplateAttributeValue(node_template_name, attribute_name, value)
        return None

    def set_node_template_operation_output_value(self, node_template_name, interface_name, operation_name, property_name, value):
        self.bps.setNodeTemplateOperationOutputValue(node_template_name, interface_name, operation_name, property_name, value)
        return None

    def get_node_template_attribute_value(self, node_template_name, attribute_name):
        return self.bps.getNodeTemplateAttributeValue(node_template_name, attribute_name)

    def get_node_template_property_value(self, node_template_name, property_name):
        return self.bps.getNodeTemplatePropertyValue(node_template_name, property_name)

    def get_node_template_operation_output_value(self, node_template_name, interface_name, operation_name, property_name):
        return self.bps.getNodeTemplateOperationOutputValue(node_template_name, interface_name, operation_name, property_name)