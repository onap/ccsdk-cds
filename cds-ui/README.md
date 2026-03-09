# CDS UI

The **CDS UI** module provides the full-stack web interface for the ONAP **Controller Design Studio (CDS)**. It consists of two Angular front-end applications, a LoopBack 4 backend-for-frontend (BFF) server, a Docker packaging layer, and an end-to-end test suite.

## Module Structure

```
cds-ui/
├── client/              # Legacy Angular 7 frontend
├── designer-client/     # Modernized Angular 8 frontend
├── server/              # LoopBack 4 BFF server (Node.js)
├── application/         # Docker container assembly
├── common/              # Shared models/resources between server and client
└── e2e-playwright/      # Playwright end-to-end tests
```

| Module              | Description                                                                                                                                |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| **client**          | Original Angular 7.1 UI with NgRx state management, blueprint designer, resource definitions, and controller catalog                       |
| **designer-client** | Modernized Angular 8.2 UI with simplified store pattern, package dashboard, multi-step creation wizard, and resource dictionary management |
| **server**          | LoopBack 4 intermediary that serves the built Angular apps as static files and proxies REST/gRPC requests to the CDS Blueprints Processor  |
| **application**     | Multi-stage Dockerfile that builds client + server into the `onap/ccsdk-cds-ui` container image                                            |
| **common**          | Shared TypeScript models and resources used by both server and client                                                                      |
| **e2e-playwright**  | Playwright tests with a mock processor, enabling full E2E testing without external ONAP infrastructure                                     |

## Architecture

```
┌──────────────────────────────────────────────────────────┐
│  Docker Container (onap/ccsdk-cds-ui)  — port 3000      │
│                                                          │
│  LoopBack 4 Server                                       │
│  ├── Static files: designer-client build (/) & client    │
│  ├── REST proxy:   /controllerblueprint/*, /resource...  │
│  └── gRPC client:  blueprint-proto communication         │
│                           │                              │
└───────────────────────────┼──────────────────────────────┘
                            ▼
                CDS Blueprints Processor (port 8080)
```

During development, the Angular dev servers (port 4200) proxy API requests to the LoopBack server (port 3000), which in turn forwards them to the Blueprints Processor backend (port 8080).

## Prerequisites

- **Java 11+** and **Maven 3.6+** (for the full Maven build)
- **Node.js** — managed per module by `frontend-maven-plugin` (client: 8.12, designer-client: 13.7, server: 16.14)
- A running **CDS Blueprints Processor** at `localhost:8080` (or use the mock processor for testing)

## Building

### Full Maven Build

From the repository root:

```bash
mvn clean install -pl cds-ui -amd
```

This builds all four Maven modules in order: client → designer-client → server → application.

### Local Development Build

To skip linting and use a faster local build for the designer-client:

```bash
mvn clean install -pl cds-ui -amd -DnpmLocal
```

### Individual Modules

```bash
# Build only the designer-client
mvn clean install -pl cds-ui/designer-client

# Build only the server
mvn clean install -pl cds-ui/server
```

### Docker Image

The application module produces the `onap/ccsdk-cds-ui` Docker image via a multi-stage build:

1. **Stage 0** — Builds the Angular client
2. **Stage 1** — Builds the LoopBack server
3. **Final** — Combines built assets and starts the server on port 3000

## Development

### Designer-Client (Recommended)

```bash
cd designer-client
npm install
npm start          # Lints, starts dev server at http://localhost:4200
```

### Server

```bash
cd server
npm install
npm run build
npm start          # Starts LoopBack server at https://localhost:3000
```

### Running E2E Tests

The Playwright test suite runs against a mock processor, requiring no external services:

```bash
cd e2e-playwright
npm install
npm test           # Starts mock-processor + server + Angular, runs tests
```

See [e2e-playwright/README.md](e2e-playwright/README.md) for details on headed mode, UI mode, and the mock processor.

## License

Copyright (C) 2018 IBM Intellectual Property. All rights reserved.

Licensed under the Apache License, Version 2.0. See the [LICENSE](http://www.apache.org/licenses/LICENSE-2.0) for details.
