import {controllerApiConfig} from '../../config/app-config';

export default {
    "name": "resourceDictionary",
    "connector": "rest",
    "baseURL": controllerApiConfig.url + "/dictionary",
    "crud": false,
    "operations": [{
            "template": {
                "method": "GET",
                "url": controllerApiConfig.url + "/dictionary/{name}",
                "headers": {
                    "accepts": "application/json",
                    "content-type": "application/json",
                    "authorization": controllerApiConfig.authToken
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
                "url": controllerApiConfig.url + "/dictionary/source-mapping",
                "headers": {
                    "accepts": "application/json",
                    "content-type": "application/json",
                    "authorization": controllerApiConfig.authToken
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
                "url": controllerApiConfig.url + "/dictionary/search/{tags}",
                "headers": {
                    "accepts": "application/json",
                    "content-type": "application/json",
                    "authorization": controllerApiConfig.authToken
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
                "url": controllerApiConfig.url + "/dictionary",
                "headers": {
                    "accepts": "application/json",
                    "content-type": "application/json",
                    "authorization": controllerApiConfig.authToken
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
                "url": controllerApiConfig.url + "/dictionary/by-names",
                "headers": {
                    "accepts": "application/json",
                    "content-type": "application/json",
                    "authorization": controllerApiConfig.authToken
                },
                "body": "{resourceDictionaryList}",
                "responsePath": "$.*"
            },
            "functions": {
                "searchbyNames": ["resourceDictionaryList"]

            }
        }
    ]
};