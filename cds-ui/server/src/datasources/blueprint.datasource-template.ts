import {controllerApiConfig} from '../config/app-config';

export default {
    "name": "blueprint",
    "connector": "rest",
    "baseURL": controllerApiConfig.http.url,
    "crud": false,
    "debug": true,
    "operations": [{
        "template": {
            "method": "GET",
            "url": controllerApiConfig.http.url + "/blueprint-model/",
            "headers": {
                "accepts": "application/json",
                "content-type": "application/json",
                "authorization": controllerApiConfig.http.authToken
            },
            "responsePath": "$.*"
        },
        "functions": {
            "getAllblueprints": []

        }
    },
    {
        "template": {
            "method": "GET",
            "url": controllerApiConfig.http.url + "/blueprint-model/search/{tags}",
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
]
};