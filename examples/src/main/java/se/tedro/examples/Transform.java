package se.tedro.examples;

import se.tedro.examples.helpers.Helpers;
import java.util.function.Function;
import se.tedro.concurrent.Async;
import se.tedro.concurrent.Stage;

/**
 * An example application showcasing transforms.
 */
public class Transform {
  public static void main(String[] argv) throws Exception {
    final Async async = Helpers.setup();

    final Function<Integer, Integer> addTen = i -> i + 10;

    final Stage<Integer> f = async.call(() -> 10);

    System.out.println("result: " + f.thenApply(addTen).join());
  }
}
