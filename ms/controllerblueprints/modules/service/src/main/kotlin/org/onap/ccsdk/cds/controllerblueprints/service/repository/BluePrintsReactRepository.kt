/*
 *  Copyright © 2018 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.controllerblueprints.service.repository

import org.onap.ccsdk.cds.controllerblueprints.service.domain.ModelType
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * ModelTypeReactRepository.
 *
 * @author Brinda Santh
 */
@Service
open class ModelTypeReactRepository(private val modelTypeRepository: ModelTypeRepository) {

    fun save(modelType: ModelType): Mono<ModelType> {
        return Mono.justOrEmpty(modelTypeRepository.save(modelType))
    }

    fun findByModelName(modelName: String): Mono<ModelType> {
        return Mono.justOrEmpty(modelTypeRepository.findByModelName(modelName))
    }

    fun findByModelNameIn(modelNames: List<String>): Flux<ModelType> {
        return Flux.fromIterable(modelTypeRepository.findByModelNameIn(modelNames))
                .subscribeOn(Schedulers.elastic())
    }

    fun findByDerivedFrom(derivedFrom: String): Flux<ModelType> {
        return Flux.fromIterable(modelTypeRepository.findByDerivedFrom(derivedFrom))
                .subscribeOn(Schedulers.elastic())
    }

    fun findByDerivedFromIn(derivedFroms: List<String>): Flux<ModelType> {
        return Flux.fromIterable(modelTypeRepository.findByDerivedFromIn(derivedFroms))
                .subscribeOn(Schedulers.elastic())
    }

    fun findByDefinitionType(definitionType: String): Flux<ModelType> {
        return Flux.fromIterable(modelTypeRepository.findByDefinitionType(definitionType))
                .subscribeOn(Schedulers.elastic())
    }

    fun findByDefinitionTypeIn(definitionTypes: List<String>): Flux<ModelType> {
        return Flux.fromIterable(modelTypeRepository.findByDefinitionTypeIn(definitionTypes))
                .subscribeOn(Schedulers.elastic())
    }

    fun findByTagsContainingIgnoreCase(tags: String): Flux<ModelType> {
        return Flux.fromIterable(modelTypeRepository.findByTagsContainingIgnoreCase(tags))
                .subscribeOn(Schedulers.elastic())
    }

    fun deleteByModelName(modelName: String): Mono<Void> {
        modelTypeRepository.deleteByModelName(modelName)
        return Mono.empty()
    }

}