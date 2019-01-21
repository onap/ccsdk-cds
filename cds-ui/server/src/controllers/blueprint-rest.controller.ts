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



import {
  Count,
  CountSchema,
  Filter,
  repository,
  Where,
} from '@loopback/repository';
import {
  post,
  param,
  get,
  getFilterSchemaFor,
  getWhereSchemaFor,
  patch,
  put,
  del,
  requestBody,
} from '@loopback/rest';
import {Blueprint} from '../models';
import {BlueprintRepository} from '../repositories';

export class BlueprintRestController {
  constructor(
    @repository(BlueprintRepository)
    public blueprintRepository : BlueprintRepository,
  ) {}

  @post('/blueprints', {
    responses: {
      '200': {
        description: 'Blueprint model instance',
        content: {'application/json': {schema: {'x-ts-type': Blueprint}}},
      },
    },
  })
  async create(@requestBody() blueprint: Blueprint): Promise<Blueprint> {
    return await this.blueprintRepository.create(blueprint);
  }

  @get('/blueprints/count', {
    responses: {
      '200': {
        description: 'Blueprint model count',
        content: {'application/json': {schema: CountSchema}},
      },
    },
  })
  async count(
    @param.query.object('where', getWhereSchemaFor(Blueprint)) where?: Where,
  ): Promise<Count> {
    return await this.blueprintRepository.count(where);
  }

  @get('/blueprints', {
    responses: {
      '200': {
        description: 'Array of Blueprint model instances',
        content: {
          'application/json': {
            schema: {type: 'array', items: {'x-ts-type': Blueprint}},
          },
        },
      },
    },
  })
  async find(
    @param.query.object('filter', getFilterSchemaFor(Blueprint)) filter?: Filter,
  ): Promise<Blueprint[]> {
    return await this.blueprintRepository.find(filter);
  }

  @patch('/blueprints', {
    responses: {
      '200': {
        description: 'Blueprint PATCH success count',
        content: {'application/json': {schema: CountSchema}},
      },
    },
  })
  async updateAll(
    @requestBody() blueprint: Blueprint,
    @param.query.object('where', getWhereSchemaFor(Blueprint)) where?: Where,
  ): Promise<Count> {
    return await this.blueprintRepository.updateAll(blueprint, where);
  }

  @get('/blueprints/{id}', {
    responses: {
      '200': {
        description: 'Blueprint model instance',
        content: {'application/json': {schema: {'x-ts-type': Blueprint}}},
      },
    },
  })
  async findById(@param.path.number('id') id: number): Promise<Blueprint> {
    return await this.blueprintRepository.findById(id);
  }

  @patch('/blueprints/{id}', {
    responses: {
      '204': {
        description: 'Blueprint PATCH success',
      },
    },
  })
  async updateById(
    @param.path.number('id') id: number,
    @requestBody() blueprint: Blueprint,
  ): Promise<void> {
    await this.blueprintRepository.updateById(id, blueprint);
  }

  @put('/blueprints/{id}', {
    responses: {
      '204': {
        description: 'Blueprint PUT success',
      },
    },
  })
  async replaceById(
    @param.path.number('id') id: number,
    @requestBody() blueprint: Blueprint,
  ): Promise<void> {
    await this.blueprintRepository.replaceById(id, blueprint);
  }

  @del('/blueprints/{id}', {
    responses: {
      '204': {
        description: 'Blueprint DELETE success',
      },
    },
  })
  async deleteById(@param.path.number('id') id: number): Promise<void> {
    await this.blueprintRepository.deleteById(id);
  }
}
