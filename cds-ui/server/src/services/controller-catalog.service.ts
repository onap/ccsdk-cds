import {getService} from '@loopback/service-proxy';
import {inject, Provider} from '@loopback/core';
import {ControllerCatalogDataSource} from '../datasources';

export interface ControllerCatalogService {
  getByTags(tags: string): Promise<JSON>;
  save(controllerCatalog: JSON): Promise<JSON>;
  getDefinitionTypes(definitionType: string): Promise<JSON>;
  deleteCatalog(name: string): Promise<JSON>;
}

export class ControllerCatalogServiceProvider implements Provider<ControllerCatalogService> {
  constructor(
    // controllerCatalog must match the name property in the datasource json file
    @inject('datasources.controllerCatalog')
    protected dataSource: ControllerCatalogDataSource = new ControllerCatalogDataSource(),
  ) {}

  value(): Promise<ControllerCatalogService> {
    return getService(this.dataSource);
  }
}
