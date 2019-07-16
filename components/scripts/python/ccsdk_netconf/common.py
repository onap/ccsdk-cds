#  Copyright (c) 2019 Bell Canada.
#  Modifications Copyright © 2018 - 2019 IBM, Bell Canada.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

class ResolutionHelper:

  def __init__(self, component_function):
    self.component_function = component_function

  def resolve_and_generate_message_from_template_prefix(self, artifact_prefix):
    return self.component_function.contentFromResolvedArtifact(artifact_prefix)

  def resolve_and_generate_message(self, artifact_mapping, artifact_template):
    return self.component_function.resolveAndGenerateMessage(artifact_mapping,
                                                             artifact_template)

  def retrieve_resolved_template_from_database(self, key, artifact_template):
    return self.component_function.storedContentFromResolvedArtifact(key, artifact_template)
