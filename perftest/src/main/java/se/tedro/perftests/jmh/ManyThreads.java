package se.tedro.perftests.jmh;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.openjdk.jmh.annotations.Benchmark;
import se.tedro.concurrent.Async;
import se.tedro.concurrent.CoreAsync;
import se.tedro.concurrent.Stage;

public class ManyThreads {
  private static final int SIZE = 1000;
  private static final int EXPECTED_SUM = 499500;

  private static int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

  @Benchmark
  public void async() throws Exception {
    final ExecutorService executor = Executors.newWorkStealingPool(THREAD_COUNT);
    final Async async = CoreAsync.builder().executor(executor).build();

    final List<Stage<Integer>> futures = new ArrayList<>();

    for (int i = 0; i < SIZE; i++) {
      final int current = i;

      futures.add(async.call(() -> current));
    }

    int sum = 0;

    for (final Stage<Integer> future : futures) {
      sum += future.join();
    }

    if (sum != EXPECTED_SUM) {
      throw new IllegalStateException("did not properly collect all values");
    }

    executor.shutdown();
  }

  @Benchmark
  public void guava() throws Exception {
    final ExecutorService executor = Executors.newWorkStealingPool(THREAD_COUNT);
    final ListeningExecutorService listeningExecutor = MoreExecutors.listeningDecorator(executor);

    final List<ListenableFuture<Integer>> futures = new ArrayList<>();

    for (int i = 0; i < SIZE; i++) {
      final int current = i;

      futures.add(listeningExecutor.submit(() -> current));
    }

    int sum = 0;

    for (final ListenableFuture<Integer> future : futures) {
      sum += future.get();
    }

    if (sum != EXPECTED_SUM) {
      throw new IllegalStateException("did not properly collect all values");
    }

    listeningExecutor.shutdown();
  }

  @Benchmark
  public void completable() throws Exception {
    final ExecutorService executor = Executors.newWorkStealingPool(THREAD_COUNT);

    final List<CompletableFuture<Integer>> futures = new ArrayList<>();

    for (int i = 0; i < SIZE; i++) {
      final int current = i;
      futures.add(CompletableFuture.supplyAsync(() -> current, executor));
    }

    int sum = 0;

    for (final CompletableFuture<Integer> f : futures) {
      sum += f.join();
    }

    if (sum != EXPECTED_SUM) {
      throw new IllegalStateException("did not properly collect all values");
    }

    executor.shutdown();
  }
}
