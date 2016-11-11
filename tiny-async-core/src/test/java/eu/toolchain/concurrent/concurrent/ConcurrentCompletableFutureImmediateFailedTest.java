package eu.toolchain.concurrent.concurrent;

import eu.toolchain.concurrent.AbstractImmediateCompletionStage;
import eu.toolchain.concurrent.FutureCaller;
import eu.toolchain.concurrent.FutureFramework;
import eu.toolchain.concurrent.ImmediateAsyncFutureTestBase;

public class ConcurrentCompletableFutureImmediateFailedTest extends ImmediateAsyncFutureTestBase {
  @Override
  protected AbstractImmediateCompletionStage<From> setupFuture(
      FutureFramework async, FutureCaller caller, From result, Throwable cause
  ) {
    final ConcurrentCompletableFuture<From> future =
        new ConcurrentCompletableFuture<>(async, caller);
    future.fail(cause);
    return future;
  }

  @Override
  protected ExpectedState setupState() {
    return ExpectedState.FAILED;
  }
}
