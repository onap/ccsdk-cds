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

import {expect} from '@loopback/testlab';
import * as http from 'http';
import {AddressInfo} from 'net';
import {processorApiConfig} from '../src/config/app-config';
import {ResourceDictionaryDataSource} from '../src/datasources';
import config from '../src/datasources/resource-dictionary.datasource-template';
import {
  ResourceDictionaryService,
  ResourceDictionaryServiceProvider,
} from '../src/services';

describe('ResourceDictionaryService', () => {
  const entry = {name: 'hostname', dataType: 'string', tags: 'hostname'};
  let server: http.Server;
  let requests: {method?: string; url?: string}[];
  let service: ResourceDictionaryService;

  before(async () => {
    server = http.createServer((req, res) => {
      requests.push({method: req.method, url: req.url});
      if (req.method === 'GET') {
        res.writeHead(200, {'Content-Type': 'application/json'});
        res.end(JSON.stringify(entry));
      } else {
        res.writeHead(200);
        res.end();
      }
    });
    await new Promise<void>(resolve => server.listen(0, 'localhost', resolve));
    const port = (server.address() as AddressInfo).port;
    const dsConfig = JSON.parse(
      JSON.stringify(config)
        .split(processorApiConfig.http.url)
        .join(`http://localhost:${port}`),
    );
    service = await new ResourceDictionaryServiceProvider(
      new ResourceDictionaryDataSource(dsConfig),
    ).value();
  });

  beforeEach(() => {
    requests = [];
  });

  after(() => server.close());

  it('returns the dictionary entry object by name', async () => {
    const result = await service.getByName('hostname');

    expect(result).to.deepEqual(entry);
    expect(requests).to.deepEqual([
      {method: 'GET', url: '/dictionary/hostname'},
    ]);
  });

  it('deletes the dictionary entry by name', async () => {
    await service.delete('hostname');

    expect(requests).to.deepEqual([
      {method: 'DELETE', url: '/dictionary/hostname'},
    ]);
  });
});
