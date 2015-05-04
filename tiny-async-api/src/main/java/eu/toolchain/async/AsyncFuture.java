package eu.toolchain.async;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * Interface for asynchronous futures with the ability to subscribe to interesting events.
 *
 * The available events are.
 *
 * <ul>
 * <li>resolved, for when a future has been resolved with a value.</li>
 * <li>failed, for when a future failed to resolve because of an exception.</li>
 * </ul>
 *
 * @param <T> The type being realized in the future's finish method.
 */
public interface AsyncFuture<T> extends java.util.concurrent.Future<T> {
    /**
     * Fail the future.
     *
     * @return {@code true} if the future was failed because of this call. {@code false} otherwise.
     */
    public boolean fail(Throwable error);

    /**
     * Cancel the future.
     *
     * @return {@code true} if the future was cancelled because of this call. {@code false} otherwise.
     */
    public boolean cancel();

    /**
     * Get the result of the future.
     *
     * @throws IllegalStateException if the result is not available.
     * @throws ExecutionException if the computation threw an exception.
     */
    public T getNow() throws ExecutionException, CancellationException;

    /**
     * Register a listener to be called when this future finishes for any reason.
     *
     * @param finishable Function to be fired.
     * @return This future.
     */
    public AsyncFuture<T> on(FutureFinished finishable);

    /**
     * Register an listener to be called when this future is cancelled.
     *
     * @param cancelled Listener to fire.
     * @return This future.
     */
    public AsyncFuture<T> on(FutureCancelled cancelled);

    /**
     * Register a listener to be called when this future is resolved.
     *
     * @param resolved Listener to fire.
     * @return This future.
     */
    public AsyncFuture<T> on(FutureResolved<? super T> resolved);

    /**
     * Register a listener that is called on all three types of events for this future; resolved, failed, and cancelled.
     *
     * @param done Listener to fire.
     * @return This future.
     */
    public AsyncFuture<T> on(FutureDone<? super T> done);

    /**
     * Registers a listener to be called when this future finishes.
     *
     * The type of the listener is ignored.
     *
     * @param done Listener to fire.
     * @return This future.
     */
    public AsyncFuture<T> onAny(FutureDone<?> done);

    /**
     * Transforms the value of one future into another using a deferred transformer function.
     *
     * <pre>
     * Future<T> (this) - *using deferred transformer* -> Future<C>
     * </pre>
     *
     * A deferred transformer is expected to return a compatible future that when resolved will resolve the future that
     * this function returns.
     *
     * <pre>
     * {@code
     *   Future<Integer> first = asyncOperation();
     * 
     *   Future<Double> second = first.transform(new Transformer<Integer, Double>() {
     *     void transform(Integer result, Future<Double> future) {
     *       future.finish(result.doubleValue());
     *     }
     *   };
     * 
     *   # use second
     * }
     * </pre>
     *
     * @param transform The function to use when transforming the value.
     * @return A future of type <C> which resolves with the transformed value.
     */
    public <C> AsyncFuture<C> transform(LazyTransform<? super T, ? extends C> transform);

    /**
     * Transforms the value of this future into another type using a transformer function.
     *
     * <pre>
     * Future<T> (this) - *using transformer* -> Future<C>
     * </pre>
     *
     * Use this if the transformation performed does not require any more async operations.
     *
     * <pre>
     * {@code
     *   Future<Integer> first = asyncOperation();
     * 
     *   Future<Double> second = future.transform(new Transformer<Integer, Double>() {
     *     Double transform(Integer result) {
     *       return result.doubleValue();
     *     }
     *   };
     * 
     *   # use second
     * }
     * </pre>
     *
     * @param transform
     * @return
     */
    public <C> AsyncFuture<C> transform(Transform<? super T, ? extends C> transform);

    /**
     * Transform an error into something useful.
     *
     * @param transform The transformation to use.
     */
    public AsyncFuture<T> error(Transform<Throwable, ? extends T> transform);

    /**
     * Transform an error into something useful.
     *
     * @param transform The transformation to use.
     */
    public AsyncFuture<T> error(LazyTransform<Throwable, ? extends T> transform);

    /**
     * Transform something cancelled into something useful.
     *
     * @param transform The transformation to use.
     */
    public AsyncFuture<T> cancelled(Transform<Void, ? extends T> transform);

    /**
     * Transform something cancelled into something useful using a lazy operation.
     *
     * @param transform The transformation to use.
     */
    public AsyncFuture<T> cancelled(LazyTransform<Void, ? extends T> transform);
}
