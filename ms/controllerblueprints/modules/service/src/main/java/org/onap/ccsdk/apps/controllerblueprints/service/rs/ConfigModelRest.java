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
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ConfigModel;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * ConfigModelRest.java Purpose: Rest service controller for ConfigModelRest Management
 *
 * @author Brinda Santh
 * @version 1.0
 */
@Api
@Path("/service")
@Produces({MediaType.APPLICATION_JSON})
public interface ConfigModelRest {

    /**
     * This is a getConfigModel rest service
     * 
     * @param id
     * @return ConfigModel
     * @throws BluePrintException
     */
    @GET
    @Path("/configmodel/{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to Search Service Template", response = ConfigModel.class)
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    @RequestMapping(value = "/configmodel/{id}", method = RequestMethod.GET)
    @ResponseBody ConfigModel getConfigModel(@ApiParam(required = true) @PathParam("id") Long id)
            throws BluePrintException;
    

    /**
     * This is a saveConfigModel rest service
     * 
     * @param configModel
     * @return ConfigModel
     * @throws BluePrintException
     */
    @POST
    @Path("/configmodel")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to get Model Type by Tags", response = ServiceTemplate.class)
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    ConfigModel saveConfigModel(@ApiParam(required = true) ConfigModel configModel)
            throws BluePrintException;

    /**
     * This is a deleteConfigModel rest service
     * 
     * @param id
     * @throws BluePrintException
     */
    @DELETE
    @Path("/configmodel/{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to delete ConfigModel.")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    void deleteConfigModel(@ApiParam(required = true) @PathParam("id") Long id) throws BluePrintException;

    /**
     * This is a getInitialConfigModel rest service
     * 
     * @param name
     * @return ConfigModel
     * @throws BluePrintException
     */
    @GET
    @Path("/configmodelinitial/{name}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to create default Service Template", response = ConfigModel.class)
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    ConfigModel getInitialConfigModel(@ApiParam(required = true) @PathParam("name") String name)
            throws BluePrintException;

    /**
     * This is a getCloneConfigModel rest service
     * 
     * @param id
     * @return ConfigModel
     * @throws BluePrintException
     */
    @GET
    @Path("/configmodelclone/{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to create default Service Template", response = ConfigModel.class)
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    ConfigModel getCloneConfigModel(@ApiParam(required = true) @PathParam("id") Long id)
            throws BluePrintException;

    /**
     * This is a publishConfigModel rest service
     * 
     * @param id
     * @return ServiceTemplate
     * @throws BluePrintException
     */
    @GET
    @Path("/configmodelpublish/{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to get Model Type by Tags", response = ConfigModel.class)
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    ConfigModel publishConfigModel(@ApiParam(required = true) @PathParam("id") Long id)
            throws BluePrintException;

    /**
     * This is a getConfigModelByNameAndVersion rest service
     * 
     * @param name
     * @param version
     * @return ConfigModel
     * @throws BluePrintException
     */
    @GET
    @Path("/configmodelbyname/{name}/version/{version}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to Search Service Template", response = ConfigModel.class)
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    ConfigModel getConfigModelByNameAndVersion(@ApiParam(required = true) @PathParam("name") String name,
                                               @ApiParam(required = true) @PathParam("version") String version) throws BluePrintException;

    /**
     * This is a searchServiceModels rest service
     * 
     * @param tags
     * @return List<ConfigModel>
     * @throws BluePrintException
     */
    @GET
    @Path("/configmodelsearch/{tags}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Provides Rest service to Search Service Template", response = ConfigModel.class)
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Service not available"),
            @ApiResponse(code = 500, message = "Unexpected Runtime error")})
    List<ConfigModel> searchConfigModels(@ApiParam(required = true) @PathParam("tags") String tags)
            throws BluePrintException;

}
