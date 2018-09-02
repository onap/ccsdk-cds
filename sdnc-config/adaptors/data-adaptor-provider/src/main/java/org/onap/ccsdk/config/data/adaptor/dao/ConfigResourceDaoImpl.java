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

package org.onap.ccsdk.config.data.adaptor.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.data.adaptor.domain.ConfigResource;
import org.onap.ccsdk.config.data.adaptor.domain.ResourceAssignmentData;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ConfigResourceDaoImpl implements ConfigResourceDao {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ConfigResourceDaoImpl.class);
    
    private JdbcTemplate jdbcTemplate;
    
    public ConfigResourceDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public ConfigResource save(ConfigResource configResource) throws SvcLogicException {
        
        ConfigResource dbConfigResource = checkConfigResource(configResource);
        
        if (dbConfigResource != null && StringUtils.isNotBlank(dbConfigResource.getConfigResourceId())) {
            configResource.setConfigResourceId(dbConfigResource.getConfigResourceId());
            
            validateConfigResource(configResource);
            
            update(configResource);
            
            saveConfigResourceAssignmentData(configResource);
        } else {
            String addQuery = "INSERT INTO CONFIG_RESOURCE "
                    + "( config_resource_id, resource_id, resource_type, service_template_name, service_template_version,"
                    + "template_name, recipe_name, request_id, resource_data, mask_data, status, created_date, updated_by ) "
                    + "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
            
            configResource.setConfigResourceId(configResource.getUniqueId());
            
            validateConfigResource(configResource);
            
            logger.info("saving config resource ({}) ...", configResource);
            jdbcTemplate.update(addQuery, configResource.getConfigResourceId(), configResource.getResourceId(),
                    configResource.getResourceType(), configResource.getServiceTemplateName(),
                    configResource.getServiceTemplateVersion(), configResource.getTemplateName(),
                    configResource.getRecipeName(), configResource.getRequestId(), configResource.getResourceData(),
                    configResource.getMaskData(), configResource.getStatus(), configResource.getCreatedDate(),
                    configResource.getUpdatedBy());
            
            saveConfigResourceAssignmentData(configResource);
        }
        dbConfigResource = getConfigResource(configResource);
        return dbConfigResource;
    }
    
    private void update(ConfigResource configResource) throws SvcLogicException {
        if (StringUtils.isNotBlank(configResource.getConfigResourceId())) {
            logger.info("updating config resource ({}) ...", configResource);
            // Added service_template_name and version in update query to update with 1802 data.
            String updateQuery = "UPDATE CONFIG_RESOURCE SET "
                    + "resource_data = ?, mask_data = ?, created_date = ?, updated_by = ? ,service_template_name = ?, service_template_version = ? "
                    + "where config_resource_id = ?";
            
            jdbcTemplate.update(updateQuery, configResource.getResourceData(), configResource.getMaskData(),
                    configResource.getCreatedDate(), configResource.getUpdatedBy(),
                    configResource.getServiceTemplateName(), configResource.getServiceTemplateVersion(),
                    configResource.getConfigResourceId());
        } else {
            throw new SvcLogicException("missing config resource id to update.");
        }
    }
    
    private void saveConfigResourceAssignmentData(ConfigResource configResource) {
        if (configResource != null && StringUtils.isNotBlank(configResource.getConfigResourceId())) {
            List<Object> listOfArguments = new ArrayList<>();
            String deleteQuery = "DELETE FROM CONFIG_RESOURCE_ASSIGNMENT_DATA WHERE config_resource_id = ? ";
            listOfArguments.add(configResource.getConfigResourceId());
            this.jdbcTemplate.update(deleteQuery, listOfArguments.toArray());
            logger.info("config resource assignment data deleted successfully for the  config_resource_id ({})",
                    configResource.getConfigResourceId());
            
            if (configResource.getResourceAssignments() != null) {
                List<ResourceAssignmentData> resourceAssignments = configResource.getResourceAssignments();
                for (ResourceAssignmentData resourceAssignmentData : resourceAssignments) {
                    if (resourceAssignmentData != null) {
                        resourceAssignmentData.setConfigResourceId(configResource.getConfigResourceId());
                        saveResourceAssignmentData(resourceAssignmentData);
                    }
                }
            }
        }
    }
    
    private ResourceAssignmentData saveResourceAssignmentData(ResourceAssignmentData resourceAssignmentData) {
        String addQuery = "INSERT INTO CONFIG_RESOURCE_ASSIGNMENT_DATA "
                + "( config_resource_assignment_data_id, config_resource_id, version, updated_by, template_key_name, "
                + "resource_name, data_type, entry_schema, resource_value, source, status, message ) "
                + "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
        
        logger.debug("saving config resource assignment data ({}) ... ", resourceAssignmentData);
        jdbcTemplate.update(addQuery, resourceAssignmentData.getId(), resourceAssignmentData.getConfigResourceId(),
                resourceAssignmentData.getVersion(), resourceAssignmentData.getUpdatedBy(),
                resourceAssignmentData.getTemplateKeyName(), resourceAssignmentData.getResourceName(),
                resourceAssignmentData.getDataType(), resourceAssignmentData.getEntrySchema(),
                resourceAssignmentData.getResourceValue(), resourceAssignmentData.getSource(),
                resourceAssignmentData.getStatus(), resourceAssignmentData.getMessage());
        
        return resourceAssignmentData;
    }
    
    @Override
    public void deleteByConfigResource(ConfigResource configResourceInput) throws SvcLogicException {
        StringBuilder selectArtifactBuffer = new StringBuilder();
        List<Object> listOfArguments = new ArrayList<>();
        
        selectArtifactBuffer.append("DELETE FROM CONFIG_RESOURCE WHERE config_resource_id = ? ");
        listOfArguments.add(configResourceInput.getConfigResourceId());
        
        String queryString = selectArtifactBuffer.toString();
        this.jdbcTemplate.update(queryString, listOfArguments.toArray());
        logger.info("config resource ({}) deleted successfully ", configResourceInput);
    }
    
    @SuppressWarnings("squid:S3776")
    @Override
    public List<ConfigResource> findByConfigResource(ConfigResource configResourceInput) throws SvcLogicException {
        StringBuilder selectArtifactBuffer = new StringBuilder();
        List<Object> listOfArguments = new ArrayList<>();
        
        selectArtifactBuffer.append("SELECT * FROM CONFIG_RESOURCE WHERE ");
        
        StringBuilder whereClauseRemaining = new StringBuilder();
        
        if (configResourceInput.getServiceTemplateName() != null) {
            if (whereClauseRemaining.length() != 0) {
                whereClauseRemaining.append("AND ");
            }
            whereClauseRemaining.append("service_template_name = ? ");
            listOfArguments.add(configResourceInput.getServiceTemplateName());
        }
        
        if (configResourceInput.getServiceTemplateVersion() != null) {
            if (whereClauseRemaining.length() != 0) {
                whereClauseRemaining.append("AND ");
            }
            whereClauseRemaining.append("service_template_version = ? ");
            listOfArguments.add(configResourceInput.getServiceTemplateVersion());
        }
        
        if (configResourceInput.getResourceId() != null) {
            if (whereClauseRemaining.length() != 0) {
                whereClauseRemaining.append("AND ");
            }
            whereClauseRemaining.append("resource_id = ? ");
            listOfArguments.add(configResourceInput.getResourceId());
        }
        
        if (configResourceInput.getResourceType() != null) {
            if (whereClauseRemaining.length() != 0) {
                whereClauseRemaining.append("AND ");
            }
            whereClauseRemaining.append("resource_type = ? ");
            listOfArguments.add(configResourceInput.getResourceType());
        }
        
        if (configResourceInput.getRequestId() != null) {
            if (whereClauseRemaining.length() != 0) {
                whereClauseRemaining.append("AND ");
            }
            whereClauseRemaining.append("request_id = ? ");
            listOfArguments.add(configResourceInput.getRequestId());
        }
        
        if (configResourceInput.getTemplateName() != null) {
            if (whereClauseRemaining.length() != 0) {
                whereClauseRemaining.append("AND ");
            }
            whereClauseRemaining.append("template_name = ? ");
            listOfArguments.add(configResourceInput.getTemplateName());
        }
        
        if (configResourceInput.getRecipeName() != null) {
            if (whereClauseRemaining.length() != 0) {
                whereClauseRemaining.append("AND ");
            }
            whereClauseRemaining.append("recipe_name = ? ");
            listOfArguments.add(configResourceInput.getRecipeName());
        }
        
        String queryString = selectArtifactBuffer.toString() + whereClauseRemaining.toString();
        logger.info("config resource queryString ({})", queryString);
        List<ConfigResource> configResources =
                this.jdbcTemplate.query(queryString, listOfArguments.toArray(), new ConfigResourceMapper());
        
        List<ConfigResource> returnConfigResources = new ArrayList<>();
        
        if (configResources != null) {
            for (ConfigResource configResource : configResources) {
                if (configResource != null) {
                    returnConfigResources.add(getConfigResource(configResource));
                }
            }
        }
        return returnConfigResources;
    }
    
    public ConfigResource checkConfigResource(ConfigResource configResource) {
        StringBuilder selectBuffer = new StringBuilder();
        List<Object> listOfArguments = new ArrayList<>();
        
        selectBuffer.append(
                "SELECT * FROM CONFIG_RESOURCE WHERE resource_id = ? AND resource_type = ? AND template_name = ? ");
        listOfArguments.add(configResource.getResourceId());
        listOfArguments.add(configResource.getResourceType());
        listOfArguments.add(configResource.getTemplateName());
        return queryOneForObject(selectBuffer.toString(), listOfArguments.toArray(), new ConfigResourceMapper());
    }
    
    @Override
    public ConfigResource getConfigResource(ConfigResource configResource) throws SvcLogicException {
        ConfigResource dbConfigResource = checkConfigResource(configResource);
        if (dbConfigResource != null && StringUtils.isNotBlank(dbConfigResource.getConfigResourceId())) {
            List<ResourceAssignmentData> resourceAssignments =
                    getResourceAssignmentdata(dbConfigResource.getConfigResourceId());
            configResource.setResourceAssignments(resourceAssignments);
        }
        return configResource;
    }
    
    private List<ResourceAssignmentData> getResourceAssignmentdata(String configResourceId) {
        List<Object> listOfArguments = new ArrayList<>();
        String queryString = "SELECT * FROM CONFIG_RESOURCE_ASSIGNMENT_DATA WHERE config_resource_id = ? ";
        logger.info("getResourceAssignmentdata queryString ({}), query inputs ({})", queryString, configResourceId);
        listOfArguments.add(configResourceId);
        return this.jdbcTemplate.query(queryString, listOfArguments.toArray(), new ResourceAssignmentDataMapper());
    }
    
    @SuppressWarnings("squid:S3776")
    private boolean validateConfigResource(ConfigResource configResource) throws SvcLogicException {
        if (configResource == null) {
            throw new SvcLogicException("config resource information is missing.");
        }
        
        if (StringUtils.isBlank(configResource.getConfigResourceId())) {
            throw new SvcLogicException("config resource id is missing.");
        }
        
        if (StringUtils.isBlank(configResource.getResourceType())) {
            throw new SvcLogicException("config resource type is missing.");
        }
        if (StringUtils.isBlank(configResource.getResourceId())) {
            throw new SvcLogicException("config resource  resource id is missing.");
        }
        
        if (StringUtils.isBlank(configResource.getRecipeName())) {
            throw new SvcLogicException("config resource action name is missing.");
        }
        
        if (StringUtils.isBlank(configResource.getTemplateName())) {
            throw new SvcLogicException("config resource template name is missing.");
        }
        
        if (configResource.getResourceAssignments() != null) {
            List<ResourceAssignmentData> resourceAssignments = configResource.getResourceAssignments();
            for (ResourceAssignmentData resourceAssignmentData : resourceAssignments) {
                if (resourceAssignmentData != null) {
                    resourceAssignmentData.setConfigResourceId(configResource.getConfigResourceId());
                    if (StringUtils.isBlank(resourceAssignmentData.getId())) {
                        resourceAssignmentData.setId(resourceAssignmentData.getUniqueId());
                    }
                    if (resourceAssignmentData.getVersion() == null || resourceAssignmentData.getVersion() == 0) {
                        resourceAssignmentData.setVersion(1);
                    }
                    if (StringUtils.isBlank(resourceAssignmentData.getUpdatedBy())) {
                        resourceAssignmentData.setUpdatedBy("System");
                    }
                    if (resourceAssignmentData.getStatus() == null) {
                        logger.warn("{} status is missing and setting to undefined", resourceAssignmentData);
                        resourceAssignmentData.setStatus("undefined");
                    }
                    if (resourceAssignmentData.getMessage() == null) {
                        resourceAssignmentData.setMessage("");
                    }
                    if (resourceAssignmentData.getResourceValue() == null) {
                        resourceAssignmentData.setResourceValue("");
                    }
                    
                    validateResourceAssignmentData(resourceAssignmentData);
                }
            }
        }
        
        return true;
    }
    
    private boolean validateResourceAssignmentData(ResourceAssignmentData resourceAssignmentData)
            throws SvcLogicException {
        if (resourceAssignmentData == null) {
            throw new SvcLogicException("resource assignment data information is missing.");
        }
        if (StringUtils.isBlank(resourceAssignmentData.getConfigResourceId())) {
            throw new SvcLogicException("resource assignment data config resource id is missing.");
        }
        if (resourceAssignmentData.getVersion() == null) {
            throw new SvcLogicException(
                    String.format("resource assignment data (%s) version is missing", resourceAssignmentData));
        }
        if (StringUtils.isBlank(resourceAssignmentData.getTemplateKeyName())) {
            throw new SvcLogicException(String.format("resource assignment data (%s) template key name is missing",
                    resourceAssignmentData));
        }
        if (StringUtils.isBlank(resourceAssignmentData.getResourceName())) {
            throw new SvcLogicException(
                    String.format("resource assignment data (%s) resource name is missing", resourceAssignmentData));
        }
        if (resourceAssignmentData.getResourceValue() == null) {
            throw new SvcLogicException(
                    String.format("resource assignment data (%s) resource value is missing", resourceAssignmentData));
        }
        if (StringUtils.isBlank(resourceAssignmentData.getSource())) {
            throw new SvcLogicException(
                    String.format("resource assignment data (%s) source is missing", resourceAssignmentData));
        }
        if (StringUtils.isBlank(resourceAssignmentData.getDataType())) {
            throw new SvcLogicException(
                    String.format("resource assignment data (%s) data type is missing", resourceAssignmentData));
        }
        if (StringUtils.isBlank(resourceAssignmentData.getStatus())) {
            throw new SvcLogicException(
                    String.format("resource assignment data (%s) status is missing", resourceAssignmentData));
        }
        if (resourceAssignmentData.getMessage() == null) {
            throw new SvcLogicException(
                    String.format("resource assignment data (%s) message is missing", resourceAssignmentData));
        }
        return true;
    }
    
    private <T> T queryOneForObject(String sql, Object[] args, RowMapper<T> rowMapper) {
        List<T> results = this.jdbcTemplate.query(sql, args, new RowMapperResultSetExtractor<T>(rowMapper, 1));
        if (results != null && !results.isEmpty()) {
            return results.get(0);
        } else {
            return null;
        }
    }
    
    class ConfigResourceMapper implements RowMapper<ConfigResource> {
        @Override
        public ConfigResource mapRow(ResultSet rs, int rowNum) throws SQLException {
            ConfigResource configResource = new ConfigResource();
            configResource.setConfigResourceId(rs.getString("config_resource_id"));
            configResource.setResourceId(rs.getString("resource_id"));
            configResource.setResourceType(rs.getString("resource_type"));
            configResource.setServiceTemplateName(rs.getString("service_template_name"));
            configResource.setServiceTemplateVersion(rs.getString("service_template_version"));
            configResource.setTemplateName(rs.getString("template_name"));
            configResource.setRecipeName(rs.getString("recipe_name"));
            configResource.setRequestId(rs.getString("request_id"));
            configResource.setResourceData(rs.getString("resource_data"));
            configResource.setMaskData(rs.getString("mask_data"));
            configResource.setStatus(rs.getString("status"));
            configResource.setCreatedDate(rs.getDate("created_date"));
            configResource.setUpdatedBy(rs.getString("updated_by"));
            return configResource;
        }
    }
    
    class ResourceAssignmentDataMapper implements RowMapper<ResourceAssignmentData> {
        @Override
        public ResourceAssignmentData mapRow(ResultSet rs, int rowNum) throws SQLException {
            ResourceAssignmentData resourceAssignmentData = new ResourceAssignmentData();
            resourceAssignmentData.setConfigResourceId(rs.getString("config_resource_id"));
            resourceAssignmentData.setDataType(rs.getString("data_type"));
            resourceAssignmentData.setEntrySchema(rs.getString("entry_schema"));
            resourceAssignmentData.setId(rs.getString("config_resource_assignment_data_id"));
            resourceAssignmentData.setMessage(rs.getString("message"));
            resourceAssignmentData.setResourceName(rs.getString("resource_name"));
            resourceAssignmentData.setResourceValue(rs.getString("resource_value"));
            resourceAssignmentData.setSource(rs.getString("source"));
            resourceAssignmentData.setStatus(rs.getString("status"));
            resourceAssignmentData.setTemplateKeyName(rs.getString("template_key_name"));
            resourceAssignmentData.setUpdatedBy(rs.getString("updated_by"));
            resourceAssignmentData.setUpdatedDate(rs.getTimestamp("updated_date"));
            resourceAssignmentData.setVersion(rs.getInt("version"));
            return resourceAssignmentData;
        }
    }
    
}
