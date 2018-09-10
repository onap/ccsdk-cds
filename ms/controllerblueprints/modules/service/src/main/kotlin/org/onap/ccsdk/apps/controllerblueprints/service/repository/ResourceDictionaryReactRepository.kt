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

package org.onap.ccsdk.apps.controllerblueprints.service.repository

import org.onap.ccsdk.apps.controllerblueprints.service.domain.ResourceDictionary
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * ResourceDictionaryReactRepository.
 *
 * @author Brinda Santh
 */
@Service
open class ResourceDictionaryReactRepository(private val resourceDictionaryRepository: ResourceDictionaryRepository) {

    fun save(resourceDictionary: ResourceDictionary): Mono<ResourceDictionary> {
        return Mono.justOrEmpty(resourceDictionaryRepository.save(resourceDictionary))
    }

    fun findByName(name: String): Mono<ResourceDictionary> {
        return Mono.justOrEmpty(resourceDictionaryRepository.findByName(name))
    }

    fun findByNameIn(names: List<String>): Flux<ResourceDictionary> {
        return Flux.fromIterable(resourceDictionaryRepository.findByNameIn(names))
                .subscribeOn(Schedulers.elastic())
    }

    fun findByTagsContainingIgnoreCase(tags: String): Flux<ResourceDictionary> {
        return Flux.fromIterable(resourceDictionaryRepository.findByTagsContainingIgnoreCase(tags))
                .subscribeOn(Schedulers.elastic())
    }

    fun deleteByName(name: String): Mono<Void> {
        resourceDictionaryRepository.deleteByName(name)
        return Mono.empty()
    }

}