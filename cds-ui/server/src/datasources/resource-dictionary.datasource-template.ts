import { processorApiConfig } from '../config/app-config';

export default {
    "name": "resourceDictionary",
    "connector": "rest",
    "baseURL": processorApiConfig.http.url + "/dictionary",
    "crud": false,
    "debug": true,
    "operations": [{
        "template": {
            "method": "GET",
            "url": processorApiConfig.http.url + "/dictionary/{name}",
            "headers": {
                "accepts": "application/json",
                "content-type": "application/json",
                "authorization": processorApiConfig.http.authToken
            },
            "responsePath": "$.*"
        },
        "functions": {
            "getByName": ["name"]

        }
    },
    {
        "template": {
            "method": "GET",
            "url": processorApiConfig.http.url + "/dictionary/source-mapping",
            "headers": {
                "accepts": "application/json",
                "content-type": "application/json",
                "authorization": processorApiConfig.http.authToken
            },
            "responsePath": "$.*"
        },
        "functions": {
            "getSourceMapping": []

        }
    },
    {
        "template": {
            "method": "GET",
            "url": processorApiConfig.http.url + "/dictionary/search/{tags}",
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
            "method": "POST",
            "url": processorApiConfig.http.url + "/dictionary",
            "headers": {
                "accepts": "application/json",
                "content-type": "application/json",
                "authorization": processorApiConfig.http.authToken
            },
            "body": "{resourceDictionary}",
            "responsePath": "$.*"
        },
        "functions": {
            "save": ["resourceDictionary"]

        }
    },
    {
        "template": {
            "method": "POST",
            "url": processorApiConfig.http.url + "/dictionary/definition",
            "headers": {
                "accepts": "application/json",
                "content-type": "application/json",
                "authorization": processorApiConfig.http.authToken
            },
            "body": "{resourceDictionary}",
            "responsePath": "$.*"
        },
        "functions": {
            "saveDefinition": ["resourceDictionary"]

        }
    },
    {
        "template": {
            "method": "POST",
            "url": processorApiConfig.http.url + "/dictionary/by-names",
            "headers": {
                "accepts": "application/json",
                "content-type": "application/json",
                "authorization": processorApiConfig.http.authToken
            },
            "body": "{resourceDictionaryList}",
            "responsePath": "$.*"
        },
        "functions": {
            "searchbyNames": ["resourceDictionaryList"]

        }
    },
    {
        "template": {
            "method": "GET",
            "url": processorApiConfig.http.url + "/model-type/{source}",
            "headers": {
                "accepts": "application/json",
                "content-type": "application/json",
                "authorization": processorApiConfig.http.authToken
            },
            "responsePath": "$.*"
        },
        "functions": {
            "getModelType": ["source"]

        }
    },
    {
        "template": {
            "method": "GET",
            "url": processorApiConfig.http.url + "/model-type/by-definition/data_type",
            "headers": {
                "accepts": "application/json",
                "content-type": "application/json",
                "authorization": processorApiConfig.http.authToken
            },
            "responsePath": "$.*"
        },
        "functions": {
            "getDataTypes": []

        }
    },
    {
        "template": {
            "method": "GET",
            "url": processorApiConfig.http.url + "/model-type/by-definition/{type}",
            "headers": {
                "accepts": "application/json",
                "content-type": "application/json",
                "authorization": processorApiConfig.http.authToken
            },
            "responsePath": "$.*"
        },
        "functions": {
            "getResourceDictionaryByType": ["type"]

        }
    }
    ]
};
