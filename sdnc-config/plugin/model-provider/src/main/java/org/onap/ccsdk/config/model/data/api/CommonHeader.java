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

package org.onap.ccsdk.config.model.data.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CommonHeader {
    
    private String timestamp;
    @JsonProperty("api-ver")
    private String apiVer;
    @JsonProperty("originator-id")
    private String originatorId;
    @JsonProperty("request-id")
    private String requestId;
    @JsonProperty("sub-request-id")
    private String subRequestId;
    private Flags flags;
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getApiVer() {
        return apiVer;
    }
    
    public void setApiVer(String apiVer) {
        this.apiVer = apiVer;
    }
    
    public String getOriginatorId() {
        return originatorId;
    }
    
    public void setOriginatorId(String originatorId) {
        this.originatorId = originatorId;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getSubRequestId() {
        return subRequestId;
    }
    
    public void setSubRequestId(String subRequestId) {
        this.subRequestId = subRequestId;
    }
    
    public Flags getFlags() {
        return flags;
    }
    
    public void setFlags(Flags flags) {
        this.flags = flags;
    }
}
