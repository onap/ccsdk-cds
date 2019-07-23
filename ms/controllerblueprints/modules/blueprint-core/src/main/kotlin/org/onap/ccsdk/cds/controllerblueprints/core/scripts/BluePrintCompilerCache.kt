/*
 *  Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.controllerblueprints.core.scripts

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import java.net.URL
import java.net.URLClassLoader


object BluePrintCompileCache {
    val log = logger(BluePrintCompileCache::class)

    private val classLoaderCache: LoadingCache<String, URLClassLoader> = CacheBuilder.newBuilder()
            .maximumSize(10)
            .build(BluePrintClassLoader)

    fun classLoader(key: String): URLClassLoader {
        return classLoaderCache.get(key)
    }

    fun cleanClassLoader(key: String) {
        classLoaderCache.invalidate(key)
        log.info("Cleaned script cache($key)")
    }

    fun hasClassLoader(key: String): Boolean {
        return classLoaderCache.asMap().containsKey(key)
    }
}

object BluePrintClassLoader : CacheLoader<String, URLClassLoader>() {

    val log = logger(BluePrintClassLoader::class)

    override fun load(key: String): URLClassLoader {
        log.info("loading cache key($key)")
        val keyPath = normalizedFile(key)
        if (!keyPath.exists()) {
            throw BluePrintException("failed to load cache($key), missing files.")
        }
        val urls = arrayListOf<URL>()
        keyPath.walkTopDown()
                .filter { it.name.endsWith("cba-kts.jar") }
                .forEach {
                    log.debug("Adding (${it.absolutePath}) to cache key($key)")
                    urls.add(it.toURI().toURL())
                }
        return URLClassLoader(urls.toTypedArray(), this.javaClass.classLoader)
    }
}