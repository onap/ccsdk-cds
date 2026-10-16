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
import {FindRoute, RequestContext, ResolvedRoute} from '@loopback/rest';
import {logger} from '../src/logger/logger';
import {MySequence} from '../src/sequence';

describe('MySequence', () => {
  afterEach(() => sinon.restore());

  it('logs the request without credentials', async () => {
    const info = sinon.stub(logger, 'info');
    const findRoute: FindRoute = () => ({} as ResolvedRoute);
    const sequence = new MySequence(
      findRoute,
      async () => [],
      async () => 'result',
      () => {},
      () => {},
    );
    const request = {
      method: 'GET',
      path: '/api/v1/blueprint-model',
      url: '/api/v1/blueprint-model?access_token=query-secret',
      headers: {
        authorization: 'Basic aGVhZGVyLXNlY3JldA==',
        cookie: 'session=cookie-secret',
      },
      query: {access_token: 'query-secret'},
      params: {},
    };

    await sequence.handle(({
      request,
      response: {statusCode: 200},
    } as unknown) as RequestContext);

    sinon.assert.calledOnce(info);
    const logged = JSON.stringify(info.firstCall.args);
    expect(logged).to.containEql('GET');
    expect(logged).to.containEql('/api/v1/blueprint-model');
    expect(logged).to.containEql('200');
    expect(logged).to.not.match(/secret|aGVhZGVy/);
  });
});
