package io.yodata.ldp.solid.server.model;

import java.util.Objects;
import java.util.function.Supplier;

public class LazyLoadProvider<T> implements Supplier<T> {

    private Supplier<T> builder;
    private T obj;

    public LazyLoadProvider(Supplier<T> builder) {
        this.builder = builder;
    }

    @Override
    public T get() {
        synchronized (this) {
            if (Objects.isNull(obj)) {
                obj = builder.get();
            }
        }

        return obj;
    }

}

