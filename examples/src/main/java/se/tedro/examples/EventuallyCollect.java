package se.tedro.examples;

import se.tedro.examples.helpers.Helpers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import se.tedro.concurrent.Async;
import se.tedro.concurrent.Stage;

/**
 * An example application showcasing using a eventual collection.
 *
 * Eventual collection takes stream collection one step further and defers the async call to
 * guarantee that only a given number of asynchronous operations may be pending at the same time.
 */
public class EventuallyCollect {
  public static void main(String[] argv) throws InterruptedException, ExecutionException {
    final Async async = Helpers.setup();

    final List<Callable<Stage<Integer>>> futures = new ArrayList<>();

    for (int i = 0; i < 100; i++) {
      final int value = i;

      futures.add(() -> async.call(() -> value));
    }

    final AtomicInteger integer = new AtomicInteger();

    final Stage<Integer> sum =
      async.eventuallyCollect(futures, integer::addAndGet, integer::get, 2);

    System.out.println("result: " + sum.join());
  }
}
