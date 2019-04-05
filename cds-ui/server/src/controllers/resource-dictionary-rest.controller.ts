/*
* ============LICENSE_START=======================================================
* ONAP : CDS
* ================================================================================
* Copyright 2019 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/

import {
  Count,
  CountSchema,
  Filter,
  repository,
  Where,
} from '@loopback/repository';
import {
  post,
  param,
  get,
  getFilterSchemaFor,
  getWhereSchemaFor,
  patch,
  put,
  del,
  requestBody,
  Request,
  Response,
  RestBindings,
} from '@loopback/rest';
import {ResourceDictionary} from '../models';
import { ResourcedictionaryService } from '../services';
import { inject } from '@loopback/core';
import * as fs from 'fs';
import * as multiparty from 'multiparty';
import * as request_lib from 'request';

const REST_RESOURCEDICTIONARY_CONTROLLER_BASE_URL = process.env.REST_RESOURCEDICTIONARY_CONTROLLER_BASE_URL || "http://localhost:8080/api/v1";
const REST_RESOURCEDICTIONARY_CONTROLLER_BASIC_AUTH_HEADER = process.env.REST_RESOURCEDICTIONARY_CONTROLLER_BASIC_AUTH_HEADER || "Basic Y2NzZGthcHBzOmNjc2RrYXBwcw==";

export class ResourceDictionaryController {
  constructor(
    //@repository(ResourcedictionaryService)
    //public resourcedictionaryRepository : ResourcedictionaryService,
	@inject('services.ResourcedictionaryService') 
    public dictionaryservice: ResourcedictionaryService,
  ) {}

 @get('/resourcedictionary', {
    responses: {
      '200': {
        description: 'ResourceDictionary model instance',
        content: { 'application/json': { schema: { 'x-ts-type': ResourceDictionary } } },
      },
    },
  })
  async getall() {
    return await this.dictionaryservice.getAllresourcedictionary(REST_RESOURCEDICTIONARY_CONTROLLER_BASIC_AUTH_HEADER);

  }
  
  @post('/save-resourcedictionary')
  async save(
    @requestBody({
      description: 'multipart/form-data value.',
      required: true,
      content: {
        'multipart/form-data': {
          // Skip body parsing
          'x-parser': 'stream',
          schema: {type: 'object'},
        },
      },
    })
    request: Request,
    @inject(RestBindings.Http.RESPONSE) response: Response,
  ): Promise<object> {
    return new Promise((resolve, reject) => { 
       this.getFileFromMultiPartForm(request).then(file=>{
         this.uploadFileToResourceDictionaryController(file, "/dictionary/").then(resp=>{
          response.setHeader("X-ONAP-RequestID", resp.headers['x-onap-requestid']);
          resolve(JSON.parse(resp.body));
         }, err=>{
           reject(err);
         });
      }, err=>{
        reject(err);
      });
    });
  }
  
  @post('/search-resourcedictionary/by-names')
  async search(
    @requestBody({
      description: 'multipart/form-data value.',
      required: true,
      content: {
        'multipart/form-data': {
          // Skip body parsing
          'x-parser': 'stream',
          schema: {type: 'object'},
        },
      },
    })
    request: Request,
    @inject(RestBindings.Http.RESPONSE) response: Response,
  ): Promise<object> {
    return new Promise((resolve, reject) => { 
       this.getFileFromMultiPartForm(request).then(file=>{
         this.uploadFileToResourceDictionaryController(file, "/dictionary/by-names").then(resp=>{
          response.setHeader("X-ONAP-RequestID", resp.headers['x-onap-requestid']);
          resolve(JSON.parse(resp.body));
         }, err=>{
           reject(err);
         });
      }, err=>{
        reject(err);
      });
    });
  }
  @get('/get-resourcedictionary/{name}')
  async download(
    @param.path.string('name') name: string,
    @inject(RestBindings.Http.REQUEST) request: Request,
    @inject(RestBindings.Http.RESPONSE) response: Response,
  ): Promise<any> {
    return new Promise((resolve, reject) => { 
      this.downloadFileFromResourceDictionaryController("/get-resourcedictionary/" + name).then(resp=>{
        response.setHeader("X-ONAP-RequestID", resp.headers['x-onap-requestid']);
        response.setHeader("Content-Disposition", resp.headers['content-disposition']);
        resolve(resp.body);
      }, err=>{
        reject(err);
      });
    });
  }
  
  @get('/search-resourcedictionary/{tags}')
  async searchtag(
    @param.path.string('tags') tags: string,
    @inject(RestBindings.Http.REQUEST) request: Request,
    @inject(RestBindings.Http.RESPONSE) response: Response,
  ): Promise<any> {
    return new Promise((resolve, reject) => { 
      this.downloadFileFromResourceDictionaryController("/get-resourcedictionary/search/" + tags).then(resp=>{
        response.setHeader("X-ONAP-RequestID", resp.headers['x-onap-requestid']);
        response.setHeader("Content-Disposition", resp.headers['content-disposition']);
        resolve(resp.body);
      }, err=>{
        reject(err);
      });
    });
  }
  
  @get('/get-resourcedictionary/source-mapping')
  async mapping(
    @inject(RestBindings.Http.REQUEST) request: Request,
    @inject(RestBindings.Http.RESPONSE) response: Response,
  ): Promise<any> {
    return new Promise((resolve, reject) => { 
      this.downloadFileFromResourceDictionaryController("/get-resourcedictionary/source-mapping").then(resp=>{
        response.setHeader("X-ONAP-RequestID", resp.headers['x-onap-requestid']);
        response.setHeader("Content-Disposition", resp.headers['content-disposition']);
        resolve(resp.body);
      }, err=>{
        reject(err);
      });
    });
  }

 




async getFileFromMultiPartForm(request: Request): Promise<any>{
    return new Promise((resolve, reject) => {
      // let options = {
      //   uploadDir: MULTIPART_FORM_UPLOAD_DIR
      // }
      let form = new multiparty.Form();
      form.parse(request, (err: any, fields: any, files: { [x: string]: any[]; }) => {
        if (err) reject(err);
        let file = files['file'][0]; // get the file from the returned files object
        if(!file){
          reject('File was not found in form data.');
        }else{
          resolve(file);
        }
      });
    })
  }
  async uploadFileToResourceDictionaryController(file: any, uri: string): Promise<any>{
    let options = {
      url: REST_RESOURCEDICTIONARY_CONTROLLER_BASE_URL + uri,
      headers: {
        Authorization: REST_RESOURCEDICTIONARY_CONTROLLER_BASIC_AUTH_HEADER,
        'content-type': 'multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW'
      },
      formData: {
        file: {
          value: fs.createReadStream(file.path),
          options: {
            filename: 'cba.json',
            contentType: 'application/json'
          }
        }
      }
    }; 
  }
  async downloadFileFromResourceDictionaryController(uri: string): Promise<any> {
    let options = {
      url: REST_RESOURCEDICTIONARY_CONTROLLER_BASE_URL + uri,
      headers: {
        Authorization: REST_RESOURCEDICTIONARY_CONTROLLER_BASIC_AUTH_HEADER,
      }
    };

    return new Promise((resolve, reject) => {
      request_lib.get(options, (err: any, resp: any, body: any) => {
        if (err) {
          reject(err);
        }else{
          resolve(resp);
        }
      })
    })
}
}