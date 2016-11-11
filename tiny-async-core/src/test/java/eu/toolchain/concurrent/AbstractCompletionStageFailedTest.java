package eu.toolchain.concurrent;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractCompletionStageFailedTest {
  private AbstractImmediateCompletionStage<To> base;

  @Mock
  private FutureFramework async;
  @Mock
  private Function<Throwable, To> transform;
  @Mock
  private Function<Throwable, CompletionStage<To>> lazyTransform;
  @Mock
  private To to;
  @Mock
  private CompletionStage<To> resolved;
  @Mock
  private CompletionStage<To> failed;

  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
    base = mock(AbstractImmediateCompletionStage.class, Mockito.CALLS_REAL_METHODS);
    base.async = async;
  }

  @Test
  public void testTransformResolved() throws Exception {
    final Throwable cause = new Exception();

    doReturn(to).when(transform).apply(cause);
    doReturn(resolved).when(async).completed(to);
    doReturn(failed).when(async).failed(any(Exception.class));

    assertEquals(resolved, base.transformFailed(transform, cause));

    final InOrder order = inOrder(transform, async);
    order.verify(transform).apply(cause);
    order.verify(async).completed(to);
    order.verify(async, never()).failed(any(Exception.class));
  }

  @Test
  public void testTransformResolvedThrows() throws Exception {
    final RuntimeException thrown = new RuntimeException();
    final RuntimeException cause = new RuntimeException();

    doThrow(thrown).when(transform).apply(cause);
    doReturn(resolved).when(async).completed(to);
    doReturn(failed).when(async).failed(any(Exception.class));

    assertEquals(failed, base.transformFailed(transform, cause));

    final InOrder order = inOrder(transform, async);
    order.verify(transform).apply(cause);
    order.verify(async, never()).completed(to);
    order.verify(async).failed(any(Exception.class));
  }

  @Test
  public void testTransformLazyResolved() throws Exception {
    final RuntimeException cause = new RuntimeException();

    doReturn(resolved).when(lazyTransform).apply(cause);
    doReturn(failed).when(async).failed(any(Exception.class));

    assertEquals(resolved, base.lazyTransformFailed(lazyTransform, cause));

    final InOrder order = inOrder(lazyTransform, async);
    order.verify(lazyTransform).apply(cause);
    order.verify(async, never()).failed(any(Exception.class));
  }

  @Test
  public void testTransformLazyResolvedThrows() throws Exception {
    final RuntimeException thrown = new RuntimeException();
    final RuntimeException cause = new RuntimeException();

    doThrow(thrown).when(lazyTransform).apply(cause);
    doReturn(failed).when(async).failed(any(Exception.class));

    assertEquals(failed, base.lazyTransformFailed(lazyTransform, cause));

    final InOrder order = inOrder(lazyTransform, async);
    order.verify(lazyTransform).apply(cause);
    order.verify(async).failed(any(Exception.class));
  }

  public interface To {
  }
}
