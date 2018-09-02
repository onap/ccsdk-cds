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

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("squid:S1118")
public class EscapeUtils {
    
    public static String escapeSql(String str) {
        if (str == null) {
            return null;
        }
        String[] searchList = new String[] {"'", "\\"};
        String[] replacementList = new String[] {"''", "\\\\"};
        return StringUtils.replaceEach(str, searchList, replacementList);
    }
    
    // For Generic Purpose
    public static String escapeSequence(String s) {
        if (s == null) {
            return null;
        }
        
        int length = s.length();
        int newLength = length;
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\':
                case '\"':
                case '\'':
                case '\0':
                    newLength += 1;
                    break;
                default:
                    // do nothing
            }
        }
        if (length == newLength) {
            // nothing to escape in the string
            return s;
        }
        StringBuilder sb = new StringBuilder(newLength);
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\'':
                    sb.append("\\\'");
                    break;
                case '\0':
                    sb.append("\\0");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
    
}
