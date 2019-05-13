import {inject} from '@loopback/core';
import {juggler} from '@loopback/repository';
import config from './blueprint.datasource-template';

export class BlueprintDataSource extends juggler.DataSource {
  static dataSourceName = 'blueprint';

  constructor(
    @inject('datasources.config.blueprint', {optional: true})
    dsConfig: object = config,
  ) {
    super(dsConfig);
  }
}
