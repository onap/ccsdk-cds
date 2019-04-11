import {inject} from '@loopback/core';
import {juggler} from '@loopback/repository';
import * as config from './resource-dictionary.datasource.json';

export class ResourceDictionaryDataSource extends juggler.DataSource {
  static dataSourceName = 'resourceDictionary';

  constructor(
    @inject('datasources.config.resourceDictionary', {optional: true})
    dsConfig: object = config,
  ) {
    super(dsConfig);
  }
}
