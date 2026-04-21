


export const steps = [
    {
        anchorId: 'allTab',
        content: 'Package list shows all your CBA packages. Each package bundles the templates, mappings, and scripts ' +
            'that CDS uses to automate day-2 operations on a network function.',
        title: 'Managing your CBA packages',
    },
    {
        anchorId: 'search',
        content: 'Search for Package by name, version, tags and type',
        title: 'Search',
    },
    {
        anchorId: 'tagFilter',
        content: 'Filter Packages by tags',
        title: 'Tag Filter',
    },
    {
        anchorId: 'create',
        content: 'Start creating a new CBA package using the built-in wizard. ' +
            'You\'ll define metadata, templates, mappings, and scripts step by step.',
        title: 'Create new package',
    },
    {
        anchorId: 'metadataTab',
        content: 'Give your package a unique name (e.g. pnf-netconf-day2) and version (1.0.0). ' +
            'Tags let you filter packages on the dashboard. Name and version must be unique — ' +
            'if a package with that name and version already exists, increment the version.',
        title: 'Metadata Tab',
        route: 'packages/createPackage'
    },
    {
        anchorId: 'mt-packageName',
        content: 'Set your package name (required).',
        title: 'Package name',
    },
    {
        anchorId: 'mt-packageVersion',
        content: 'Set your package version like 1.0.0 (required).',
        title: 'Package version',
    },
    {
        anchorId: 'mt-packageDescription',
        content: 'Set your package description (required).',
        title: 'Package description',
    },
    {
        anchorId: 'mt-packageTags',
        content: 'Set your package Tags (Optional).',
        title: 'Package tag',
    },
    // {
    //     anchorId: 'mt-packageKeys',
    //     content: 'Set your package custom keys (Optional)',
    //     title: 'Package keys',
    // },
    // Template & Mapping
    {
        anchorId: 'tm-templateTab',
        content: 'A template is the configuration payload CDS sends to the device at runtime — for example, ' +
            'a NETCONF RPC body or a REST request. Write it in Velocity or Jinja2 using $variable placeholders. ' +
            'The Mapping section links each placeholder to a Data Dictionary entry that CDS resolves when the workflow runs.',
        title: 'Template & Mapping',
        stepId: 'tm-templateTab'
    },
    {
        anchorId: 'tm-templateName',
        content: 'Set your Template & Mapping Name.',
        title: 'Template & Mapping name',
        stepId: 'tm-templateName'
    },
    {
        anchorId: 'tm-templateType',
        content: 'Set your Template Type.',
        title: 'Template Type',
    },
    {
        anchorId: 'tm-templateContent',
        content: 'Click \'Import File\' to get content from a file, or write template content manually.',
        title: 'Template Content',
    },
    {
        anchorId: 'tm-mappingContent',
        content: 'Set your mapping content from the current template, or from an external file (XML, CSV).',
        title: 'Mapping Content',
    },
    {
        anchorId: 'tm-templateFinish',
        content: 'Click your \'Finish\' button to save your template.',
        title: 'Finish',
        stepId: 'tm-templateFinish'
    },
    // {
    //     anchorId: 'tm-templateEdit',
    //     content: 'Create another new Template or Click on the previous one to edit.',
    //     title: 'Create & Edit',
    //     stepId: 'tm-templateEdit'
    // },
    // Script
    {
        anchorId: 'st-scriptsTab',
        content: 'Upload Kotlin or Python scripts that implement custom workflow logic. ' +
            'Scripts referenced by your blueprint must implement a CDS component interface. ' +
            'For PNF blueprints, Kotlin scripts are typical. For resource resolution, Python is common.',
        title: 'Scripts',
        stepId: 'st-scriptsTab'
    },
    {
        anchorId: 'st-scriptsImport',
        content: 'Click to import kotlin or python files.',
        title: 'Import File'
    },
    // DSL
    {
        anchorId: 'dslTab',
        content: 'DSL Properties define named connections to external systems — NETCONF endpoints, AAI, SDNC. ' +
            'Use these names inside your node templates. For PNF blueprints, define a netconf-connection here; ' +
            'for VNF blueprints, define AAI and SDNC connections.',
        title: 'External Systems support',
        stepId: 'dslTab'
    },
    // save package
    {
        anchorId: 'packageSave',
        content: 'Save stores your draft in CDS. After saving, use the lifecycle bar above to: ' +
            '(1) import your Data Dictionary, (2) enrich the blueprint — CDS validates and completes the mappings, ' +
            'and (3) publish it so SO can use it in macro service orchestration.',
        title: 'Save'
    }
];
