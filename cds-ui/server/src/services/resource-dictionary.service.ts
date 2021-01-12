import { getService } from '@loopback/service-proxy';
import { inject, Provider } from '@loopback/core';
import { ResourceDictionaryDataSource } from '../datasources';

export interface ResourceDictionaryService {
  getByName(name: string): Promise<JSON>;
  getSourceMapping(): Promise<JSON>;
  getByTags(tags: string): Promise<JSON>;
  save(resourceDictionary: JSON): Promise<JSON>;
  saveDefinition(resourceDictionary: JSON): Promise<JSON>;
  searchbyNames(resourceDictionaryList: JSON): Promise<JSON>;
  getModelType(source: string): Promise<JSON>;
  getDataTypes(): Promise<JSON>;
  getResourceDictionaryByType(type: string): Promise<JSON>;
}

export class ResourceDictionaryServiceProvider implements Provider<ResourceDictionaryService> {
  constructor(
    // resourceDictionary must match the name property in the datasource json file
    @inject('datasources.resourceDictionary')
    protected dataSource: ResourceDictionaryDataSource = new ResourceDictionaryDataSource(),
  ) { }

  value(): Promise<ResourceDictionaryService> {
    return getService(this.dataSource);
  }
}
