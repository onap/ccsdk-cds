import {getService} from '@loopback/service-proxy';
import {inject, Provider} from '@loopback/core';
import {BlueprintDataSource} from '../datasources';

export interface BlueprintService {
   getAllblueprints(): Promise<any>;
   getBlueprintsByKeyword(keyword: string): Promise<any>;
   getByTags(tags: string): Promise<JSON>;
   getPagedBueprints(limit: number, offset: number , sort: string): Promise<any>;
}

export class BlueprintServiceProvider implements Provider<BlueprintService> {
  constructor(
    // blueprint must match the name property in the datasource json file
    @inject('datasources.blueprint')
    protected dataSource: BlueprintDataSource = new BlueprintDataSource(),
  ) {}

  value(): Promise<BlueprintService> {
    return getService(this.dataSource);
  }
}
