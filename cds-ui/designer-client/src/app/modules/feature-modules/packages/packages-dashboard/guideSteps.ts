

export const steps = [
    // {
    //     anchorId: 'allTab',
    //     content: 'This Tab contain all packages you created before',
    //     title: 'All Package',
    // },
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
    // {
    //     anchorId: 'import',
    //     content: 'Import a package to CDS',
    //     title: 'Import',
    // },
    {
        anchorId: 'create',
        content: 'Create a new Package',
        title: 'Create',
    },
    {
        anchorId: 'metadataTab',
        content: 'Set your package basic information',
        title: 'Metadata Tab',
        route: 'packages/createPackage',
        stepId: 'metadataTab'
    },
    {
        anchorId: 'mt-packageName',
        content: 'Set your package name (required)',
        title: 'Package name',
    },
    {
        anchorId: 'mt-packageVersion',
        content: 'Set your package version like 1.0.0 (required)',
        title: 'Package version',
    },
    {
        anchorId: 'mt-packageDescription',
        content: 'Set your package description (required)',
        title: 'Package description',
    },
    {
        anchorId: 'mt-packageTags',
        content: 'Set your package Tags (Optional)',
        title: 'Package tag',
    },
    // -------
    // {
    //     anchorId: 'mt-packageKeys',
    //     content: 'Set your package custom keys (Optional)',
    //     title: 'Package keys',
    // },
    // Temaplate & Mapping
    {
        anchorId: 'tm-templateTab',
        content: 'Create Your  \'Template & Mapping \' files',
        title: 'Temaplate & Mapping',
        stepId: 'tm-templateTab'
    },
    {
        anchorId: 'tm-templateName',
        content: 'Set your Template & Mapping Name',
        title: 'Temaplte & Mapping name',
        stepId: 'tm-templateName'
    },
    {
        anchorId: 'tm-templateType',
        content: 'Set your Template Type',
        title: 'Temaplte Type',
    },
    {
        anchorId: 'tm-templateContent',
        content: 'Click \'Import File\' to get content from a file, or write template content manually',
        title: 'Template Content',
    },
    {
        anchorId: 'tm-mappingContent',
        content: 'Set your mapping content from the current template, or from an external file (XML, CSV)',
        title: 'Mapping Content',
    },
    {
        anchorId: 'tm-templateFinish',
        content: 'Click your \' Finish \' button when you finish ',
        title: 'Finish',
        stepId: 'tm-templateFinish'
    },
    {
        anchorId: 'tm-templateEdit',
        content: 'Create another new Template or Click on the previous one to edit',
        title: 'Create & Edit',
        stepId: 'tm-templateEdit'
    },
    // Script
    {
        anchorId: 'st-scriptsTab',
        content: 'Move To Scripts Tab to set your Kotlin and Python scripts',
        title: 'Scripts',
        stepId: 'st-scriptsTab'
    },
    {
        anchorId: 'st-scriptsImport',
        content: 'Click \' Import File\'button kotlin and python files',
        title: 'Import File'
    },
    // DSL
    {
        anchorId: 'dslTab',
        content: 'Write your Authentication Properties in Javascript',
        title: 'ESAP',
        stepId: 'dslTab'
    },
    // save package
    {
        anchorId: 'packageSave',
        content: 'Click \' Save \' button to create your package',
        title: 'Save'
    }
];
