package org.onap.ccsdk.config.rest.adaptor.data;

import java.util.Map;

public class RestResponse {

    private String statusCode;
    private String body;
    private Map<String, String> parameters;
    private Map<String, String> responseHeaders;
    private Map<String, String> requestHeaders;

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Map<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    @Override
    public String toString() {
        return "RestResponse [statusCode=" + statusCode + ", body=" + body + ", parameters=" + parameters
                + ", responseHeaders=" + responseHeaders + ", requestHeaders=" + requestHeaders + "]";
    }


}
