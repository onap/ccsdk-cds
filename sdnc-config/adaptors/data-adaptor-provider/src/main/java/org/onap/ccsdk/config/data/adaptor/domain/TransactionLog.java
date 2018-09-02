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

package org.onap.ccsdk.config.data.adaptor.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class TransactionLog implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String transactionLogId;
    private String requestId;
    private String messageType;
    private Date creationDate;
    private String message;
    
    public TransactionLog() {
        
    }
    
    public TransactionLog(String requestId, String messageType, String message) {
        this.requestId = requestId;
        this.messageType = messageType;
        this.message = message;
    }
    
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("[");
        buffer.append("transactionLogId =" + transactionLogId);
        buffer.append(", requestId =" + requestId);
        buffer.append(", messageType =" + messageType);
        buffer.append(", creationDate =" + creationDate);
        buffer.append("]");
        return buffer.toString();
    }
    
    public String getTransactionLogId() {
        return transactionLogId;
    }
    
    public void setTransactionLogId(String transactionLogId) {
        this.transactionLogId = transactionLogId;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getMessageType() {
        return messageType;
    }
    
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getUniqueId() {
        return UUID.randomUUID().toString();
    }
    
}
