/*
 * Copyright (C) 2019 Bell Canada. All rights reserved.
 *
 * NOTICE:  All the intellectual and technical concepts contained herein are
 * proprietary to Bell Canada and are protected by trade secret or copyright law.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package org.onap.ccsdk.cds.cdssdclistener.util;

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
