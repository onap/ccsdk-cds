import { processorApiConfig } from '../config/app-config';

export default {
    "name": "blueprint",
    "connector": "rest",
    "baseURL": processorApiConfig.http.url,
    "crud": false,
    "debug": true,
    "operations": [{
        "template": {
            "method": "GET",
            "url": processorApiConfig.http.url + "/blueprint-model/",
            "headers": {
                "accepts": "application/json",
                "content-type": "application/json",
                "authorization": processorApiConfig.http.authToken
            },
            "responsePath": "$.*"
        },
        "functions": {
            "getAllblueprints": []

        }
    }, {
        "template": {
            "method": "GET",
            "url": processorApiConfig.http.url + "/blueprint-model/{id}",
            "headers": {
                "accepts": "application/json",
                "content-type": "application/json",
                "authorization": processorApiConfig.http.authToken
            },
            "responsePath": "$.*"
        },
        "functions": {
            "getOneBlueprint": ["id"]

        }
    },
    {
        "template": {
            "method": "DELETE",
            "url": processorApiConfig.http.url + "/blueprint-model/{id}",
            "headers": {
                "accepts": "application/json",
                "content-type": "application/json",
                "authorization": processorApiConfig.http.authToken
            },
            "responsePath": "$.*"
        },
        "functions": {
            "deleteBlueprint": ["id"]

        }
    },

    {
        "template": {
            "method": "GET",
            "url": processorApiConfig.http.url + "/blueprint-model/search/{tags}",
            "headers": {
                "accepts": "application/json",
                "content-type": "application/json",
                "authorization": processorApiConfig.http.authToken
            },
            "responsePath": "$.*"
        },
        "functions": {
            "getByTags": ["tags"]

        }
    },
    {
        "template": {
            "method": "GET",
            "url": processorApiConfig.http.url + "/blueprint-model/meta-data/{keyword}",
            "headers": {
                "accepts": "application/json",
                "content-type": "application/json",
                "authorization": processorApiConfig.http.authToken
            },
            "responsePath": "$.*"
        },
        "functions": {
            "getBlueprintsByKeyword": ["keyword"]

        }
    },
    {
        "template": {
            "method": "GET",
            "url": processorApiConfig.http.url + "/blueprint-model/paged?limit={limit}&offset={offset}&sort={sort}&sortType={sortType}",
            "headers": {
                "accepts": "application/json",
                "content-type": "application/json",
                "authorization": processorApiConfig.http.authToken
            },
            "responsePath": "$",
        },
        "functions": {
            "getPagedBlueprints": ["limit", "offset", "sort", "sortType"],
        }
    },
    {
        "template": {
            "method": "GET",
            "url": processorApiConfig.http.url + "/blueprint-model/paged/meta-data/{keyword}?limit={limit}&offset={offset}&sort={sort}&sortType={sortType}",
            "headers": {
                "accepts": "application/json",
                "content-type": "application/json",
                "authorization": processorApiConfig.http.authToken
            },
            "responsePath": "$",
        },
        "functions": {
            "getMetaDataPagedBlueprints": ["limit", "offset", "sort", "keyword", "sortType"],
        }
    },
    {
        "template": {
            "method": "GET",
            "url": processorApiConfig.http.url + "/blueprint-model/by-name/{name}/version/{version}",
            "headers": {
                "accepts": "application/json",
                "content-type": "application/json",
                "authorization": processorApiConfig.http.authToken
            },
            "responsePath": "$",
        },
        "functions": {
            "getBlueprintByNameAndVersion": ["name", "version"],
        }
    },
    ]

};
