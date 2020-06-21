/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2019 Orange. All rights reserved.
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


export class MetaDataTabModel {

    mode: string;
    name: string;
    description: string;
    version: string;
    mapOfCustomKey: Map<string, string> = new Map<string, string>();
    entryFileName: string;
    templateName: string;
    templateTags: Set<string> = new Set<string>();

    setCustomKey(mapOfCustomKey: Map<string, string>) {
        this.mapOfCustomKey = mapOfCustomKey;
        return this;
    }

}


export interface FolderNodes {
    name: string;
    children?: FolderNodes[];
}

export class FolderNodeElement {
    TREE_DATA: FolderNodes[] = [
        {
            name: 'Definitions',
            children: [
                {name: 'activation-blueprint.json'},
                {name: 'artifacts_types.json'},
                {name: 'data_types.json'},
                {name: 'vLB_CDS.json'},
            ]
        },
        {
            name: 'Scripts',
            children: [
                {
                    name: 'kotlin',
                    children: [
                        {name: 'ScriptComponent.cba.kts'},
                        {name: 'ResourceAssignmentProcessor.cba.kts'},
                    ]
                }
            ]
        },
        {
            name: 'Templates',
            children: [
                {
                    name: 'baseconfig-template'
                }
            ]
        },
        {
            name: 'TOSCA-Metadata',
            children: [
                {
                    name: 'TOSCA.meta'
                }
            ]
        },
    ];
}

export class FilesContent {

    private static mapOfFilesNamesAndContent: Map<string, string> = new Map<string, string>();

    public static getMapOfFilesNamesAndContent(): Map<string, string> {
        return FilesContent.mapOfFilesNamesAndContent;
    }

    public static putData(fileName: string, content: string) {
        FilesContent.mapOfFilesNamesAndContent.set(fileName, content);
    }

    public static clear() {
        this.mapOfFilesNamesAndContent = new Map<string, string>();
    }
}
