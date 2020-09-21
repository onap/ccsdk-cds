

export const steps = [
    {
        anchorId: 'allTab',
        content: 'Package list is where you get access to your all and most recent CBA packages.',
        title: 'Managing your CBA packages',
    },
    // {
    //     anchorId: 'search',
    //     content: 'Search for Package by name, version, tags and type',
    //     title: 'Search',
    // },
    // {
    //     anchorId: 'tagFilter',
    //     content: 'Filter Packages by tags',
    //     title: 'Tag Filter',
    // },
    {
        anchorId: 'create',
        content: 'Start creating a full CBA packages from built-in forms without programming.',
        title: 'Create new package',
    },
    {
        anchorId: 'metadataTab',
        content: 'It captures the model entities that compose the cba package name, version, description and searchable tags.',
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
    // Temaplate & Mapping
    {
        anchorId: 'tm-templateTab',
        content: 'A template is an artifact, and uses Modeling Concepts#artifact-mapping-resource and artifact-template-velocity. ',
        title: 'Temaplate & Mapping',
        stepId: 'tm-templateTab'
    },
    {
        anchorId: 'tm-templateName',
        content: 'Set your Template & Mapping Name.',
        title: 'Temaplte & Mapping name',
        stepId: 'tm-templateName'
    },
    {
        anchorId: 'tm-templateType',
        content: 'Set your Template Type.',
        title: 'Temaplte Type',
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
        content: 'It is Kotlin/Python scripts that allows the execution of a sequence of instructions as part of CDS workflow execution.',
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
        content: 'Interaction with external systems is made dynamic, removing development cycle to support new endpoint.',
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
