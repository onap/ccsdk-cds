# CDS UI Client

The **CDS UI Client** is the web-based front-end for the ONAP **Controller Design Studio (CDS)**. It provides a graphical interface for designing, managing, deploying, and testing service blueprints used in network automation.

## Key Features

- **Blueprint Designer** — Visual drag-and-drop blueprint design powered by JointJS
- **Blueprint Management** — Upload, modify, deploy, and test service blueprint packages (CBA)
- **Resource Definitions** — Create and manage resource definitions for services
- **Controller Catalog** — Browse and manage available controller functions
- **Integrated Code Editor** — Ace Editor with syntax highlighting for JSON, Python, XML, YAML, Kotlin, and Velocity templates
- **JSON Schema Editor** — Built-in JSON editor for blueprint and resource configurations

## Technology Stack

| Layer            | Technology                          |
| ---------------- | ----------------------------------- |
| Framework        | Angular 7.1                         |
| State Management | NgRx (Store, Effects, Router Store) |
| UI Components    | Angular Material 7.1, Bootstrap 4.3 |
| Diagramming      | JointJS 3.0                         |
| Code Editing     | Ace Editor (ng2-ace-editor)         |
| Package Handling | JSZip                               |
| Testing          | Karma + Jasmine, Protractor (E2E)   |

## Prerequisites

- **Node.js** 8.9 or later
- **npm** 6.x
- **Angular CLI** 7.1 (`npm install -g @angular/cli@7.1`)
- A running **CDS Blueprints Processor** backend (default: `localhost:8080`)

## Getting Started

```bash
# Install dependencies
npm install

# Start the development server (http://localhost:4200)
npm start
```

The app proxies API requests through the LoopBack 4 server located in `../server/`, which forwards them to the CDS Blueprints Processor backend.

## Available Scripts

| Command         | Description                                                |
| --------------- | ---------------------------------------------------------- |
| `npm start`     | Start the Angular dev server on port 4200 with live reload |
| `npm run build` | Build the app (output: `../server/public`)                 |
| `npm test`      | Run unit tests via Karma                                   |
| `npm run lint`  | Lint the project with TSLint                               |
| `npm run e2e`   | Run end-to-end tests via Protractor                        |

For a production build:

```bash
ng build --prod
```

## Project Structure

```
src/app/
├── feature-modules/
│   ├── blueprint/                # Blueprint CRUD (select, modify, deploy, test)
│   ├── blueprint-designer/       # Visual blueprint designer (JointJS)
│   ├── controller-catalog/       # Controller catalog management
│   └── resource-definition/      # Resource creation and editing
├── common/
│   ├── constants/                # App-wide constants
│   ├── core/                     # Core services and HTTP interceptors
│   ├── modules/                  # Shared Angular modules
│   ├── shared/                   # Shared components
│   └── utility/                  # Utility functions
├── app.module.ts                 # Root module
└── app-routing.module.ts         # Lazy-loaded feature routes
```

## Architecture

```
Angular Client  ──►  LoopBack 4 Server (../server)  ──►  CDS Blueprints Processor
   (port 4200)            (intermediary)                      (port 8080)
```

The Angular client communicates with a LoopBack 4 REST server that acts as a gateway to the CDS backend microservices. The built Angular assets are served as static files from the LoopBack server in production.

## Building with Maven

This module is part of the larger ONAP CDS Maven build. From the repository root:

```bash
mvn clean install -pl cds-ui/client
```

## License

Copyright (C) 2018 IBM Intellectual Property. All rights reserved.

Licensed under the Apache License, Version 2.0. See the [LICENSE](http://www.apache.org/licenses/LICENSE-2.0) for details.
