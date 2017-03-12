package se.tedro.concurrent;

public class ConcurrentCompletableEffectTest extends AbstractCompletableEffectTest {
  @Override
  protected <T> Completable<T> newCompletable(final Caller caller) {
    return new ConcurrentCompletable<>(caller);
  }
}
