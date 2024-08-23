package com.github.brickwall2900.birthdays;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class Wrapper {
    public static Runnable wrap(Runnable runnable) {
        return runnable;
    }

    public static Runnable wrapException(ThrowableRunnable runnable) {
        return () -> {
            try { runnable.run(); } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static Runnable wrap(Consumer<? super Varargs> callback, Object... objects) {
        return () -> callback.accept(new Varargs(objects));
    }

    public static <T> Runnable wrap(Consumer<T> callback, T object) {
        return () -> callback.accept(object);
    }

    public static <T, U> Runnable wrap(BiConsumer<T, U> callback, T object1, U object2) {
        return () -> callback.accept(object1, object2);
    }

    public static <T, U, V> Runnable wrap(TriConsumer<T, U, V> callback, T object1, U object2, V object3) {
        return () -> callback.apply(object1, object2, object3);
    }

    public static <T> Consumer<Object> wrapIndirect(Consumer<T> callback, T object) {
        return o -> callback.accept(object);
    }

    public static <T> Consumer<Object> wrapConsumerToVoid(Runnable runnable) {
        return o -> runnable.run();
    }

    public static <V> Runnable wrapReturnable(Callable<V> callable, AtomicReference<V> reference) {
        return () -> {
            try {
                reference.set(callable.call());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T, U, V> Runnable wrapReturnable(BiFunction<T, U, V> func, AtomicReference<V> reference, T arg1, U arg2) {
        return () -> {
            try {
                reference.set(func.apply(arg1, arg2));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T, U, R> Callable<R> wrapCallable(BiFunction<T, U, R> func, T arg1, U arg2) {
        return () -> func.apply(arg1, arg2);
    }

    public static Runnable wrapException(ThrowableRunnable runnable, Consumer<Exception> errorHandler) {
        return () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                if (errorHandler != null) {
                    errorHandler.accept(e);
                }
            }
        };
    }

    public static class Varargs {
        private final Object[] objects;

        public Varargs(Object[] objects) {
            this.objects = objects;
        }

        public <T> T get(int i, Class<T> as) {
            Objects.requireNonNull(as);

            Object o = objects[i];
            if (o == null) { return null; }
            if (as.isInstance(o)) {
                return as.cast(o);
            } else {
                throw new ClassCastException("Cannot cast object that is " + o.getClass() + " to " + as);
            }
        }
    }

    @FunctionalInterface
    public interface TriConsumer<T, U, V> {
        void apply(T arg1, U arg2, V arg3);
    }

    @FunctionalInterface
    public interface ThrowableRunnable {
        void run() throws Exception;
    }
}
