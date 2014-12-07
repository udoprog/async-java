package eu.toolchain.async;

import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;

/**
 * A callback which has already been resolved as 'resolved'.
 *
 * @param <T>
 */
@RequiredArgsConstructor
public class ResolvedFuture<T> implements AsyncFuture<T> {
    private final AsyncFramework async;
    private final AsyncCaller caller;
    private final T value;

    /* transition */

    @Override
    public boolean fail(Throwable error) {
        return false;
    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    /* register listeners */

    @Override
    public AsyncFuture<T> on(FutureDone<T> handle) {
        caller.resolveFutureDone(handle, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AsyncFuture<T> onAny(FutureDone<?> handle) {
        return on((FutureDone<T>) handle);
    }

    @Override
    public AsyncFuture<T> on(FutureFinished finishable) {
        caller.runFutureFinished(finishable);
        return this;
    }

    @Override
    public AsyncFuture<T> on(FutureCancelled cancelled) {
        return this;
    }

    /* check state */

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    /* get value */

    @Override
    public T get() {
        return value;
    }

    @Override
    public T get(long timeout, TimeUnit unit) {
        return value;
    }

    @Override
    public T getNow() {
        return value;
    }

    /* transform */

    @Override
    public <C> AsyncFuture<C> transform(LazyTransform<T, C> transform) {
        try {
            return transform.transform(value);
        } catch (Exception e) {
            return async.failed(e, caller);
        }
    }

    @Override
    public <C> AsyncFuture<C> transform(Transform<T, C> transform) {
        C result;

        try {
            result = transform.transform(value);
        } catch (Exception e) {
            return async.failed(e, caller);
        }

        return async.resolved(result, caller);
    }

    @Override
    public AsyncFuture<T> error(Transform<Throwable, T> transform) {
        return this;
    }

    @Override
    public AsyncFuture<T> error(LazyTransform<Throwable, T> transform) {
        return this;
    }

    @Override
    public AsyncFuture<T> cancelled(Transform<Void, T> transform) {
        return this;
    }

    @Override
    public AsyncFuture<T> cancelled(LazyTransform<Void, T> transform) {
        return this;
    }
}