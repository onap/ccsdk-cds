/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2026 Deutsche Telekom AG.
===================================================================

Unless otherwise specified, all software contained herein is licensed
under the Apache License, Version 2.0 (the License);
you may not use this software except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
============LICENSE_END============================================
*/

import {get, param, Response, RestBindings} from '@loopback/rest';
import {inject} from '@loopback/core';
import * as request_lib from 'request';
import {processorApiConfig} from '../config/app-config';

export class ResolutionController {
  @get('/resolution/resources')
  async getResolvedResources(
    @param.query.string('bpName') bpName: string,
    @param.query.string('bpVersion') bpVersion: string,
    @param.query.string('artifactName') artifactName: string,
    @param.query.string('resolutionKey') resolutionKey: string,
    @param.query.string('resourceType') resourceType: string,
    @param.query.string('resourceId') resourceId: string,
    @inject(RestBindings.Http.RESPONSE) response: Response,
  ): Promise<Response> {
    return this.proxyGet(
      '/resources',
      {
        bpName,
        bpVersion,
        artifactName,
        resolutionKey,
        resourceType,
        resourceId,
      },
      response,
    );
  }

  @get('/resolution/template')
  async getResolvedTemplate(
    @param.query.string('bpName') bpName: string,
    @param.query.string('bpVersion') bpVersion: string,
    @param.query.string('artifactName') artifactName: string,
    @param.query.string('resolutionKey') resolutionKey: string,
    @param.query.string('resourceType') resourceType: string,
    @param.query.string('resourceId') resourceId: string,
    @param.query.string('occurrence') occurrence: string,
    @inject(RestBindings.Http.RESPONSE) response: Response,
  ): Promise<Response> {
    return this.proxyGet(
      '/template',
      {
        bpName,
        bpVersion,
        artifactName,
        resolutionKey,
        resourceType,
        resourceId,
        occurrence,
      },
      response,
    );
  }

  @get('/resolution/config-snapshot')
  async getConfigSnapshot(
    @param.query.string('resourceType') resourceType: string,
    @param.query.string('resourceId') resourceId: string,
    @param.query.string('status') status: string,
    @inject(RestBindings.Http.RESPONSE) response: Response,
  ): Promise<Response> {
    return this.proxyGet(
      '/configs',
      {resourceType, resourceId, status},
      response,
    );
  }

  @get('/resolution/config-snapshots-by-type')
  async getConfigSnapshotsByType(
    @param.query.string('resourceType') resourceType: string,
    @param.query.string('status') status: string,
    @inject(RestBindings.Http.RESPONSE) response: Response,
  ): Promise<Response> {
    return this.proxyGet(
      '/configs/allByType',
      {resourceType, status},
      response,
    );
  }

  private proxyGet(
    uri: string,
    query: {[key: string]: string | undefined},
    response: Response,
  ): Promise<Response> {
    const qs: {[key: string]: string} = {};
    Object.keys(query)
      .filter(key => query[key])
      .forEach(key => (qs[key] = query[key] as string));
    const options = {
      url: processorApiConfig.http.url + uri,
      qs,
      headers: {
        Authorization: processorApiConfig.http.authToken,
      },
    };
    return new Promise((resolve, reject) => {
      request_lib
        .get(options)
        .on('error', err => {
          reject(err);
        })
        .pipe(response)
        .once('finish', () => {
          resolve(response);
        });
    });
  }
}
