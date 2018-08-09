/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.apps.controllerblueprints.resource.dict.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
/**
 *
 * DecryptionRule.java Purpose:
 * @author Brinda Santh
 */
public class DecryptionRule {

    private List<String> sources = null;
    private String path;
    private String rule;
    @JsonProperty("decrypt-type")
    private String decryptType;

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getDecryptType() {
        return decryptType;
    }

    public void setDecryptType(String decryptType) {
        this.decryptType = decryptType;
    }

}
