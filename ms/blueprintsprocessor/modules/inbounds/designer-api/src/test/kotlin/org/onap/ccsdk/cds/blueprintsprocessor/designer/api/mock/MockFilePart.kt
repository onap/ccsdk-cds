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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.mock

import org.apache.commons.io.FilenameUtils
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.codec.multipart.FilePart
import org.springframework.util.FileCopyUtils
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.io.File
import java.nio.file.Path

class MockFilePart(private val fileName: String) : FilePart {

    val log = LoggerFactory.getLogger(MockFilePart::class.java)!!
    override fun content(): Flux<DataBuffer> {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun headers(): HttpHeaders {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun filename(): String {
        return FilenameUtils.getName(fileName)
    }

    override fun name(): String {
        return FilenameUtils.getBaseName(fileName)
    }

    override fun transferTo(path: Path): Mono<Void> {
        log.info("Copying file($fileName to $path")
        FileCopyUtils.copy(File(fileName), path.toFile())
        return Mono.empty()
    }
}
