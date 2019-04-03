/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2018-19 IBM Intellectual Property. All rights reserved.
===================================================================

Unless otherwise specified, all software contained herein is licensed
under the Apache License, Version 2.0 (the License);
you may not use this software except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
============LICENSE_END============================================
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
  Response,
  RestBindings,
} from '@loopback/rest';
import {Blueprint} from '../models';
//import {BlueprintRepository} from '../repositories';
import { inject } from '@loopback/core';
//import { BlueprintService } from '../services';
import * as fs from 'fs';
import * as multiparty from 'multiparty';
import * as request_lib from 'request';

const REST_BLUEPRINT_CONTROLLER_BASE_URL = process.env.REST_BLUEPRINT_CONTROLLER_BASE_URL || "http://localhost:8080/api/v1";
const REST_BLUEPRINT_CONTROLLER_BASIC_AUTH_HEADER = process.env.REST_BLUEPRINT_CONTROLLER_BASIC_AUTH_HEADER || "Basic Y2NzZGthcHBzOmNjc2RrYXBwcw==";

export class BlueprintRestController {
  constructor(
    @repository(BlueprintRepository)
    public blueprintRepository : BlueprintRepository,
    @inject('services.BlueprintService') 
    public bpservice: BlueprintService,
  ) {}

  /*@get('/blueprints', {
    responses: {
      '200': {
        description: 'Blueprint model instance',
        content: { 'application/json': { schema: { 'x-ts-type': Blueprint } } },
      },
    },
  })
  async getall() {
    //return await this.bpservice.getAllbleuprints();

  }*/

  @post('/create-blueprint')
  async upload(
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
         this.uploadFileToBlueprintController(file, "/blueprint-model/").then(resp=>{
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

  @post('/enrich-blueprint')
  async enrich(
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
  ): Promise<any> {
    return new Promise((resolve, reject) => { 
       this.getFileFromMultiPartForm(request).then(file=>{
         this.uploadFileToBlueprintController(file, "/blueprint-model/enrich/").then(resp=>{
           response.setHeader("X-ONAP-RequestID", resp.headers['x-onap-requestid']);
           response.setHeader("Content-Disposition", resp.headers['content-disposition']);
           resolve(resp.body);
         }, err=>{
           reject(err);
         });
      }, err=>{
        reject(err);
      });
    });
  }

  @get('/download-blueprint/{id}')
  async download(
    @param.path.string('id') id: string,
    @inject(RestBindings.Http.REQUEST) request: Request,
    @inject(RestBindings.Http.RESPONSE) response: Response,
  ): Promise<any> {
    return new Promise((resolve, reject) => { 
      this.downloadFileFromBlueprintController("/blueprint-model/download/" + id).then(resp=>{
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

  async uploadFileToBlueprintController(file: any, uri: string): Promise<any>{
    let options = {
      url: REST_BLUEPRINT_CONTROLLER_BASE_URL + uri,
      headers: {
        Authorization: REST_BLUEPRINT_CONTROLLER_BASIC_AUTH_HEADER,
        'content-type': 'multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW'
      },
      formData: {
        file: {
          value: fs.createReadStream(file.path),
          options: {
            filename: 'cba.zip',
            contentType: 'application/zip'
          }
        }
      }
    };

    return new Promise((resolve, reject) => {
      request_lib.post(options, (err: any, resp: any, body: any) => {
        if (err) {
          //delete tmp file
          fs.unlink(file.path, (err: any) => {
            if (err) {
              console.error(err);
              return
            }
          })
          reject(err);
        }else{
          resolve(resp);
        }
      })
    })
  }

  async downloadFileFromBlueprintController(uri: string): Promise<any> {
    let options = {
      url: REST_BLUEPRINT_CONTROLLER_BASE_URL + uri,
      headers: {
        Authorization: REST_BLUEPRINT_CONTROLLER_BASIC_AUTH_HEADER,
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