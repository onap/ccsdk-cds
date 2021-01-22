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


import { get, param, post, Request, requestBody, Response, RestBindings, del } from '@loopback/rest';
import { Blueprint } from '../models';
import { inject } from '@loopback/core';
import { BlueprintService } from '../services';
import * as fs from 'fs';
import * as multiparty from 'multiparty';
import * as request_lib from 'request';
import { appConfig, processorApiConfig } from '../config/app-config';
import { bluePrintManagementServiceGrpcClient } from '../clients/blueprint-management-service-grpc-client';
import { BlueprintDetail } from '../models/blueprint.detail.model';

export class BlueprintRestController {
  constructor(
    @inject('services.BlueprintService')
    public bpservice: BlueprintService,
  ) {
  }

  @get('/controllerblueprint/all', {
    responses: {
      '200': {
        description: 'Blueprint model instance',
        content: { 'application/json': { schema: { 'x-ts-type': Blueprint } } },
      },
    },
  })
  async getall() {
    return await this.bpservice.getAllblueprints();
  }

  @get('/controllerblueprint/{id}', {
    responses: {
      '200': {
        description: 'Blueprint model instance',
        content: { 'application/json': { schema: { 'x-ts-type': BlueprintDetail } } },
      },
    },
  })
  async getOneBlueprint(@param.path.string('id') id: string) {
    return await this.bpservice.getOneBlueprint(id);
  }

  @del('/controllerblueprint/{id}', {
    responses: {
      '200': {
        description: 'Blueprint model instance',
        content: { 'application/json': { schema: { 'x-ts-type': BlueprintDetail } } },
      },
    },
  })
  async deleteBlueprint(@param.path.string('id') id: string) {
    return await this.bpservice.deleteBlueprint(id);
  }


  @get('/controllerblueprint/paged', {
    responses: {
      '200': {
        description: 'Blueprint model instance with pagination',
        content: { 'application/json': { schema: { 'x-ts-type': Blueprint } } },
      },
    },
  })
  async getPagedBlueprints(
    @param.query.number('limit') limit: number,
    @param.query.number('offset') offset: number,
    @param.query.string('sort') sort: string,
    @param.query.string('sortType') sortType: string) {
    return await this.bpservice.getPagedBlueprints(limit, offset, sort, sortType);
  }

  @get('/controllerblueprint/metadata/paged/{keyword}', {
    responses: {
      '200': {
        description: 'Blueprint model instance with pagination',
        content: { 'application/json': { schema: { 'x-ts-type': Blueprint } } },
      },
    },
  })
  async getMetaDataPagedBlueprints(
    @param.path.string('keyword') keyword: string,
    @param.query.number('limit') limit: number,
    @param.query.number('offset') offset: number,
    @param.query.string('sort') sort: string,
    @param.query.string('sortType') sortType: string) {
    return await this.bpservice.getMetaDataPagedBlueprints(limit, offset, sort, keyword, sortType);
  }

  @get('/controllerblueprint/meta-data/{keyword}', {
    responses: {
      '200': {
        description: 'Blueprint model instance',
        content: { 'application/json': { schema: { 'x-ts-type': Blueprint } } },
      },
    },
  })
  async getAllPacakgesByKeword(@param.path.string('keyword') keyword: string) {
    return await this.bpservice.getBlueprintsByKeyword(keyword);
  }

  @get('/controllerblueprint/by-name/{name}/version/{version}', {
    responses: {
      '200': {
        description: 'Blueprint model instance',
        content: { 'application/json': { schema: { 'x-ts-type': Blueprint } } },
      },
    },
  })
  async getPacakgesByNameAndVersion(@param.path.string('name') name: string, @param.path.string('version') version: string) {
    return await this.bpservice.getBlueprintByNameAndVersion(name, version);
  }

  @get('/controllerblueprint/searchByTags/{tags}', {
    responses: {
      '200': {
        content: { 'application/json': {} },
      },
    },
  })
  async getByTags(@param.path.string('tags') tags: string) {
    return await this.bpservice.getByTags(tags);
  }

