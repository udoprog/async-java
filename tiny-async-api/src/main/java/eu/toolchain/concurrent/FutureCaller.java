package eu.toolchain.concurrent;

import java.util.function.Consumer;

/**
 * User-defined functions to whenDone unexpected circumstances.
 *
 * <p>The implementation of these methods will be invoked from the calling thread that interacts with
 * the future.
 *
 * <p>None of the below methods throw checked exceptions, and they are intended to never throw
 * anything, with the exception of {@code Error}. This means that the implementor is required to
 * make sure this doesn't happen, the best way to accomplish this is to wrap each callback in a
 * try-catch statement like this:
 *
 * <pre>{@code
 * new FutureCaller() {
 *   public <T> void complete(CompletionHandle<T> whenDone, T result) {
 *     try {
 *       whenDone.completed(result);
 *     } catch(Exception e) {
 *       // log unexpected error
 *     }
 *   }
 *
 *   // .. other methods
 * }
 * }</pre>
 *
 * <p>The core of the framework provides some base classes for easily accomplishing this, most
 * notable is {@code DirectAsyncCaller}.
 *
 * @author udoprog
 */
public interface FutureCaller {
  /**
   * Indicate that a Managed reference has been leaked.
   *
   * @param reference the reference that was leaked
   * @param stack the stacktrace for where it was leaked, can be {@code null} if the information is
   * unavailable
   */
  void referenceLeaked(Object reference, StackTraceElement[] stack);

  /**
   * Execute the given action.
   *
   * @param runnable action to execute
   */
  void execute(Runnable runnable);
}