import {inject} from '@loopback/core';
import {juggler} from '@loopback/repository';
import * as config from './blueprint.datasource.json';

export class BlueprintDataSource extends juggler.DataSource {
  static dataSourceName = 'blueprint';

  constructor(
    @inject('datasources.config.blueprint', {optional: true})
    dsConfig: object = config,
  ) {
    super(dsConfig);
  }
}
