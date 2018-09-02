/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.onap.ccsdk.config.generator.tool;

import com.fasterxml.jackson.core.io.CharTypes;
import com.fasterxml.jackson.databind.node.TextNode;

public class CustomTextNode extends TextNode {
    
    public CustomTextNode(String v) {
        super(v);
    }
    
    @Override
    public String toString() {
        int len = textValue().length();
        len = len + 2 + (len >> 4);
        StringBuilder sb = new StringBuilder(len);
        appendQuoted(sb, textValue());
        return sb.toString();
    }
    
    protected static void appendQuoted(StringBuilder sb, String content) {
        CharTypes.appendQuoted(sb, content);
    }
    
}
