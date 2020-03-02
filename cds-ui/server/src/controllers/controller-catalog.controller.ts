// Uncomment these imports to begin using these cool features!

// import {inject} from '@loopback/context';
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
import { inject } from '@loopback/core';
import { ControllerCatalogService } from '../services';

export class ControllerCatalogController {
  constructor(
    @inject('services.ControllerCatalogService')
    public Ccservice: ControllerCatalogService,
  ){ }

  @get('/controllercatalog/search/{tags}', {
    responses: {
      '200': {
        content: { 'application/json': {} },
      },
    },
  })
  async getByTags(@param.path.string('tags') tags: string) {
    return await this.Ccservice.getByTags(tags);
  }

  @post('/controllercatalog/save', {
    responses: {
      '200': {
        content: { 'application/json': {} }
      }
    },
  })
  async save(@requestBody({
    content: { 'application/json': { schema: { 'x-ts-type': JSON } } },
    accepts: { 'application/json': { schema: { 'x-ts-type': JSON } } }
  }) controllerCatalog: JSON): Promise<any> {
    return await this.Ccservice.save(controllerCatalog);
  }
  
  @get('/controllercatalog/model-type/by-definition/{definitionType}', {
    responses: {
      '200': {
        content: { 'application/json': {} },
      },
    },
  })
  async getDataTypes(@param.path.string('definitionType') definitionType: string) {
    return await this.Ccservice.getDefinitionTypes(definitionType);
  }

  @del('/controllercatalog/{name}', {
    responses: {
      '200': {
        content: { 'application/json': {} }
      }
    },
  })
  async delete(@param.path.string('name') name: string) {
    return await this.Ccservice.delete(name);
  }
  
}
