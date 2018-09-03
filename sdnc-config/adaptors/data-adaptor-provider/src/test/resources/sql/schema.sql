
CREATE TABLE CAPABILITY (
	capability_id 			SERIAL 			PRIMARY KEY,
	capability_name 		VARCHAR(50) 	NOT NULL 		UNIQUE,
	implementation_name 	VARCHAR(100) 	NOT NULL,
	operation_name 			VARCHAR(50) 	NOT NULL,
	operation_description 	VARCHAR(50) 	NOT NULL,
	input_definition 		VARCHAR(50) 	NOT NULL,
	output_definition 		VARCHAR(50) 	NOT NULL,
	dependency_definition 	VARCHAR(50) 	NOT NULL,
	tags 					VARCHAR(50) 	NOT NULL,
	creation_date 			DATETIME 		NOT NULL
);

CREATE TABLE CONFIG_RESOURCE (
   config_resource_id   	VARCHAR(50) 	PRIMARY KEY,
   resource_id          	VARCHAR(50)   	NOT NULL,
   resource_type        	VARCHAR(50)   	NOT NULL,
   service_template_name   	VARCHAR(50)		NULL          DEFAULT NULL,
   service_template_version VARCHAR(50)		NULL          DEFAULT NULL,
   template_name        	VARCHAR(50)   	NOT NULL,
   recipe_name          	VARCHAR(50)   	NOT NULL,
   request_id           	VARCHAR(50)   	NOT NULL,
   resource_data        	LONGTEXT      	NULL          DEFAULT NULL,
   mask_data            	LONGTEXT      	NULL          DEFAULT NULL,
   created_date         	DATETIME      	NOT NULL      DEFAULT CURRENT_TIMESTAMP,
   status               	VARCHAR(20)   	NOT NULL,
   updated_by           	VARCHAR(50)   	NOT NULL
);

CREATE TABLE CONFIG_RESOURCE_ASSIGNMENT_DATA (
   config_resource_assignment_data_id 	VARCHAR(50) 	PRIMARY KEY,
   config_resource_id    				VARCHAR(50) 	NOT NULL,
   version 								INT(11) 		NOT NULL,
   updated_date    						DATETIME		NOT NULL        DEFAULT CURRENT_TIMESTAMP,
   updated_by      						VARCHAR(50)    	NOT NULL,
   template_key_name     				VARCHAR(50)    	NOT NULL,
   resource_name   						VARCHAR(50)    	NOT NULL,
   data_type       						VARCHAR(100)    NOT NULL,
   entry_schema    						VARCHAR(100)    NULL 			DEFAULT NULL,
   resource_value  						LONGTEXT        NOT NULL,
   source   							VARCHAR(50)    	NOT NULL,
   status   							VARCHAR(50)    	NOT NULL,
   message  							LONGTEXT        NOT NULL
);

CREATE TABLE CONFIG_TRANSACTION_LOG (
   config_transaction_log_id  VARCHAR(50)	PRIMARY KEY,
   request_id                 VARCHAR(50)   NULL          DEFAULT NULL,
   message_type               VARCHAR(100)  NULL          DEFAULT NULL,
   creation_date              DATETIME      NOT NULL      DEFAULT CURRENT_TIMESTAMP,
   message                    LONGTEXT      NULL          DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS CONFIG_PROPERTY_MAP (
   reference_key  		VARCHAR(100) 	NOT NULL,
   reference_value  	VARCHAR(250) 	NOT NULL
);

