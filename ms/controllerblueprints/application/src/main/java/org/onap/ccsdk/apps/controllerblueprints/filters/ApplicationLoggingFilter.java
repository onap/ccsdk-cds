/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
 *  Modifications Copyright © 2018 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.apps.controllerblueprints.filters;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants;
import org.onap.logging.ref.slf4j.ONAPLogAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ApplicationLoggingFilter
 *
 * @author Brinda Santh 8/14/2018
 */
@Component
@WebFilter(asyncSupported = true, urlPatterns = {"/*"})
@Order(Ordered.HIGHEST_PRECEDENCE)
@SuppressWarnings("unused")
public class ApplicationLoggingFilter implements Filter {
    private static Logger log = LoggerFactory.getLogger(ApplicationLoggingFilter.class);

    @SuppressWarnings("unused")
    @Value("${appVersion}")
    private String appVersion;

    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        ONAPLogAdapter onapLogAdapter = new ONAPLogAdapter(log);
        onapLogAdapter.entering(req);

        String[] tokens = StringUtils.split(appVersion, '.');
        Preconditions.checkNotNull(tokens, "failed to split application versions");
        Preconditions.checkArgument(tokens.length == 3, "failed to tokenize application versions");
        res.addHeader(BluePrintConstants.RESPONSE_HEADER_TRANSACTION_ID, MDC.get("RequestID"));
        res.addHeader(BluePrintConstants.RESPONSE_HEADER_MINOR_VERSION, tokens[1]);
        res.addHeader(BluePrintConstants.RESPONSE_HEADER_PATCH_VERSION, tokens[2]);
        res.addHeader(BluePrintConstants.RESPONSE_HEADER_LATEST_VERSION, appVersion);
        chain.doFilter(request, response);
        // Clean the MDC info
        onapLogAdapter.exiting();
    }

    @Override
    public void init(FilterConfig filterConfig) {
      //method does nothing
    }

    @Override
    public void destroy() {
      //method does nothing
    }
}