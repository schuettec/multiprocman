package com.github.schuettec.multiprocman.common;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This class defines the {@link CumulatedEventJoin} that joins multiple events into one by taking a specified maximum
 * event frequency into account.
 */
public class CumulatedEventJoin<T> extends EventJoin {

    private BiFunction<T, T, T> cumulationFunction;
    private Consumer<T> consumer;

    private Object lock = new Object();
    private T currentValue;
    private Supplier<T> initialValueSupplier;

    public CumulatedEventJoin(Supplier<T> initialValueSupplier, BiFunction<T, T, T> cumulationFunction,
            Consumer<T> consumer, long frequency, TimeUnit unit) {
        super(frequency, unit);
        requireNonNull(cumulationFunction, "cumulation function must not be null!");
        requireNonNull(consumer, "consumer  must not be null!");
        this.initialValueSupplier = initialValueSupplier;
        this.currentValue = initialValueSupplier.get();
        this.cumulationFunction = cumulationFunction;
        this.consumer = consumer;
    }

    public void noticeEvent(T value) {
        synchronized (lock) {
            this.currentValue = cumulationFunction.apply(currentValue, value);
            this.noticeEvent();
        }
    }

    @Override
    public void acceptEvent() {
        synchronized (lock) {
            consumer.accept(currentValue);
            this.currentValue = initialValueSupplier.get();
        }
    }

    public static <T> BiFunction<T, T, T> returnOnNull(BiFunction<T, T, T> function) {
        return (s, t) -> {
            if (isNull(s) || isNull(t)) {
                if (isNull(s)) {
                    return t;
                } else {
                    return s;
                }
            }
            return function.apply(s, t);
        };
    }
}
