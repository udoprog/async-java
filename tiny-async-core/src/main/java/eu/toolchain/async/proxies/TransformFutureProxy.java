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
public class TransformFutureProxy<S, T> implements AsyncFuture<T> {
    private final TinyAsync async;
    private final AsyncFuture<S> source;
    private final Transform<S, T> transform;

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
        source.on(new FutureDone<S>() {
            @Override
            public void failed(Throwable e) throws Exception {
                handle.failed(e);
            }

            @Override
            public void resolved(S result) throws Exception {
                try {
                    handle.resolved(transform.transform(result));
                } catch (Exception e) {
                    handle.failed(e);
                }
            }

            @Override
            public void cancelled() throws Exception {
                handle.cancelled();
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
        return null;
    }

    /* check state */

    @Override
    public boolean isCancelled() {
        return source.isCancelled();
    }

    @Override
    public boolean isDone() {
        return source.isDone();
    }

    /* get result */

    @Override
    public T get() throws InterruptedException, ExecutionException {
        final S result = source.get();

        try {
            return transform.transform(result);
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        final S result = source.get(timeout, unit);

        try {
            return transform.transform(result);
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public T getNow() throws ExecutionException {
        final S result = source.getNow();

        try {
            return transform.transform(result);
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }

    /* transform */

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