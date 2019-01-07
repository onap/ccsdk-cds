-- -----------------------------------------------------
-- table CONFIG_MODEL
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS configurator.CONFIG_MODEL (
  config_model_id 		VARCHAR(100) NOT NULL,
  service_uuid 			VARCHAR(50) NULL DEFAULT NULL,
  distribution_id 		VARCHAR(50) NULL DEFAULT NULL,
  service_name 			VARCHAR(255) NULL DEFAULT NULL,
  service_description 		VARCHAR(255) NULL DEFAULT NULL,
  resource_uuid 		VARCHAR(255) NULL DEFAULT NULL,
  resource_instance_name 	VARCHAR(255) NULL DEFAULT NULL,
  resource_name 		varchar(255) null default null,
  resource_version 		varchar(50) null default null,
  resource_type 		varchar(50) null default null,
  artifact_uuid 		varchar(50) null default null,
  artifact_type 		varchar(50) not null,
  artifact_version 		varchar(25) not null,
  artifact_description 		longtext null default null,
  internal_version 		int(11) null default null,
  creation_date 		datetime not null default current_timestamp,
  artifact_name 		varchar(100) not null,
  published 			varchar(1) not null,
  updated_by 			varchar(100) not null,
  tags 				longtext null default null,
  primary key PK_CONFIG_MODEL (config_model_id),
  UNIQUE KEY UK_CONFIG_MODEL (artifact_name , artifact_version)
) ENGINE=InnoDB;


-- -----------------------------------------------------
-- table CONFIG_MODEL_CONTENT
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS configurator.CONFIG_MODEL_CONTENT (
  config_model_content_id 	VARCHAR(100) NOT NULL,
  config_model_id 		INT NOT NULL,
  name 				VARCHAR(100) NOT NULL,
  content_type 			VARCHAR(50) NOT NULL,
  description 			LONGTEXT NULL DEFAULT NULL,
  updated_date 			DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  content 			LONGTEXT NULL DEFAULT NULL,
  PRIMARY KEY PK_CONFIG_MODEL_CONTENT (config_model_content_id),
  UNIQUE KEY UK_CONFIG_MODEL_CONTENT (config_model_id, name, content_type),
  FOREIGN KEY FK_CONFIG_MODEL_CONTENT (config_model_id) REFERENCES configurator.CONFIG_MODEL(config_model_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- table MODEL_TYPE
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS configurator.MODEL_TYPE (
  model_name 		VARCHAR(100) NOT NULL,
  derived_from 		VARCHAR(100) NOT NULL,
  definition_type 	VARCHAR(100) NOT NULL,
  definition 		LONGTEXT NOT NULL,
  version 		VARCHAR(10) NOT NULL,
  description 		LONGTEXT NOT NULL,
  tags 			LONGTEXT NULL DEFAULT NULL,  
  creation_date 	DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by 		VARCHAR(100) NOT NULL,
  PRIMARY KEY PK_MODEL_TYPE (model_name),
  INDEX IX_MODEL_TYPE (model_name)
) ENGINE=InnoDB;


-- -----------------------------------------------------
-- table RESOURCE_DICTIONARY
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS configurator.RESOURCE_DICTIONARY (
  name 			VARCHAR(100) NOT NULL,
  data_type 		VARCHAR(100) NOT NULL,
  entry_schema 		VARCHAR(100) NULL DEFAULT NULL,
  definition 		LONGTEXT NOT NULL,
  description 		LONGTEXT NOT NULL,
  tags 			LONGTEXT NOT NULL,  
  creation_date 	DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by 		VARCHAR(100) NOT NULL,
  primary key PK_RESOURCE_DICTIONARY (name),
  INDEX IX_RESOURCE_DICTIONARY (name)
) ENGINE=InnoDB;
