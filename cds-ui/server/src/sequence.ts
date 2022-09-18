/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2018-19 IBM Intellectual Property. All rights reserved.
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

import { inject } from '@loopback/context';
import {
  FindRoute,
  InvokeMethod,
  ParseParams,
  Reject,
  RequestContext,
  RestBindings,
  Send,
  SequenceHandler,
} from '@loopback/rest';
import { logger } from './logger/logger';
import { v4 as uuid } from 'uuid';

const SequenceActions = RestBindings.SequenceActions;

export class MySequence implements SequenceHandler {
  constructor(
    @inject(SequenceActions.FIND_ROUTE) protected findRoute: FindRoute,
    @inject(SequenceActions.PARSE_PARAMS) protected parseParams: ParseParams,
    @inject(SequenceActions.INVOKE_METHOD) protected invoke: InvokeMethod,
    @inject(SequenceActions.SEND) public send: Send,
    @inject(SequenceActions.REJECT) public reject: Reject,
  ) { }

  async handle(context: RequestContext) {
    const { request, response } = context;
    try {
      if (!('X-ONAP-RequestID' in request.headers || 'x-onap-requestid' in request.headers)) {
        request.headers = { 'X-ONAP-RequestID': uuid(), ...request.headers}
        logger.info(JSON.stringify(request.headers))
      }
      const route = this.findRoute(request);
      const args = await this.parseParams(request, route);
      const result = await this.invoke(route, args);
      this.send(response, result);
    } catch (err: unknown) {
      if (err instanceof Error) {
        this.reject(context, err);
      }
    } finally {
      const { authorization, ...headers} = request.headers;
      logger.info("Incoming request from %s %s and with header %s query %s params %s and response code: %s",
        request.method, request.url, JSON.stringify(headers), JSON.stringify(request.query), JSON.stringify(request.params), JSON.stringify(response.statusCode))
    }
  }
}
