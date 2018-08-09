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

package org.onap.ccsdk.apps.controllerblueprints.service.common;

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.PrintWriter;
import java.io.StringWriter;

@Provider
public class ServiceExceptionMapper implements ExceptionMapper<BluePrintException> {

    @Override
    public Response toResponse(BluePrintException ex) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setCode(ex.getCode());
        errorMessage.setMessage(ex.getMessage());
        StringWriter errorStackTrace = new StringWriter();
        ex.printStackTrace(new PrintWriter(errorStackTrace));
        errorMessage.setDeveloperMessage(ex.toString());
        return Response.status(500).entity(errorMessage).type(MediaType.APPLICATION_JSON).build();
    }

}