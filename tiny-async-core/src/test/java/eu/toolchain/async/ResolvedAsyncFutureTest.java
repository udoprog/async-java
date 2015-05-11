package eu.toolchain.async;

public class ResolvedAsyncFutureTest extends AbstractImmediateAsyncFuture {
    @Override
    protected int setupResolved() {
        return 1;
    }

    @Override
    protected AsyncFuture<Object> setupFuture(AsyncFramework async, AsyncCaller caller, Object result, Throwable cause) {
        return new ResolvedAsyncFuture<Object>(async, caller, result);
    }
}