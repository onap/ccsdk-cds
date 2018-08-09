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
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ModelType;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * ModelTypeRest.java Purpose: Rest service controller for Artifact Handling
 *
 * @author Brinda Santh
 * @version 1.0
 */
@Api
@Path("/service")
@Produces({MediaType.APPLICATION_JSON})
public interface ModelTypeRest {

    /**
     * This is a getModelTypeByName rest service
     * 
     * @param name
     * @return ModelType
     * @throws BluePrintException
     */
    @GET
    @Path("/modeltype/{name}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to get Model Type by id", response = ModelType.class)
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    ModelType getModelTypeByName(@ApiParam(required = true) @PathParam("name") String name)
            throws BluePrintException;

    /**
     * This is a saveModelType rest service
     * 
     * @param modelType
     * @return ModelType
     * @throws BluePrintException
     */

    @POST
    @Path("/modeltype")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to Save Model Type", response = ModelType.class)
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    ModelType saveModelType(@ApiParam(required = true) ModelType modelType) throws BluePrintException;

    /**
     * This is a deleteModelType rest service
     * 
     * @param name
     * @throws BluePrintException
     */
    @DELETE
    @Path("/modeltype/{name}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to delete Model Type")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    void deleteModelTypeByName(@ApiParam(required = true) @PathParam("name") String name)
            throws BluePrintException;

    /**
     * This is a searchModelType rest service
     * 
     * @param tags
     * @return List<ModelType>
     * @throws BluePrintException
     */
    @GET
    @Path("/modeltypesearch/{tags}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to get Model Type by tags", response = ModelType.class,
            responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    List<ModelType> searchModelTypes(@ApiParam(required = true) @PathParam("tags") String tags)
            throws BluePrintException;

    /**
     * This is a getModelTypeByDefinitionType rest service
     * 
     * @param definitionType
     * @return List<ModelType>
     * @throws BluePrintException
     */
    @GET
    @Path("/modeltypebydefinition/{definitionType}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to get Model Type by tags", response = ModelType.class,
            responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    List<ModelType> getModelTypeByDefinitionType(
            @ApiParam(required = true) @PathParam("definitionType") String definitionType)
            throws BluePrintException;

}
