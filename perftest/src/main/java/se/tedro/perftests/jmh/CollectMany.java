package se.tedro.perftests.jmh;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.openjdk.jmh.annotations.Benchmark;
import se.tedro.concurrent.Async;
import se.tedro.concurrent.CoreAsync;
import se.tedro.concurrent.Stage;

public class CollectMany {
  private static final int SIZE = 10000;

  private static Async async = CoreAsync.builder().build();

  @Benchmark
  public void async() throws Exception {
    final List<Stage<Boolean>> futures = new ArrayList<>();

    for (int i = 0; i < SIZE; i++) {
      futures.add(async.completed(true));
    }

    async.collect(futures).join();
  }

  @Benchmark
  public void guava() throws Exception {
    final List<ListenableFuture<Boolean>> futures = new ArrayList<>();

    for (int i = 0; i < SIZE; i++) {
      futures.add(com.google.common.util.concurrent.Futures.immediateFuture(true));
    }

    com.google.common.util.concurrent.Futures.allAsList(futures).get();
  }

  @Benchmark
  public void completable() throws Exception {
    final List<CompletableFuture<Boolean>> futures = new ArrayList<>();

    for (int i = 0; i < SIZE; i++) {
      futures.add(CompletableFuture.completedFuture(true));
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
  }
}
