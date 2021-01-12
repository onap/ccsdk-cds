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
import { ResourceDictionaryService } from '../services';

export class DataDictionaryController {
  constructor(
    @inject('services.ResourceDictionaryService')
    public rdservice: ResourceDictionaryService,
  ) { }

  @get('/resourcedictionary/{name}', {
    responses: {
      '200': {
        content: { 'application/json': {} },
      },
    },
  })
  async getByName(@param.path.string('name') name: string) {
    return await this.rdservice.getByName(name);
  }

  @get('/resourcedictionary/search/{tags}', {
    responses: {
      '200': {
        content: { 'application/json': {} },
      },
    },
  })
  async getByTags(@param.path.string('tags') tags: string) {
    return await this.rdservice.getByTags(tags);
  }

  @get('/resourcedictionary/source-mapping', {
    responses: {
      '200': {
        content: { 'application/json': {} },
      },
    },
  })
  async getSourceMapping() {
    return await this.rdservice.getSourceMapping();
  }

  @post('/resourcedictionary/save', {
    responses: {
      '200': {
        content: { 'application/json': {} }
      }
    },
  })
  async saveDefinition(@requestBody({
    content: { 'application/json': { schema: { 'x-ts-type': JSON } } },
    accepts: { 'application/json': { schema: { 'x-ts-type': JSON } } }
  }) resourceDictionary: JSON): Promise<any> {
    return await this.rdservice.saveDefinition(resourceDictionary);
  }

  @post('/resourcedictionary/definition', {
    responses: {
      '200': {
        content: { 'application/json': {} }
      }
    },
  })
  async save(@requestBody({
    content: { 'application/json': { schema: { 'x-ts-type': JSON } } },
    accepts: { 'application/json': { schema: { 'x-ts-type': JSON } } }
  }) resourceDictionary: JSON): Promise<any> {
    return await this.rdservice.saveDefinition(resourceDictionary);
  }


  @post('/resourcedictionary/search/by-names', {
    responses: {
      '200': {
        content: { 'application/json': {} }
      }
    },
  })
  async searchByNames(@requestBody({
    content: { 'application/json': { schema: { 'x-ts-type': JSON } } },
    accepts: { 'application/json': { schema: { 'x-ts-type': JSON } } }
  }) resourceDictionaryList: JSON): Promise<any> {
    return await this.rdservice.searchbyNames(resourceDictionaryList);
  }

  @get('/resourcedictionary/model-type/{source}', {
    responses: {
      '200': {
        content: { 'application/json': {} },
      },
    },
  })
  async getmodelType(@param.path.string('source') source: string) {
    return await this.rdservice.getModelType(source);
  }

  /**
   * @deprecated   use getResourceDictionaryByType Instead.
   */
  @get('/resourcedictionary/model-type/by-definition/data_type', {
    responses: {
      '200': {
        content: { 'application/json': {} },
      },
    },
  })
  async getDataTypes() {
    console.warn("Calling deprecated function!");
    return await this.rdservice.getDataTypes();
  }

  @get('/resourcedictionary/model-type/by-definition/{type}', {
    responses: {
      '200': {
        content: { 'application/json': {} },
      },
    },
  })
  async getResourceDictionaryByType(@param.path.string('type') type: string) {
    return await this.rdservice.getResourceDictionaryByType(type);
  }

}
