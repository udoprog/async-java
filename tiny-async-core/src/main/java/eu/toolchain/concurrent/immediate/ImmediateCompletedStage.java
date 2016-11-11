package eu.toolchain.concurrent.immediate;

import eu.toolchain.concurrent.AbstractImmediateCompletionStage;
import eu.toolchain.concurrent.CompletionHandle;
import eu.toolchain.concurrent.CompletionStage;
import eu.toolchain.concurrent.FutureCaller;
import eu.toolchain.concurrent.FutureFramework;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A callback which has already been resolved as 'resolved'.
 *
 * @param <T>
 */
public class ImmediateCompletedStage<T> extends AbstractImmediateCompletionStage<T>
    implements CompletionStage<T> {
  private final FutureCaller caller;
  private final T result;

  public ImmediateCompletedStage(
      final FutureFramework async, final FutureCaller caller, final T result
  ) {
    super(async);
    this.caller = caller;
    this.result = result;
  }

  @Override
  public boolean cancel() {
    return false;
  }

  @Override
  public CompletionStage<T> bind(CompletionStage<?> other) {
    return this;
  }

  @Override
  public CompletionStage<T> handle(CompletionHandle<? super T> handle) {
    caller.resolve(handle, result);
    return this;
  }

  @Override
  public CompletionStage<T> whenFinished(Runnable runnable) {
    caller.finish(runnable);
    return this;
  }

  @Override
  public CompletionStage<T> whenCancelled(Runnable runnable) {
    return this;
  }

  @Override
  public CompletionStage<T> whenCompleted(Consumer<? super T> consumer) {
    caller.resolve(consumer, result);
    return this;
  }

  @Override
  public CompletionStage<T> whenFailed(Consumer<? super Throwable> consumer) {
    return this;
  }

  @Override
  public boolean isDone() {
    return true;
  }

  @Override
  public boolean isCompleted() {
    return true;
  }

  @Override
  public boolean isFailed() {
    return false;
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public Throwable cause() {
    throw new IllegalStateException("future is not in a failed state");
  }

  @Override
  public T join() {
    return result;
  }

  @Override
  public T join(long timeout, TimeUnit unit) {
    return result;
  }

  @Override
  public T joinNow() {
    return result;
  }

  @Override
  public <R> CompletionStage<R> thenApply(Function<? super T, ? extends R> fn) {
    return transformResolved(fn, result);
  }

  @Override
  public <R> CompletionStage<R> thenCompose(
      Function<? super T, ? extends CompletionStage<R>> fn
  ) {
    return lazyTransformResolved(fn, result);
  }

  @Override
  public CompletionStage<T> thenCatchFailed(Function<? super Throwable, ? extends T> fn) {
    return this;
  }

  @Override
  public CompletionStage<T> thenComposeFailed(
      Function<? super Throwable, ? extends CompletionStage<T>> fn
  ) {
    return this;
  }

  @Override
  public CompletionStage<T> thenCatchCancelled(Supplier<? extends T> supplier) {
    return this;
  }

  @Override
  public CompletionStage<T> thenComposeCancelled(Supplier<? extends CompletionStage<T>> supplier) {
    return this;
  }
}
