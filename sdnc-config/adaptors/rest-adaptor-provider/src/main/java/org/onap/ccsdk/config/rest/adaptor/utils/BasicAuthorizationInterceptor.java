package org.onap.ccsdk.config.rest.adaptor.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;

public class BasicAuthorizationInterceptor implements ClientHttpRequestInterceptor {
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final String username;

    private final String pass;


    /**
     * Create a new interceptor which adds a BASIC authorization header for the given username and pass.
     *
     * @param username the username to use
     * @param pass the password to use
     */
    public BasicAuthorizationInterceptor(String username, String pass) {
        Assert.hasLength(username, "Username must not be empty");
        this.username = username;
        this.pass = (pass != null ? pass : "");
    }


    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        String token = Base64Utils.encodeToString((this.username + ":" + this.pass).getBytes(UTF_8));
        request.getHeaders().add("Authorization", "Basic " + token);
        return execution.execute(request, body);
    }
}
