import {processorApiConfig} from '../config/app-config';

export default {
    "name": "controllerCatalog",
    "connector": "rest",
    "baseURL": processorApiConfig.http.url + "/model-type",
    "crud": false,
    "debug": true,
    "operations": [

          {
            "template": {
                "method": "GET",
                "url": processorApiConfig.http.url + "/model-type/search/{tags}",
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
                "url": processorApiConfig.http.url + "/model-type",
                "headers": {
                    "accepts": "application/json",
                    "content-type": "application/json",
                    "authorization": processorApiConfig.http.authToken
                },
                "body": "{controllerCatalog}",
                "responsePath": "$.*"
            },
            "functions": {
                "save": ["controllerCatalog"]

          }
        },
          {
            "template": {
                "method": "GET",
                "url": processorApiConfig.http.url + "/model-type/by-definition/{definitionType}",
                "headers": {
                    "accepts": "application/json",
                    "content-type": "application/json",
                    "authorization": processorApiConfig.http.authToken
                },
                "responsePath": "$.*"
            },
            "functions": {
                "getDefinitionTypes": ["definitionType"]

           }
        },
           {
             "template": {
                   "method": "DEL",
                   "url": processorApiConfig.http.url + "/model-type/{name}",
                   "headers": {
                       "accepts": "application/json",
                       "content-type": "application/json",
                       "authorization": processorApiConfig.http.authToken
                   },
                   "responsePath": "$.*"
             },
             "functions": {
                  "delete": ["name"]
             }
           }
    ]
};