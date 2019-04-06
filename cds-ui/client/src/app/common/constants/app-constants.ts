/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2018 IBM Intellectual Property. All rights reserved.
===================================================================

Unless otherwise specified, all software contained herein is licensed
under the Apache License, Version 2.0 (the License);
you may not use this software except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
============LICENSE_END============================================
*/
export const GlobalContants = {
    endpoints: {

    },
    // cbawizard: {
    //     stepsRequired: {stepCount: 3, 
    //                     steps: [{name:'CBA Metadata', componentURL:'/controllerBlueprint/selectTemplate'}, 
    //                             {name:'Controller Blueprint Designer', componentURL:'/controllerBlueprint/modifyTemplate'}, 
    //                             {name:'Test', componentURL:'/controllerBlueprint/testTemplate'},
    //                             {name:'Deploy', componentURL:'/controllerBlueprint/deployTemplate'}]
    //                     }
    // }
    cbawizard: {
        stepsRequired:
        {
            stepCount: 4,
            steps: [{
                name: 'CBA Metadata',
                componentURL: '/controllerBlueprint/selectTemplate',
                label: 'CBA Metadata',
                link: '/blueprint/selectTemplate',
                index: 0,
                component: 'SelectTemplateComponent'
            },
            {
                name: 'Controller Blueprint Designer',
                componentURL: '/controllerBlueprint/modifyTemplate',
                label: 'Controller Blueprint Designer',
                link: '/blueprint/modifyTemplate',
                index: 1,
                component: 'ModifyTemplateComponent'
            },
            {
                name: 'Test',
                componentURL: '/controllerBlueprint/testTemplate',
                label: 'Test',
                link: '/blueprint/testTemplate',
                index: 2,
                component: 'TestTemplateComponent'
            },
            {
                name: 'Deploy',
                componentURL: '/controllerBlueprint/deployTemplate',
                label: 'Deploy',
                link: '/blueprint/deployTemplate',
                index: 3,
                component: 'DeployTemplateComponent'
            }]
        }
    },
    datadictionary: {
        stepsRequired:
        {
            stepCount: 3,
            steps: [{
                name: 'Resource Creation', componentURL: '/dataDictionary/selectTemplate',
                label: 'Resource Creation',
                component: 'ResourceCreationComponent'

            },
            {
                name: 'Edit/Validate', componentURL: '/dataDictionary/modifyTemplate',
                label: 'Edit/Validate',
                component: 'ResourceEditComponent'
            },
            {
                name: 'Save', componentURL: '/dataDictionary/saveTemplate',
                label: 'Save Resource',
                component: 'SaveResourceComponent'
            }]
        }

    }
};
export const LoopbackConfig = {
    url: "http://127.0.0.1:3000",
    authtoken: "ccsdkapps"
}