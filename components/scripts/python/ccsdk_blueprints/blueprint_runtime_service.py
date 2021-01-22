class BlueprintRuntimeService:

    def __init__(self, bps):
        self.bps = bps

    def resolveNodeTemplateArtifact(self, node_template_name, artifact_name):
        return self.bps.resolveNodeTemplateArtifact(node_template_name, artifact_name)

    def setNodeTemplateAttributeValue(self, nodeTemplateName, attributeName, value):
        return self.bps.setNodeTemplateAttributeValue(nodeTemplateName, attributeName, value)

    def setNodeTemplatePropertyValue(self, nodeTemplateName, propertyName, value):
        return self.bps.setNodeTemplatePropertyValue(nodeTemplateName, propertyName, value)

    def put_resolution_store(self, ra_name, value):
        self.bps.putResolutionStore(ra_name, value)
        return None

    def put_dictionary_store(self, ra_dictionary_name, value):
        self.bps.putResolutionStore(ra_dictionary_name, value)
        return None
