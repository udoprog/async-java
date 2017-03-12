package se.tedro.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Coordinator thread for handling delayed callables executing with a given parallelism.
 *
 * @param <S> The source type being collected.
 * @param <T> The target type the source type is being collected into.
 */
public class DelayedCollectCoordinator<S, T> implements Handle<S>, Runnable {
  /* lock that must be acquired before using {@link callables} */
  private final Object lock = new Object();

  private final Caller caller;
  private final Iterator<? extends Callable<? extends Stage<? extends S>>> tasks;
  private final Consumer<? super S> consumer;
  private final Supplier<? extends T> supplier;
  private final Completable<? super T> future;
  private final int parallelism;

  volatile boolean cancel = false;
  volatile boolean done = false;
  volatile int pending = 0;

  public DelayedCollectCoordinator(
    final Caller caller, final Collection<? extends Callable<? extends Stage<? extends S>>> tasks,
    final Consumer<S> consumer, Supplier<T> supplier, final Completable<? super T> future,
    int parallelism
  ) {
    this.caller = caller;
    this.tasks = tasks.iterator();
    this.consumer = consumer;
    this.supplier = supplier;
    this.future = future;
    this.parallelism = parallelism;
  }

  @Override
  public void failed(Throwable cause) {
    next(() -> {
      pending--;
      cancel = true;
    });
  }

  @Override
  public void completed(S result) {
    caller.execute(() -> consumer.accept(result));
    next(() -> pending--);
  }

  @Override
  public void cancelled() {
    next(() -> {
      pending--;
      cancel = true;
    });
  }

  // coordinate thread.
  @Override
  public void run() {
    final List<Callable<? extends Stage<? extends S>>> initial = new ArrayList<>();

    synchronized (lock) {
      for (int i = 0; i < parallelism && tasks.hasNext(); i++) {
        initial.add(tasks.next());
      }

      pending += initial.size();
    }

    initial.forEach(this::call);

    future.whenCancelled(() -> {
      next(() -> cancel = true);
    });
  }

  private void next(final Runnable effect) {
    final Callable<? extends Stage<? extends S>> next;

    synchronized (lock) {
      effect.run();

      if (cancel || !tasks.hasNext()) {
        end();
        return;
      }

      pending++;
      next = tasks.next();
    }

    call(next);
  }

  private void call(final Callable<? extends Stage<? extends S>> next) {
    final Stage<? extends S> f;

    try {
      f = next.call();
    } catch (final Exception e) {
      failed(e);
      return;
    }

    f.handle(this);
  }

  private void end() {
    if (pending > 0 || done) {
      return;
    }

    done = true;

    final T result;

    try {
      result = supplier.get();
    } catch (final Exception e) {
      future.fail(e);
      return;
    }

    future.complete(result);
  }
}
