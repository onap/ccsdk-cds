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
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ResourceDictionary;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * ResourceDictionaryRest.java Purpose: Rest service controller for Artifact Handling
 *
 * @author Brinda Santh
 * @version 1.0
 */
@Api
@Path("/service")
@Produces({MediaType.APPLICATION_JSON})

public interface ResourceDictionaryRest {

    /**
     * This is a getDataDictionaryByPath rest service
     * 
     * @param name
     * @return ResourceDictionary
     * @throws BluePrintException
     */
    @GET
    @Path("/dictionary/{name}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to get Resource dictionary", response = ResourceDictionary.class)
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    ResourceDictionary getResourceDictionaryByName(@ApiParam(required = true) @PathParam("name") String name)
            throws BluePrintException;

    /**
     * This is a saveDataDictionary rest service
     * 
     * @param resourceMapping
     * @return ResourceDictionary
     * @throws BluePrintException
     */

    @POST
    @Path("/dictionary")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to Save Resource dictionary Type", response = ResourceDictionary.class)
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    ResourceDictionary saveResourceDictionary(@ApiParam(required = true) ResourceDictionary resourceMapping)
            throws BluePrintException;

    /**
     * This is a deleteDataDictionaryByName rest service
     * 
     * @param name
     * @throws BluePrintException
     */
    @DELETE
    @Path("/dictionary/{name}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to delete ResourceDictionary Type")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    void deleteResourceDictionaryByName(@ApiParam(required = true) @PathParam("name") String name)
            throws BluePrintException;

    /**
     * This is a searchResourceDictionaryByTags rest service
     * 
     * @param tags
     * @return ResourceDictionary
     * @throws BluePrintException
     */
    @GET
    @Path("/dictionarysearch/{tags}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to search Resource dictionary by tags",
            response = ResourceDictionary.class, responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    List<ResourceDictionary> searchResourceDictionaryByTags(
            @ApiParam(required = true) @PathParam("tags") String tags) throws BluePrintException;

    /**
     * This is a searchResourceDictionaryByNames rest service
     * 
     * @param names
     * @return List<ResourceDictionary>
     * @throws BluePrintException
     */
    @POST
    @Path("/dictionarybynames")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to get ResourceDictionary Type by names",
            response = ResourceDictionary.class, responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    List<ResourceDictionary> searchResourceDictionaryByNames(@ApiParam(required = true) List<String> names)
            throws BluePrintException;

}
