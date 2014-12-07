package eu.toolchain.async.proxies;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.RequiredArgsConstructor;
import eu.toolchain.async.AsyncFuture;
import eu.toolchain.async.FutureCancelled;
import eu.toolchain.async.FutureDone;
import eu.toolchain.async.FutureFinished;
import eu.toolchain.async.LazyTransform;
import eu.toolchain.async.TinyAsync;
import eu.toolchain.async.Transform;

@RequiredArgsConstructor
public class TransformCancelledFutureProxy<T> implements AsyncFuture<T> {
    private final TinyAsync async;
    private final AsyncFuture<T> source;
    private final Transform<Void, T> transform;

    /* transition */

    @Override
    public boolean cancel() {
        return source.cancel();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return source.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean fail(Throwable error) {
        return source.fail(error);
    }

    /* register listeners */

    @Override
    public AsyncFuture<T> on(final FutureDone<T> handle) {
        source.on(new FutureDone<T>() {
            @Override
            public void failed(Throwable cause) throws Exception {
                handle.failed(cause);
            }

            @Override
            public void resolved(T result) throws Exception {
                handle.resolved(result);
            }

            @Override
            public void cancelled() throws Exception {
                final T result;

                try {
                    result = transform.transform(null);
                } catch (Exception inner) {
                    handle.failed(inner);
                    return;
                }

                handle.resolved(result);
            }
        });

        return this;
    }

    @Override
    public AsyncFuture<T> onAny(FutureDone<?> handle) {
        source.onAny(handle);
        return this;
    }

    @Override
    public AsyncFuture<T> on(FutureFinished finishable) {
        source.on(finishable);
        return this;
    }

    @Override
    public AsyncFuture<T> on(FutureCancelled cancelled) {
        source.on(cancelled);
        return this;
    }

    /* check state */

    @Override
    public boolean isDone() {
        return source.isDone();
    }

    @Override
    public boolean isCancelled() {
        return source.isCancelled();
    }

    /* get result */

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return source.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return source.get(timeout, unit);
    }

    @Override
    public T getNow() throws ExecutionException {
        return source.getNow();
    }

    /* transform values */

    @Override
    public <C> AsyncFuture<C> transform(Transform<T, C> transform) {
        return async.transform(this, transform);
    }

    @Override
    public <C> AsyncFuture<C> transform(LazyTransform<T, C> transform) {
        return async.transform(this, transform);
    }

    @Override
    public AsyncFuture<T> error(Transform<Throwable, T> transform) {
        return async.error(this, transform);
    }

    @Override
    public AsyncFuture<T> error(LazyTransform<Throwable, T> transform) {
        return async.error(this, transform);
    }

    @Override
    public AsyncFuture<T> cancelled(Transform<Void, T> transform) {
        return async.cancelled(this, transform);
    }

    @Override
    public AsyncFuture<T> cancelled(LazyTransform<Void, T> transform) {
        return async.cancelled(this, transform);
    }
}