package com.observeai.platform.realtime.neutrino.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Either <T> {

    private final T entity;
    private final Error error;


    public static <T> Either<T> buildEntity(T entity) {
        return new Either<>(entity, null);
    }

    public static <T> Either<T> buildError(Error error) {
        return new Either<>(null, error);
    }

    public boolean hasError() {
        return this.error != null;
    }

}
