## How to use library

##### 1. Set Error Catalog service type (Database or properties file service) in application.properties file

```
##### Error Managements #####
## For database service type ##
#    error.catalog.type=DB
## For database service type ##
#    error.catalog.type=properties
error.catalog.applicationId=cds
error.catalog.type=properties
error.catalog.errorDefinitionFileDirectory=/opt/app/onap/config
```

##### 2. Generate exception

- HTTP Error Exception
```
errorCatalogException: ErrorCatalogException = httpProcessorException(ErrorCatalogCodes.ERROR_TYPE, 
"Error message here...")
```

- GRPC Error Exception
```
errorCatalogException: ErrorCatalogException = grpcProcessorException(ErrorCatalogCodes.ERROR_TYPE, 
"Error message here...")
```

##### 3. Update an existing exception
```
e = errorCatalogException.code(500)
e = errorCatalogException.action("message")
...
```

##### 4. Add a HTTP REST Exception handler
@RestControllerAdvice("domain.here")
open class ExceptionHandler(private val errorCatalogService: ErrorCatalogService) :
        ErrorCatalogExceptionHandler(errorCatalogService)