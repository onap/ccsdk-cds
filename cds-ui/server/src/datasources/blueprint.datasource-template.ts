import {controllerApiConfig} from '../../config/app-config';

export default {
    "name": "blueprint",
    "connector": "rest",
    "baseURL": controllerApiConfig.url,
    "crud": false,
    "operations": [{
        "template": {
            "method": "GET",
            "url": controllerApiConfig.url + "/blueprint-model/",
            "headers": {
                "accepts": "application/json",
                "content-type": "application/json",
                "authorization": controllerApiConfig.authToken
            },
            "responsePath": "$.*"
        },
        "functions": {
            "getAllblueprints": []

        }
    }]
};