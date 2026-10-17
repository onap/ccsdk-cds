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

/// <reference types="mocha" />

import {expect, sinon} from '@loopback/testlab';
import {Response} from '@loopback/rest';
import {PassThrough, Readable} from 'stream';
import request_lib = require('request');
import {processorApiConfig} from '../src/config/app-config';
import {ResolutionController} from '../src/controllers/resolution.controller';

describe('ResolutionController', () => {
  let upstream: sinon.SinonStub;
  let response: PassThrough;
  let body: string;

  beforeEach(() => {
    upstream = sinon.stub(request_lib, 'get').callsFake(((() => {
      const stream = new Readable({read() {}});
      stream.push('upstream-body');
      stream.push(null);
      return stream;
    }) as unknown) as typeof request_lib.get);
    body = '';
    response = new PassThrough();
    response.on('data', chunk => (body += chunk));
  });

  afterEach(() => sinon.restore());

  const controller = new ResolutionController();
  const asResponse = () => (response as unknown) as Response;

  it('proxies resolved resources by resolution key, dropping empty keys', async () => {
    await controller.getResolvedResources(
      'vFW',
      '1.0.0',
      'baseconfig',
      'key-1',
      '',
      (undefined as unknown) as string,
      asResponse(),
    );

    sinon.assert.calledOnce(upstream);
    const options = upstream.firstCall.args[0];
    expect(options.url).to.equal(processorApiConfig.http.url + '/resources');
    expect(options.qs).to.eql({
      bpName: 'vFW',
      bpVersion: '1.0.0',
      artifactName: 'baseconfig',
      resolutionKey: 'key-1',
    });
    expect(options.headers.Authorization).to.equal(
      processorApiConfig.http.authToken,
    );
    expect(body).to.equal('upstream-body');
  });

  it('proxies a rendered template with its occurrence', async () => {
    await controller.getResolvedTemplate(
      'vFW',
      '1.0.0',
      'baseconfig',
      '',
      'vnf',
      'vnf-1',
      '2',
      asResponse(),
    );

    const options = upstream.firstCall.args[0];
    expect(options.url).to.equal(processorApiConfig.http.url + '/template');
    expect(options.qs).to.eql({
      bpName: 'vFW',
      bpVersion: '1.0.0',
      artifactName: 'baseconfig',
      resourceType: 'vnf',
      resourceId: 'vnf-1',
      occurrence: '2',
    });
  });

  it('proxies a config snapshot by resource type, id and status', async () => {
    await controller.getConfigSnapshot(
      'PNF',
      'pnf-1',
      'CANDIDATE',
      asResponse(),
    );

    const options = upstream.firstCall.args[0];
    expect(options.url).to.equal(processorApiConfig.http.url + '/configs');
    expect(options.qs).to.eql({
      resourceType: 'PNF',
      resourceId: 'pnf-1',
      status: 'CANDIDATE',
    });
  });

  it('lists config snapshots by resource type', async () => {
    await controller.getConfigSnapshotsByType('PNF', 'RUNNING', asResponse());

    const options = upstream.firstCall.args[0];
    expect(options.url).to.equal(
      processorApiConfig.http.url + '/configs/allByType',
    );
    expect(options.qs).to.eql({resourceType: 'PNF', status: 'RUNNING'});
  });
});
