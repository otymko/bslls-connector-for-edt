import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.lsp4j.jsonrpc.Launcher;

import com.github.otymko.dt.bsl.lsconnector.BSLPlugin;
import com.github.otymko.dt.bsl.lsconnector.lsp.BSLConnector;
import com.github.otymko.dt.bsl.lsconnector.lsp.BSLLanguageClient;

/** Failed and cancelled RPC listeners must finish instead of spinning or leaking threads. */
public class ConnectorListenerTest {
    public static void main(String[] args) throws Exception {
        BSLPlugin.createWarningStatus("Initialize the logging fixture before timing listeners");
        failedFutureTerminates();
        cancelledFutureTerminates();
        interruptionTerminates();
    }

    private static void failedFutureTerminates() throws Exception {
        AtomicBoolean cancel = new AtomicBoolean();
        Future<Void> failure = new Future<>() {
            @Override public boolean cancel(boolean interrupt) { return cancel.compareAndSet(false, true); }
            @Override public boolean isCancelled() { return cancel.get(); }
            @Override public boolean isDone() { return true; }
            @Override public Void get() throws ExecutionException {
                if (cancel.get()) {
                    throw new java.util.concurrent.CancellationException();
                }
                throw new ExecutionException(new IllegalStateException("listener failed"));
            }
            @Override public Void get(long timeout, TimeUnit unit) throws ExecutionException { return get(); }
        };
        int errorsBefore = BSLPlugin.ERRORS.get();
        runListener(failure, false);
        if (BSLPlugin.ERRORS.get() != errorsBefore + 1) {
            throw new AssertionError("Listener failure must be logged once");
        }
        System.out.println("PASS listener failure terminates and is logged once");
    }

    private static void cancelledFutureTerminates() throws Exception {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        cancelled.cancel(true);
        runListener(cancelled, false);
        System.out.println("PASS cancelled listener terminates normally");
    }

    private static void interruptionTerminates() throws Exception {
        runListener(new CompletableFuture<>(), true);
        System.out.println("PASS interrupted listener terminates with interruption preserved");
    }

    private static void runListener(Future<Void> future, boolean interrupt) throws Exception {
        BSLConnector connector = new BSLConnector(new BSLLanguageClient(),
                new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream());
        CountDownLatch listening = new CountDownLatch(1);
        Launcher<?> launcher = (Launcher<?>) Proxy.newProxyInstance(Launcher.class.getClassLoader(),
                new Class<?>[] {Launcher.class}, (proxy, method, arguments) -> {
                    if (method.getName().equals("startListening")) {
                        listening.countDown();
                        return future;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        Field launcherField = BSLConnector.class.getDeclaredField("launcher");
        launcherField.setAccessible(true);
        launcherField.set(connector, launcher);
        Method start = BSLConnector.class.getDeclaredMethod("start");
        start.setAccessible(true);
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicBoolean interruptedOnExit = new AtomicBoolean();
        Thread thread = new Thread(() -> {
            try {
                start.invoke(connector);
            } catch (InvocationTargetException cause) {
                error.set(cause.getCause());
            } catch (ReflectiveOperationException cause) {
                error.set(cause);
            } finally {
                interruptedOnExit.set(Thread.currentThread().isInterrupted());
            }
        });
        thread.setDaemon(true);
        try {
            thread.start();
            if (!listening.await(5, TimeUnit.SECONDS)) {
                throw new AssertionError("Listener did not enter startListening");
            }
            if (interrupt) {
                thread.interrupt();
            }
            thread.join(5000);
            if (thread.isAlive() || error.get() != null) {
                throw new AssertionError("Listener did not terminate normally: "
                        + java.util.Arrays.toString(thread.getStackTrace()), error.get());
            }
            if (interrupt && !interruptedOnExit.get()) {
                throw new AssertionError("Listener lost its interruption flag");
            }
        } finally {
            future.cancel(true);
            thread.interrupt();
            thread.join(2000);
        }
    }
}
