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

import org.junit.Assert;
import org.junit.Test;

public class EscapeUtilsTest {
    
    @Test
    public void testEscapeSql() {
        Assert.assertEquals("", EscapeUtils.escapeSql(""));
        Assert.assertEquals("text", EscapeUtils.escapeSql("text"));
        
        Assert.assertEquals("''", EscapeUtils.escapeSql("'"));
        Assert.assertEquals("\\\\", EscapeUtils.escapeSql("\\"));
        
        Assert.assertEquals("text''text", EscapeUtils.escapeSql("text'text"));
        Assert.assertEquals("text\\\\text", EscapeUtils.escapeSql("text\\text"));
    }
    
    @Test
    public void testEscapeSequence() {
        Assert.assertEquals("", EscapeUtils.escapeSequence(""));
        Assert.assertEquals("text", EscapeUtils.escapeSequence("text"));
        
        Assert.assertEquals("\\\'", EscapeUtils.escapeSequence("'"));
        Assert.assertEquals("\\\"", EscapeUtils.escapeSequence("\""));
        Assert.assertEquals("\\\\", EscapeUtils.escapeSequence("\\"));
        
        Assert.assertEquals("text\\\'text", EscapeUtils.escapeSequence("text'text"));
        Assert.assertEquals("text\\\"text", EscapeUtils.escapeSequence("text\"text"));
        Assert.assertEquals("text\\\\text", EscapeUtils.escapeSequence("text\\text"));
    }
    
}
