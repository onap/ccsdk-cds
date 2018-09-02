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

package org.onap.ccsdk.config.model.data;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ConstraintClause.java Purpose: 3.5.2 Constraint clause.
 *
 * @version 1.0
 */
public class ConstraintClause {
    @JsonProperty("equal")
    private Object equal;
    
    @JsonProperty("greater_than")
    private Object greaterThan;
    
    @JsonProperty("greater_or_equal")
    private Object greaterOrEqual;
    
    @JsonProperty("less_than")
    private Object lessThan;
    
    @JsonProperty("less_or_equal")
    private Object lessOrEqual;
    
    @JsonProperty("in_range")
    private Object inRange;
    
    @JsonProperty("valid_values")
    private List<Object> validValues;
    
    @JsonProperty("length")
    private Object length;
    
    @JsonProperty("min_length")
    private Object minLength;
    
    @JsonProperty("max_length")
    private Object maxLength;
    
    @JsonProperty("pattern")
    private String pattern;
    
    public Object getEqual() {
        return equal;
    }
    
    public void setEqual(Object equal) {
        this.equal = equal;
    }
    
    public Object getGreaterThan() {
        return greaterThan;
    }
    
    public void setGreaterThan(Object greaterThan) {
        this.greaterThan = greaterThan;
    }
    
    public Object getGreaterOrEqual() {
        return greaterOrEqual;
    }
    
    public void setGreaterOrEqual(Object greaterOrEqual) {
        this.greaterOrEqual = greaterOrEqual;
    }
    
    public Object getLessThan() {
        return lessThan;
    }
    
    public void setLessThan(Object lessThan) {
        this.lessThan = lessThan;
    }
    
    public Object getLessOrEqual() {
        return lessOrEqual;
    }
    
    public void setLessOrEqual(Object lessOrEqual) {
        this.lessOrEqual = lessOrEqual;
    }
    
    public Object getInRange() {
        return inRange;
    }
    
    public void setInRange(Object inRange) {
        this.inRange = inRange;
    }
    
    public List<Object> getValidValues() {
        return validValues;
    }
    
    public void setValidValues(List<Object> validValues) {
        this.validValues = validValues;
    }
    
    public Object getLength() {
        return length;
    }
    
    public void setLength(Object length) {
        this.length = length;
    }
    
    public Object getMinLength() {
        return minLength;
    }
    
    public void setMinLength(Object minLength) {
        this.minLength = minLength;
    }
    
    public Object getMaxLength() {
        return maxLength;
    }
    
    public void setMaxLength(Object maxLength) {
        this.maxLength = maxLength;
    }
    
    public String getPattern() {
        return pattern;
    }
    
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
    
}
