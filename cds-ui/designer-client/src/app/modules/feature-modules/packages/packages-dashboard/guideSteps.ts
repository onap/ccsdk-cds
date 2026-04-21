


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
        content: 'Give your automation package a unique name (e.g. vRouter-day2) and version (e.g. 1.0.0). ' +
            'Tags let operators search for packages on the dashboard.',
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
        content: 'A template is the configuration payload CDS sends to the device — a NETCONF RPC or REST body. ' +
            'Mapping links each template variable to a Resource Dictionary entry that CDS resolves at runtime.',
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
        content: 'Upload Kotlin or Python scripts that implement your workflow logic. ' +
            'Each script must implement a CDS component interface (e.g. ComponentFunctionScriptingService).',
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
        content: 'DSL Properties define named connections to external systems (AAI, SDNC, Netconf). ' +
            'Reference these names inside your templates and workflow definitions.',
        title: 'External Systems support',
        stepId: 'dslTab'
    },
    // save package
    {
        anchorId: 'packageSave',
        content: 'Click to save your package.',
        title: 'Save'
    }
];