  @post('/controllerblueprint/create-blueprint')
  async upload(
    @requestBody({
      description: 'multipart/form-data value.',
      required: true,
      content: {
        'multipart/form-data': {
          // Skip body parsing
          'x-parser': 'stream',
          schema: { type: 'object' },
        },
      },
    })
    request: Request,
    @inject(RestBindings.Http.RESPONSE) response: Response,
  ): Promise<Response> {
    return new Promise((resolve, reject) => {
      this.getFileFromMultiPartForm(request).then(file => {
        // if (appConfig.action.deployBlueprint.grpcEnabled)
        if (appConfig.action.grpcEnabled)
          return this.uploadFileToBlueprintProcessorGrpc(file, 'DRAFT', response);
        else
          return this.uploadFileToBlueprintController(file, '/blueprint-model/', response);
      }, err => {
        reject(err);
      });
    });
    // return new Promise((resolve, reject) => {
    //   this.getFileFromMultiPartForm(request).then(file => {
    //     this.uploadFileToBlueprintController(file, "/blueprint-model/", response).then(resp => {
    //       resolve(resp);
    //     }, err => {
    //       reject(err);
    //     });
    //   }, err => {
    //     reject(err);
    //   });
    // });
  }

  @post('/controllerblueprint/publish')
  async publish(
    @requestBody({
      description: 'multipart/form-data value.',
      required: true,
      content: {
        'multipart/form-data': {
          // Skip body parsing
          'x-parser': 'stream',
          schema: { type: 'object' },
        },
      },
    })
    request: Request,
    @inject(RestBindings.Http.RESPONSE) response: Response,
  ): Promise<Response> {
    return new Promise((resolve, reject) => {
      this.getFileFromMultiPartForm(request).then(file => {
        // if (appConfig.action.deployBlueprint.grpcEnabled)
        if (appConfig.action.grpcEnabled)
          return this.uploadFileToBlueprintProcessorGrpc(file, 'PUBLISH', response);
        else
          return this.uploadFileToBlueprintController(file, '/blueprint-model/publish/', response);
      }, err => {
        reject(err);
      });
    });
    // return new Promise((resolve, reject) => {
    //   this.getFileFromMultiPartForm(request).then(file => {
    //     this.uploadFileToBlueprintController(file, "/blueprint-model/publish/", response).then(resp => {
    //       resolve(resp);
    //     }, err => {
    //       reject(err);
    //     });
    //   }, err => {
    //     reject(err);
    //   });
    // });
  }

  @post('/controllerblueprint/enrich-blueprint')
  async enrich(
    @requestBody({
      description: 'multipart/form-data value.',
      required: true,
      content: {
        'multipart/form-data': {
          // Skip body parsing
          'x-parser': 'stream',
          schema: { type: 'object' },
        },
      },
    })
    request: Request,
    @inject(RestBindings.Http.RESPONSE) response: Response,
  ): Promise<Response> {
    return new Promise((resolve, reject) => {
      this.getFileFromMultiPartForm(request).then(file => {
        if (appConfig.action.grpcEnabled)
          return this.uploadFileToBlueprintProcessorGrpc(file, 'ENRICH', response);
        else
          return this.uploadFileToBlueprintController(file, '/blueprint-model/enrich/', response);
        //   this.uploadFileToBlueprintController(file, "/blueprint-model/enrich/", response).then(resp => {
        //     resolve(resp);
        //   }, err => {
        //     reject(err);
        //   });
        // }, err => {
        //   reject(err);
      });
    });
  }

  @post('/controllerblueprint/enrichandpublish')
  async enrichAndPublish(
    @requestBody({
      description: 'multipart/form-data value.',
      required: true,
      content: {
        'multipart/form-data': {
          // Skip body parsing
          'x-parser': 'stream',
          schema: { type: 'object' },
        },
      },
    })
    request: Request,
    @inject(RestBindings.Http.RESPONSE) response: Response,
  ): Promise<Response> {
    return new Promise((resolve, reject) => {
      this.getFileFromMultiPartForm(request).then(file => {
        if (appConfig.action.grpcEnabled)
          return this.uploadFileToBlueprintProcessorGrpc(file, 'ENRICH', response);
        else
          return this.uploadFileToBlueprintController(file, '/blueprint-model/enrichandpublish/', response);
      });
    });
  }

  @get('/controllerblueprint/download-blueprint/{name}/{version}')
  async download(
    @param.path.string('name') name: string,
    @param.path.string('version') version: string,
    @inject(RestBindings.Http.RESPONSE) response: Response,
  ): Promise<Response> {

    if (appConfig.action.grpcEnabled)
      return this.downloadFileFromBlueprintProcessorGrpc(name, version, response);
    else
      return this.downloadFileFromBlueprintController('/blueprint-model/download/by-name/' + name + '/version/' + version, response);
  }


