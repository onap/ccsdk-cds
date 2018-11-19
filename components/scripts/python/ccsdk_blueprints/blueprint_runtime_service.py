class BluePrintRuntimeService:

    def __init__(self, bps):
        self.bps = bps

    def resolveNodeTemplateArtifact(self, node_template_name, artifact_name):
        return self.bps.resolveNodeTemplateArtifact(node_template_name, artifact_name)

    def setNodeTemplateAttributeValue(self, nodeTemplateName, attributeName, value):
        return self.bps.setNodeTemplateAttributeValue(nodeTemplateName, attributeName, value)

    def setNodeTemplatePropertyValue(self, nodeTemplateName, propertyName, value):
        return self.bps.setNodeTemplatePropertyValue(nodeTemplateName, propertyName, value)
