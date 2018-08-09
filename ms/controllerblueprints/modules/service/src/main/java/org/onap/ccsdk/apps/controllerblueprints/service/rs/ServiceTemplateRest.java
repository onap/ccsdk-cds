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

package org.onap.ccsdk.apps.controllerblueprints.service.rs;

import io.swagger.annotations.*;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.core.data.ServiceTemplate;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ConfigModelContent;
import org.onap.ccsdk.apps.controllerblueprints.service.model.AutoMapResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;


/**
 * ServiceTemplateRest.java Purpose: ServiceTemplateRest interface
 *
 * @author Brinda Santh
 * @version 1.0
 */

@Api
@Path("/service")
@Produces({MediaType.APPLICATION_JSON})
public interface ServiceTemplateRest {

    /**
     * This is a enrichServiceTemplate rest service
     * 
     * @param serviceTemplate
     * @return ServiceTemplate
     * @throws BluePrintException
     */
    @POST
    @Path("/servicetemplate/enrich")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to enrich service template", response = ServiceTemplate.class)
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    ServiceTemplate enrichServiceTemplate(@ApiParam(required = true) ServiceTemplate serviceTemplate)
            throws BluePrintException;

    /**
     * This is a validateServiceTemplate rest service
     * 
     * @param serviceTemplate
     * @return ServiceTemplate
     * @throws BluePrintException
     */
    @POST
    @Path("/servicetemplate/validate")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to validate service template", response = ServiceTemplate.class)
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    ServiceTemplate validateServiceTemplate(@ApiParam(required = true) ServiceTemplate serviceTemplate)
            throws BluePrintException;

    /**
     * This is a generateResourceAssignments rest service
     * 
     * @param templateContent
     * @return List<ResourceAssignment>
     * @throws BluePrintException
     */
    @POST
    @Path("/resourceassignment/generate")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to auto map for the Resource Mapping",
            response = ResourceAssignment.class, responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    List<ResourceAssignment> generateResourceAssignments(
            @ApiParam(required = true) ConfigModelContent templateContent) throws BluePrintException;

    /**
     * This is a autoMap rest service
     * 
     * @param resourceAssignments
     * @return AutoMapResponse
     * @throws BluePrintException
     */
    @POST
    @Path("/resourceassignment/automap")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to auto map for the Resource assignments",
            response = AutoMapResponse.class)
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    AutoMapResponse autoMap(@ApiParam(required = true) List<ResourceAssignment> resourceAssignments)
            throws BluePrintException;

    /**
     * This is a validateResourceAssignments rest service
     * 
     * @param resourceAssignments
     * @return List<ResourceAssignment>
     * @throws BluePrintException
     */
    @POST
    @Path("/resourceassignment/validate")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to validate Resource assignments", response = ResourceAssignment.class,
            responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    List<ResourceAssignment> validateResourceAssignments(
            @ApiParam(required = true) List<ResourceAssignment> resourceAssignments) throws BluePrintException;

}
