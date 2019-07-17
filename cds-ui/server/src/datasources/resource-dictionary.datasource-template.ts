import {controllerApiConfig} from '../config/app-config';

export default {
    "name": "resourceDictionary",
    "connector": "rest",
    "baseURL": controllerApiConfig.http.url + "/dictionary",
    "crud": false,
    "debug": true,
    "operations": [{
            "template": {
                "method": "GET",
                "url": controllerApiConfig.http.url + "/dictionary/{name}",
                "headers": {
                    "accepts": "application/json",
                    "content-type": "application/json",
                    "authorization": controllerApiConfig.http.authToken
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
                "url": controllerApiConfig.http.url + "/dictionary/source-mapping",
                "headers": {
                    "accepts": "application/json",
                    "content-type": "application/json",
                    "authorization": controllerApiConfig.http.authToken
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
                "url": controllerApiConfig.http.url + "/dictionary/search/{tags}",
                "headers": {
                    "accepts": "application/json",
                    "content-type": "application/json",
                    "authorization": controllerApiConfig.http.authToken
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
                "url": controllerApiConfig.http.url + "/dictionary",
                "headers": {
                    "accepts": "application/json",
                    "content-type": "application/json",
                    "authorization": controllerApiConfig.http.authToken
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
                "url": controllerApiConfig.http.url + "/dictionary/by-names",
                "headers": {
                    "accepts": "application/json",
                    "content-type": "application/json",
                    "authorization": controllerApiConfig.http.authToken
                },
                "body": "{resourceDictionaryList}",
                "responsePath": "$.*"
            },
            "functions": {
                "searchbyNames": ["resourceDictionaryList"]

            }
        },
        ,
        {
            "template": {
                "method": "GET",
                "url": controllerApiConfig.http.url + "/model-type/{source}",
                "headers": {
                    "accepts": "application/json",
                    "content-type": "application/json",
                    "authorization": controllerApiConfig.http.authToken
                },
                "responsePath": "$.*"
            },
            "functions": {
                "getModelType": ["source"]

            }
        }
    ]
};