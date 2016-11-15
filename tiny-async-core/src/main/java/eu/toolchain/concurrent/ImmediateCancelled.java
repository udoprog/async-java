package eu.toolchain.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A completable which has already been cancelled.
 *
 * @param <T> type of the completable
 */
@EqualsAndHashCode(of = {}, doNotUseGetters = true, callSuper = false)
@ToString(of = {})
public class ImmediateCancelled<T> extends AbstractImmediate<T> implements Stage<T> {
  private final Caller caller;

  public ImmediateCancelled(final Caller caller) {
    super(caller);
    this.caller = caller;
  }

  @Override
  public boolean cancel() {
    return false;
  }

  @Override
  public Stage<T> whenDone(CompletionHandle<? super T> handle) {
    caller.execute(handle::cancelled);
    return this;
  }

  @Override
  public Stage<T> whenFinished(Runnable runnable) {
    caller.execute(runnable);
    return this;
  }

  @Override
  public Stage<T> whenCancelled(Runnable runnable) {
    caller.execute(runnable);
    return this;
  }

  @Override
  public Stage<T> whenComplete(Consumer<? super T> consumer) {
    return this;
  }

  @Override
  public Stage<T> whenFailed(Consumer<? super Throwable> consumer) {
    return this;
  }

  @Override
  public boolean isDone() {
    return true;
  }

  @Override
  public boolean isCompleted() {
    return false;
  }

  @Override
  public boolean isFailed() {
    return false;
  }

  @Override
  public boolean isCancelled() {
    return true;
  }

  @Override
  public Throwable cause() {
    throw new IllegalStateException("completable is not in a failed state");
  }

  @Override
  public T join() {
    throw new CancellationException();
  }

  @Override
  public T join(long timeout, TimeUnit unit) {
    throw new CancellationException();
  }

  @Override
  public T joinNow() {
    throw new CancellationException();
  }

  @Override
  public <U> Stage<U> thenApply(Function<? super T, ? extends U> fn) {
    return new ImmediateCancelled<>(caller);
  }

  @Override
  public <U> Stage<U> thenCompose(
      Function<? super T, ? extends Stage<U>> fn
  ) {
    return new ImmediateCancelled<>(caller);
  }

  @Override
  public Stage<T> thenApplyFailed(Function<? super Throwable, ? extends T> fn) {
    return this;
  }

  @Override
  public Stage<T> thenComposeFailed(
      Function<? super Throwable, ? extends Stage<T>> fn
  ) {
    return this;
  }

  @Override
  public Stage<T> thenApplyCancelled(Supplier<? extends T> supplier) {
    return immediateCatchCancelled(supplier);
  }

  @Override
  public Stage<T> thenComposeCancelled(Supplier<? extends Stage<T>> supplier) {
    return immediateComposeCancelled(supplier);
  }
}
