import {inject} from '@loopback/core';
import {juggler} from '@loopback/repository';
import config from './controller-catalog.datasource-template';

export class ControllerCatalogDataSource extends juggler.DataSource {
  static dataSourceName = 'controllerCatalog';

  constructor(
    @inject('datasources.config.controllerCatalog', {optional: true})
    dsConfig: object = config,
  ) {
    super(dsConfig);
  }
}
