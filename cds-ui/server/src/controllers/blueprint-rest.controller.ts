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
import { inject } from '@loopback/core';
import { Blueprint } from '../models';
import { BlueprintRepository } from '../repositories';
import { Request } from 'express-serve-static-core';
import * as FormData from 'form-data';
import * as Formidable from 'formidable';
import * as fs from 'fs';


export class BlueprintRestController {
  authtoken: any;
  formData: FormData = new FormData();
  contentType: string;
  constructor(
    @repository(BlueprintRepository)
    public blueprintRepository: BlueprintRepository,
    @inject('services.BlueprintService') public bpservice: BlueprintService
  ) { }

  @post('/blueprints', {
    responses: {
      '200': {
        description: 'Blueprint model instance',
        content: { 'application/json': { schema: { 'x-ts-type': Blueprint } } },
      },
    },
  })
  async create(@requestBody({
    description: 'multipart/form-data value.',
    required: true,
    content: {
      'multipart/form-data': {
        'x-parser': 'stream',
        schema: { type: 'object' },
      },
    },
  }) request: Request,
    @inject(RestBindings.Http.RESPONSE) response: Response): Promise<object> {
    this.authtoken = request.headers.authorization;
    var form = new Formidable.IncomingForm();
    form.multiples = true;
    form.parse(request, (err, fields, files) => {
      let filedat = this.getMapValue(files, 'file');
      fs.rename(filedat.path, form.uploadDir + filedat.name, (err) => {
        if (err) throw err;
      });
      let fileToSend = form.uploadDir + filedat.name;
      this.formData.append('file', fs.createReadStream(fileToSend), { filename: 'cba.zip' });
      this.contentType = "multipart/form-data; boundary=" + this.formData.getBoundary();
      console.log(this.contentType);
    });
    return this.bpservice.saveblueprint(this.contentType, this.authtoken, this.formData);
  }
  @get('/blueprints', {
    responses: {
      '200': {
        description: 'Array of Blueprint model instances',
        content: {
          'application/json': {
            schema: { type: 'array', items: { 'x-ts-type': Blueprint } },
          },
        },
      },
    },
  })
  async getAllBlueprint(): Promise<any> {
    return await this.bpservice.getAllblueprints();;
  }

  getMapValue(obj: any, key: any) {
    if (obj.hasOwnProperty(key))
      return obj[key];
    throw new Error("Invalid map key.");
  }

}
