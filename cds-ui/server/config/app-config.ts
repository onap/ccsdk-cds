export const controllerApiConfig = Object.freeze({
    url: process.env.API_BLUEPRINT_CONTROLLER_BASE_URL || "http://localhost:8080/api/v1",
    authToken: process.env.API_BLUEPRINT_CONTROLLER_AUTH_TOKEN || "Basic Y2NzZGthcHBzOmNjc2RrYXBwcw=="
});

export const processorApiConfig = Object.freeze({
    url: process.env.API_BLUEPRINT_PROCESSOR_BASE_URL || "http://localhost:8081/api/v1",
    authToken: process.env.API_BLUEPRINT_PROCESSOR_AUTH_TOKEN || "Basic Y2NzZGthcHBzOmNjc2RrYXBwcw=="
});