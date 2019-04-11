import {getService} from '@loopback/service-proxy';
import {inject, Provider} from '@loopback/core';
import {ResourceDictionaryDataSource} from '../datasources';

export interface ResourceDictionaryService {
  getByName(name: string, authtoken: string): Promise<JSON>;
  getSourceMapping(authtoken: string): Promise<JSON>;
  getByTags(tags: string, authtoken: string): Promise<JSON>;
  save(authtoken: string, resourceDictionary: JSON): Promise<JSON>;
  searchbyNames(authtoken: string, resourceDictionaryList: JSON): Promise<JSON>;
}

export class ResourceDictionaryServiceProvider implements Provider<ResourceDictionaryService> {
  constructor(
    // resourceDictionary must match the name property in the datasource json file
    @inject('datasources.resourceDictionary')
    protected dataSource: ResourceDictionaryDataSource = new ResourceDictionaryDataSource(),
  ) {}

  value(): Promise<ResourceDictionaryService> {
    return getService(this.dataSource);
  }
}
