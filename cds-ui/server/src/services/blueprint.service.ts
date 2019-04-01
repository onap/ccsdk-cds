import {getService} from '@loopback/service-proxy';
import {inject, Provider} from '@loopback/core';
import {BlueprintDataSource} from '../datasources';
import { Stream } from 'stream';
import { BufferType } from '@loopback/repository';
import { RequestBodyObject } from 'openapi3-ts';
import * as FormData from 'form-data';

export interface BlueprintResp {
  id: string;
  artifactUUId: string,
  artifactType: string,
  artifactVersion: string,
  artifactDescription: string,
  internalVersion: string,
  createdDate: string,
  artifactName: string,
  published: string,
  updatedBy: string,
  tags: string
}
export interface BlueprintModel {
  bpresp: BlueprintResp;
}

export interface BpdesigntimeService {
  // this is where you define the Node.js methods that will be
  // mapped to the SOAP operations as stated in the datasource
  // json file.
  getAllbleuprints(): Promise<any>;
  // saveblueprint(authtoken: string,cbazipfile: FormData): Promise<JSON>;
  saveblueprint(contentType:string,authtoken: string,cbazipfile: FormData): Promise<JSON>;
  saveModel(authtoken: string, bp: JSON): Promise<any>;
}

export class BpdesigntimeServiceProvider implements Provider<BpdesigntimeService> {
  constructor(
    // bpdesigntime must match the name property in the datasource json file
    @inject('datasources.bpdesigntime')
    protected dataSource: BlueprintDataSource = new BlueprintDataSource(),
  ) {}

  value(): Promise<BpdesigntimeService> {
    return getService(this.dataSource);
  }
}
