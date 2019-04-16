.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.


Resource Rest Authentication 
----------------------------

token-auth:

"dsl_definitions": {
  "dynamic-rest-source": {
    "type" : "token-auth",
    "url" : "http://localhost:32778",
    "token" : "Token 0123456789abcdef0123456789abcdef01234567"
  }
}

basic-auth:

"dsl_definitions": {
  "dynamic-rest-source": {
    "type" : "basic-auth",
    "url" : "http://localhost:32778",
    "username" : "bob",
    "password": "marley"
 }
}

ssl-basic-auth:

"dsl_definitions": {
  "dynamic-rest-source": {
    "type" : "ssl-basic-auth",
    "url" : "http://localhost:32778",
    "keyStoreInstance": "JKS or PKCS12",
    "sslTrust": "trusture",
    "sslTrustPassword": "trustore password",
    "sslKey": "keystore",
    "sslKeyPassword: "keystore password"
 }
}