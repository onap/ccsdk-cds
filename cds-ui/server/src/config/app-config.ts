export const appConfig = Object.freeze({
    action: Object.freeze({
        deployBlueprint: Object.freeze({
            grpcEnabled: process.env.APP_ACTION_DEPLOY_BLUEPRINT_GRPC_ENABLED || true
        })
    })
});

export const controllerApiConfig = Object.freeze({
    http: Object.freeze({
        url: process.env.API_BLUEPRINT_CONTROLLER_HTTP_BASE_URL || "http://localhost:8080/api/v1",
        authToken: process.env.API_BLUEPRINT_CONTROLLER_HTTP_AUTH_TOKEN || "Basic Y2NzZGthcHBzOmNjc2RrYXBwcw=="
    })
});

export const processorApiConfig = Object.freeze({
    http: Object.freeze({
        url: process.env.API_BLUEPRINT_PROCESSOR_HTTP_BASE_URL || "http://localhost:8081/api/v1",
        authToken: process.env.API_BLUEPRINT_PROCESSOR_HTTP_AUTH_TOKEN || "Basic Y2NzZGthcHBzOmNjc2RrYXBwcw=="
    }),
    grpc: Object.freeze({
        host: process.env.API_BLUEPRINT_PROCESSOR_GRPC_HOST || "localhost",
        port: process.env.API_BLUEPRINT_PROCESSOR_GRPC_PORT || 9111,
        authToken: process.env.API_BLUEPRINT_PROCESSOR_GRPC_AUTH_TOKEN || "Basic Y2NzZGthcHBzOmNjc2RrYXBwcw==",
        bluePrintManagement: Object.freeze({
            //this path is relative to 'dist' folder
            protoPath: __dirname + '../../proto/BluePrintManagement.proto'
        })
    })
});


