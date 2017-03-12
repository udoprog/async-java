package se.tedro.concurrent;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractCompletableEffectTest {
  private Completable<Result> completable;

  private List<Throwable> whenFailed;
  private List<Result> whenComplete;
  private int whenCancelled;

  private boolean completed;
  private boolean cancelled;
  private boolean failed;

  @Before
  public final void abstractSetup() {
    whenFailed = new ArrayList<>();
    whenComplete = new ArrayList<>();
    whenCancelled = 0;

    completed = false;
    cancelled = false;
    failed = false;
  }

  @Test
  public void testComplete() {
    final Result result = mock(Result.class);

    completed = true;
    whenComplete.add(result);

    assertEffect(() -> completable.complete(result));
  }

  @Test
  public void testFailed() {
    final Exception e = new Exception();
    failed = true;
    whenFailed.add(e);

    assertEffect(() -> completable.fail(e));
  }

  @Test
  public void testCancelled() {
    cancelled = true;
    whenCancelled = 1;

    assertEffect(() -> completable.cancel());
  }

  protected abstract <T> Completable<T> newCompletable(Caller caller);

  protected void assertEffect(final Runnable effect) {
    final ConcurrentLinkedQueue<Throwable> callerErrors = new ConcurrentLinkedQueue<>();
    final AtomicInteger executedCount = new AtomicInteger();

    final Caller caller = new DirectCaller() {
      @Override
      public void execute(final Runnable runnable) {
        super.execute(runnable);
        executedCount.incrementAndGet();
      }

      @Override
      protected void internalError(final String what, final Throwable e) {
        callerErrors.add(e);
      }
    };

    completable = newCompletable(caller);

    assertFalse(completable.isDone());
    assertFalse(completable.isCompleted());
    assertFalse(completable.isCancelled());
    assertFalse(completable.isFailed());

    final RuntimeException doneError = new RuntimeException("done");
    final AtomicInteger doneCount = new AtomicInteger();
    completable.whenDone(doneCount::incrementAndGet);
    completable.whenDone(() -> doThrow(doneError));

    final RuntimeException failedError = new RuntimeException("failed");
    final ConcurrentLinkedQueue<Throwable> whenFailed = new ConcurrentLinkedQueue<>();
    completable.whenFailed(whenFailed::add);
    completable.whenFailed(e -> doThrow(failedError));

    final RuntimeException completedError = new RuntimeException("completed");
    final ConcurrentLinkedQueue<Result> whenComplete = new ConcurrentLinkedQueue<>();
    completable.whenComplete(whenComplete::add);
    completable.whenComplete(e -> doThrow(completedError));

    final AtomicInteger whenCancelled = new AtomicInteger();
    final RuntimeException cancelledError = new RuntimeException("cancelled");
    completable.whenCancelled(whenCancelled::incrementAndGet);
    completable.whenCancelled(() -> doThrow(cancelledError));

    assertEquals(0, doneCount.get());

    effect.run();

    assertEquals(1, doneCount.get());

    assertTrue(completable.isDone());
    assertEquals(this.completed, completable.isCompleted());
    assertEquals(this.cancelled, completable.isCancelled());
    assertEquals(this.failed, completable.isFailed());

    completable.whenDone(doneCount::incrementAndGet);

    final List<Throwable> expectedCallerErrors = new ArrayList<>();

    if (cancelled) {
      expectedCallerErrors.add(cancelledError);
    }

    if (completed) {
      expectedCallerErrors.add(completedError);
    }

    if (failed) {
      expectedCallerErrors.add(failedError);
    }

    expectedCallerErrors.add(doneError);

    assertEquals(2, doneCount.get());
    assertEquals(9, executedCount.get());
    assertEquals(expectedCallerErrors, ImmutableList.copyOf(callerErrors));
    assertEquals(this.whenFailed, ImmutableList.copyOf(whenFailed));
    assertEquals(this.whenComplete, ImmutableList.copyOf(whenComplete));
    assertEquals(this.whenCancelled, whenCancelled.get());
  }

  private void doThrow(final RuntimeException e) {
    throw e;
  }

  interface Result {
  }
}
