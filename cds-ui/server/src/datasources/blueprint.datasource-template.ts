import {processorApiConfig} from '../config/app-config';

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
            "getOneBluePrint": ["id"]

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
                "url": processorApiConfig.http.url + "/blueprint-model/paged?limit={limit}&offset={offset}&sort={sort}",
                "headers": {
                    "accepts": "application/json",
                    "content-type": "application/json",
                    "authorization": processorApiConfig.http.authToken
                },
                "responsePath": "$",
            },
            "functions": {
                "getPagedBueprints": ["limit", "offset", "sort"],
            }
        },
        {
            "template": {
                "method": "GET",
                "url": processorApiConfig.http.url + "/blueprint-model/paged/meta-data/{keyword}?limit={limit}&offset={offset}&sort={sort}",
                "headers": {
                    "accepts": "application/json",
                    "content-type": "application/json",
                    "authorization": processorApiConfig.http.authToken
                },
                "responsePath": "$",
            },
            "functions": {
                "getMetaDataPagedBlueprints": ["limit", "offset", "sort", "keyword"],
            }
        },
    ]

};
