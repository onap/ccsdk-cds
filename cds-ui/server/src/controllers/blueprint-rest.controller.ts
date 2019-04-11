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
  Request,
  Response,
  RestBindings,
} from '@loopback/rest';
import {Blueprint} from '../models';
import { inject } from '@loopback/core';
import { BlueprintService } from '../services';
import * as fs from 'fs';
import * as multiparty from 'multiparty';
import * as request_lib from 'request';

const REST_BLUEPRINT_CONTROLLER_BASE_URL = process.env.REST_BLUEPRINT_CONTROLLER_BASE_URL || "http://localhost:8080/api/v1";
const REST_BLUEPRINT_CONTROLLER_BASIC_AUTH_HEADER = process.env.REST_BLUEPRINT_CONTROLLER_BASIC_AUTH_HEADER || "Basic Y2NzZGthcHBzOmNjc2RrYXBwcw==";
const REST_BLUEPRINT_PROCESSOR_BASE_URL= process.env.REST_BLUEPRINT_PROCESSOR_BASE_URL ||"http://localhost:8081/api/v1";
const MULTIPART_FORM_UPLOAD_DIR = process.env.MULTIPART_FORM_UPLOAD_DIR || "/tmp";

export class BlueprintRestController {
  constructor(
    @inject('services.BlueprintService') 
    public bpservice: BlueprintService,
  ) {}

  @get('/blueprints', {
    responses: {
      '200': {
        description: 'Blueprint model instance',
        content: { 'application/json': { schema: { 'x-ts-type': Blueprint } } },
      },
    },
  })
  async getall() {
    return await this.bpservice.getAllblueprints(REST_BLUEPRINT_CONTROLLER_BASIC_AUTH_HEADER);
  }

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
  ): Promise<Response> {
    return new Promise((resolve, reject) => { 
       this.getFileFromMultiPartForm(request).then(file=>{
         this.uploadFileToBlueprintController(file, REST_BLUEPRINT_CONTROLLER_BASE_URL+"/blueprint-model/", response).then(resp=>{
          resolve(resp);
         }, err=>{
           reject(err);
         });
      }, err=>{
        reject(err);
      });
    });
  }
  @post('/publish')
  async publish(
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
  ): Promise<Response> {
    return new Promise((resolve, reject) => { 
       this.getFileFromMultiPartForm(request).then(file=>{
         this.uploadFileToBlueprintController(file, REST_BLUEPRINT_CONTROLLER_BASE_URL+"/blueprint-model/publish/", response).then(resp=>{
          resolve(resp);
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
  ): Promise<Response> {
    return new Promise((resolve, reject) => { 
       this.getFileFromMultiPartForm(request).then(file=>{
         this.uploadFileToBlueprintController(file, REST_BLUEPRINT_CONTROLLER_BASE_URL+"/blueprint-model/enrich/", response).then(resp=>{
           resolve(resp);
         }, err=>{
           reject(err);
         });
      }, err=>{
        reject(err);
      });
    });
  }

  @get('/download-blueprint/{name}/{version}')
  async download(
    @param.path.string('name') name: string,
    @param.path.string('version') version:string,
    @inject(RestBindings.Http.RESPONSE) response: Response,
  ): Promise<Response> {
    return this.downloadFileFromBlueprintController(REST_BLUEPRINT_CONTROLLER_BASE_URL+"/blueprint-model/download/by-name/"+name+"/version/"+version, response);
  }

  async getFileFromMultiPartForm(request: Request): Promise<multiparty.File>{
    return new Promise((resolve, reject) => {
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

  @post('/deploy-blueprint')
  async deploy(
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
  ): Promise<Response> {
    return new Promise((resolve, reject) => { 
       this.getFileFromMultiPartForm(request).then(file=>{
         this.uploadFileToBlueprintController(file, REST_BLUEPRINT_PROCESSOR_BASE_URL+"/execution-service/upload/", response).then(resp=>{
          resolve(resp);
         }, err=>{
           reject(err);
         });
      }, err=>{
        reject(err);
      });
    });
  }
  async uploadFileToBlueprintController(file: multiparty.File, uri: string, response: Response): Promise<Response>{
    let options = {
      // url: REST_BLUEPRINT_CONTROLLER_BASE_URL + uri,
      url:uri,
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

    var removeTempFile = () => {
      fs.unlink(file.path, (err: any) => {
        if (err) {
          console.error(err);
        } 
      });
    }

    return new Promise((resolve, reject) => {
      request_lib.post(options)
        .on("error", err => {
          reject(err);
        })
        .pipe(response)
        .once("finish", () => {
          removeTempFile();
          resolve(response);
        });
    })
  }
  async downloadFileFromBlueprintController(uri: string, response: Response): Promise<Response> {
    let options = {
      url: uri,
      // REST_BLUEPRINT_CONTROLLER_BASE_URL + uri,
      headers: {
        Authorization: REST_BLUEPRINT_CONTROLLER_BASIC_AUTH_HEADER,
      }
    };
    return new Promise((resolve, reject) => {
      request_lib.get(options)
        .on("error", err => {
          reject(err);
        })
        .pipe(response)
        .once("finish", () => {
          resolve(response);
        });
    })
  }
}