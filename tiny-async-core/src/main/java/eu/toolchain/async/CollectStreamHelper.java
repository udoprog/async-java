package eu.toolchain.async;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of {@link AsyncFuture#end(List, AsyncFuture.StreamCollector)}.
 *
 * @author udoprog
 *
 * @param <T>
 */
public class CollectStreamHelper<S, T> implements FutureDone<S> {
    private final AsyncCaller caller;
    private final StreamCollector<S, T> collector;
    private final ResolvableFuture<? super T> target;
    private final AtomicInteger countdown;

    private final AtomicInteger successful = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();
    private final AtomicInteger cancelled = new AtomicInteger();

    public CollectStreamHelper(final AsyncCaller caller, final int size, final StreamCollector<S, T> collector,
            final ResolvableFuture<? super T> target) {
        if (size <= 0)
            throw new IllegalArgumentException("size");

        this.caller = caller;
        this.collector = collector;
        this.target = target;
        this.countdown = new AtomicInteger(size);
    }

    @Override
    public void failed(Throwable e) throws Exception {
        failed.incrementAndGet();
        caller.failStreamCollector(collector, e);
        check();
    }

    @Override
    public void resolved(S result) throws Exception {
        successful.incrementAndGet();
        caller.resolveStreamCollector(collector, result);
        check();
    }

    @Override
    public void cancelled() throws Exception {
        cancelled.incrementAndGet();
        caller.cancelStreamCollector(collector);
        check();
    }

    private void check() throws Exception {
        if (countdown.decrementAndGet() == 0)
            done();
    }

    private void done() {
        final T result;

        try {
            result = collector.end(successful.get(), failed.get(), cancelled.get());
        } catch (Exception e) {
            target.fail(e);
            return;
        }

        target.resolve(result);
    }
}