  async getFileFromMultiPartForm(request: Request): Promise<multiparty.File> {
    return new Promise((resolve, reject) => {
      let form = new multiparty.Form();
      form.parse(request, (err: any, fields: any, files: { [x: string]: any[]; }) => {
        if (err) reject(err);
        let file = files['file'][0]; // get the file from the returned files object
        if (!file) {
          reject('File was not found in form data.');
        } else {
          resolve(file);
        }
      });
    });
  }

  @post('/controllerblueprint/deploy-blueprint')
  async deploy(
    @requestBody({
      description: 'multipart/form-data value.',
      required: true,
      content: {
        'multipart/form-data': {
          // Skip body parsing
          'x-parser': 'stream',
          schema: { type: 'object' },
        },
      },
    })
    request: Request,
    @inject(RestBindings.Http.RESPONSE) response: Response,
  ): Promise<Response> {
    return new Promise((resolve, reject) => {
      this.getFileFromMultiPartForm(request).then(file => {
        // if (appConfig.action.deployBlueprint.grpcEnabled)
        if (appConfig.action.grpcEnabled)
          return this.uploadFileToBlueprintProcessorGrpc(file, 'PUBLISH', response);
        else
          return this.uploadFileToBlueprintProcessor(file, '/blueprint-model/publish', response);
      }, err => {
        reject(err);
      });
    });
  }

  async uploadFileToBlueprintController(file: multiparty.File, uri: string, response: Response): Promise<Response> {
    return this.uploadFileToBlueprintService(file, processorApiConfig.http.url + uri, processorApiConfig.http.authToken, response);
  }

  async uploadFileToBlueprintProcessor(file: multiparty.File, uri: string, response: Response): Promise<Response> {
    return this.uploadFileToBlueprintService(file, processorApiConfig.http.url + uri, processorApiConfig.http.authToken, response);
  }

  async uploadFileToBlueprintService(file: multiparty.File, url: string, authToken: string, response: Response): Promise<Response> {
    let options = {
      url: url,
      headers: {
        Authorization: authToken,
        'content-type': 'multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW',
      },
      formData: {
        file: {
          value: fs.createReadStream(file.path),
          options: {
            filename: 'cba.zip',
            contentType: 'application/zip',
          },
        },
      },
    };

    var removeTempFile = () => {
      fs.unlink(file.path, (err: any) => {
        if (err) {
          console.error(err);
        }
      });
    };

    return new Promise((resolve, reject) => {
      request_lib.post(options)
        .on('error', err => {
          reject(err);
        })
        .pipe(response)
        .once('finish', () => {
          removeTempFile();
          resolve(response);
        });
    });
  }

  async downloadFileFromBlueprintController(uri: string, response: Response): Promise<Response> {
    return this.downloadFileFromBlueprintService(processorApiConfig.http.url + uri, processorApiConfig.http.authToken, response);
  }

  async downloadFileFromBlueprintService(url: string, authToken: string, response: Response): Promise<Response> {
    let options = {
      url: url,
      headers: {
        Authorization: authToken,
      },
    };
    return new Promise((resolve, reject) => {
      request_lib.get(options)
        .on('error', err => {
          reject(err);
        })
        .pipe(response)
        .once('finish', () => {
          resolve(response);
        });
    });
  }

  async uploadFileToBlueprintProcessorGrpc(file: multiparty.File, actionName: string, response: Response): Promise<Response> {
    return new Promise<Response>((resolve, reject) => {
      bluePrintManagementServiceGrpcClient.uploadBlueprint(file.path, actionName).then(output => {
        response.send(output.status.message);
        resolve(response);
      }, err => {
        response.status(500).send(err);
        resolve(response);
      });
    });
  }

  async downloadFileFromBlueprintProcessorGrpc(blueprintName: string, blueprintVersion: string, response: Response): Promise<Response> {
    return new Promise<Response>((resolve, reject) => {
      bluePrintManagementServiceGrpcClient.downloadBlueprint(blueprintName, blueprintVersion)
        .then(output => {
          response.send(output.status.message);
          resolve(response);
        }, err => {
          response.status(500).send(err);
          resolve(response);
        });
    });
  }
}
