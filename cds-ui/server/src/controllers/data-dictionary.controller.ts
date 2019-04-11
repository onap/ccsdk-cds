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

const REST_RESOURCE_DICTIONARY_BASIC_AUTH_HEADER = process.env.REST_BLUEPRINT_CONTROLLER_BASIC_AUTH_HEADER || "Basic Y2NzZGthcHBzOmNjc2RrYXBwcw==";
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
    return await this.rdservice.getByName(name, REST_RESOURCE_DICTIONARY_BASIC_AUTH_HEADER);
  }
  @get('/resourcedictionary/{tags}', {
    responses: {
      '200': {
        content: { 'application/json': {} },
      },
    },
  })
  async getByTags(@param.path.string('tags') tags: string) {
    return await this.rdservice.getByTags(tags, REST_RESOURCE_DICTIONARY_BASIC_AUTH_HEADER);
  }

  @get('/resourcedictionary/source-mapping', {
    responses: {
      '200': {
        content: { 'application/json': {} },
      },
    },
  })
  async getSourceMapping() {
    return await this.rdservice.getSourceMapping(REST_RESOURCE_DICTIONARY_BASIC_AUTH_HEADER);
  }
  @post('/resourcedictionary/save', {
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
    return await this.rdservice.save(REST_RESOURCE_DICTIONARY_BASIC_AUTH_HEADER, resourceDictionary);
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
    return await this.rdservice.searchbyNames(REST_RESOURCE_DICTIONARY_BASIC_AUTH_HEADER, resourceDictionaryList);
  }
}
