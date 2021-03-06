package se.tedro.examples;

import se.tedro.examples.helpers.Helpers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import se.tedro.concurrent.Async;
import se.tedro.concurrent.Stage;

/**
 * An example application showcasing using stream collection.
 *
 * Stream collection avoids having to setup a complete collection of results by accumulating each
 * individual result as it becomes available.
 */
public class StreamCollect {
  public static void main(String[] argv) throws InterruptedException, ExecutionException {
    final Async async = Helpers.setup();

    final List<Stage<Integer>> futures = new ArrayList<>();

    for (int i = 0; i < 100; i++) {
      final int value = i;

      futures.add(async.call(() -> value));
    }

    final AtomicInteger integer = new AtomicInteger();

    final Stage<Integer> sum = async.streamCollect(futures, integer::addAndGet, integer::get);

    System.out.println("result: " + sum.join());
  }
}
