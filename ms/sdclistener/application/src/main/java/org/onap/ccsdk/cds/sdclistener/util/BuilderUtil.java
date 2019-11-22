/*
 * Copyright © 2019 Bell Canada
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.cds.sdclistener.util;

import java.util.function.Consumer;

/**
 * A generic builder for constructing an object.
 *
 * @param <T> - Any object
 */
public class BuilderUtil<T> {

    private final T object;

    public BuilderUtil(T instance) {
        this.object = instance;
    }

    public BuilderUtil<T> build(Consumer<T> consumer) {
        consumer.accept(object);
        return this;
    }

    public T create() {
        return object;
    }

}
