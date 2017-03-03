package se.tedro.examples;

import se.tedro.examples.helpers.Helpers;
import java.util.concurrent.ExecutionException;
import se.tedro.concurrent.Completable;
import se.tedro.concurrent.CoreAsync;
import se.tedro.concurrent.Stage;

/**
 * An example application showcasing manually resolving a {@code ResolvableFuture}.
 */
public class SomethingReckless {
  public static Stage<Integer> somethingReckless(final CoreAsync async) {
    final Completable<Integer> future = async.completable();

    // access the configured executor.
    async.executor().execute(() -> future.complete(42));

    return future;
  }

  public static void main(String[] argv) throws InterruptedException, ExecutionException {
    final CoreAsync async = Helpers.setup();

    System.out.println(somethingReckless(async).join());
  }
}
