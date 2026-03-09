# CDS UI Designer Client

The **Designer Client** is the modernized Angular 8 web interface for the ONAP **Controller Design Studio (CDS)**. It replaces the legacy Angular 7 client with a streamlined architecture, providing tools for designing, creating, and managing CBA (Controller Blueprint Archive) packages and resource definitions for network automation.

## Key Features

- **Package Dashboard** — Browse, search, filter, and paginate published blueprint packages
- **Blueprint Designer** — Visual drag-and-drop blueprint composition powered by JointJS, with action and function attribute editing
- **Package Creation Wizard** — Multi-step workflow for creating CBA packages (metadata, DSL definitions, imports, templates, mappings, scripts)
- **Source Editor** — Raw JSON/YAML editing with Ace Editor (supports JSON, Python, XML, Kotlin, Velocity, YAML)
- **Resource Dictionary** — Create and manage resource definitions with metadata and source templates
- **Package Import** — Drag-and-drop file upload for importing existing packages
- **Guided Tours** — Built-in onboarding tours via ngx-tour

## Technology Stack

| Layer            | Technology                                            |
| ---------------- | ----------------------------------------------------- |
| Framework        | Angular 8.2                                           |
| State Management | Simple Store pattern (RxJS BehaviorSubjects)          |
| UI Components    | Angular Material 8.2, ng-bootstrap 5.1, Bootstrap 4.3 |
| Diagramming      | JointJS 3.0                                           |
| Code Editing     | Ace Editor (ng2-ace-editor)                           |
| Data Tables      | angular-datatables 9.0                                |
| Notifications    | ngx-toastr                                            |
| File Handling    | ngx-file-drop, file-saver                             |
| Loading UI       | ngx-ui-loader                                         |
| Testing          | Karma + Jasmine, Protractor (E2E)                     |

## Prerequisites

- **Node.js** 13.7+ (managed automatically via Maven)
- **npm** 6.13+
- A running CDS backend server (LoopBack 4 server in `../server/`, proxying to the Blueprints Processor)

## Getting Started

```bash
# Install dependencies
npm install

# Start the dev server with proxy and linting (http://localhost:4200)
npm start
```

The dev server proxies `/controllerblueprint/*` and `/resourcedictionary/*` requests to `https://localhost:3000` (the LoopBack server), which forwards them to the CDS Blueprints Processor backend.

## Available Scripts

| Command               | Description                                                            |
| --------------------- | ---------------------------------------------------------------------- |
| `npm start`           | Lint, start the dev server with proxy config, and build for production |
| `npm run build`       | Lint and create a production build (AOT, output: `../server/public`)   |
| `npm run build:local` | Production build without linting                                       |
| `npm test`            | Run unit tests via Karma                                               |
| `npm run lint`        | Lint the project with TSLint                                           |
| `npm run e2e`         | Run end-to-end tests via Protractor                                    |
| `npm run sonar`       | Run SonarQube analysis                                                 |

## Project Structure

```
src/app/
├── modules/
│   ├── feature-modules/
│   │   ├── packages/                       # Blueprint package management
│   │   │   ├── packages-dashboard/         #   Dashboard (list, search, sort, filter, import)
│   │   │   ├── designer/                   #   Visual blueprint designer (JointJS)
│   │   │   │   ├── actions/                #     Action editing
│   │   │   │   ├── action-attributes/      #     Action attribute config
│   │   │   │   ├── functions-attribute/    #     Function attribute config
│   │   │   │   ├── jointjs/               #     JointJS integration
│   │   │   │   └── source-view/           #     Raw source editor
│   │   │   ├── package-creation/           #   Multi-step creation wizard
│   │   │   │   ├── metadata-tab/          #     Package metadata
│   │   │   │   ├── dsl-definitions-tab/   #     DSL & imports
│   │   │   │   ├── template-mapping/      #     Template mappings
│   │   │   │   ├── topology-template/     #     Topology template
│   │   │   │   └── scripts-tab/           #     Script management
│   │   │   ├── configuration-dashboard/    #   Existing package configuration
│   │   │   └── model/                      #   Blueprint data models
│   │   │
│   │   └── resource-dictionary/            # Resource definition management
│   │       ├── resource-dictionary-dashboard/    # Dictionary listing
│   │       └── resource-dictionary-creation/     # Creation wizard
│   │           ├── dictionary-editor/
│   │           ├── dictionary-metadata/
│   │           └── sources-template/
│   │
│   └── shared-modules/                     # Shared UI (header, etc.)
│
└── common/
    ├── constants/                          # Global endpoints and wizard steps
    ├── core/
    │   ├── services/                       # Base HTTP and typed API services
    │   ├── stores/                         # Base Store class
    │   ├── pipes/                          # Search filtering pipe
    │   └── canDactivate/                   # Route deactivation guards
    └── model/                              # Pagination model
```

## Architecture

```
Designer Client  ──►  LoopBack 4 Server (../server)  ──►  CDS Blueprints Processor
   (port 4200)           (port 3000)                          (port 8080)
```

API requests are proxied through the LoopBack server. In production, the built Angular assets (output to `../server/public`) are served as static files by the same server.

## Building with Maven

This module integrates into the ONAP CDS Maven build, which manages Node.js/npm versions automatically:

```bash
# From the repository root
mvn clean install -pl cds-ui/designer-client
```

The Maven build uses `frontend-maven-plugin` to install Node.js 13.7.0, run `npm install`, and execute the production build.

## License

Copyright (C) 2018 IBM Intellectual Property. All rights reserved.

Licensed under the Apache License, Version 2.0. See the [LICENSE](http://www.apache.org/licenses/LICENSE-2.0) for details.
