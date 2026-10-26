import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.github.otymko.dt.bsl.lsconnector.lsp.BSLConnector;
import com.github.otymko.dt.bsl.lsconnector.lsp.BSLLanguageClient;

/** Delaying the listening thread must not delay publication of the RPC proxy. */
public class ConnectorStartupTest {
    public static void main(String[] args) throws Exception {
        var output = new ByteArrayOutputStream();
        var connector = new BSLConnector(new BSLLanguageClient(), new ByteArrayInputStream(new byte[0]), output);
        var allowListener = new CountDownLatch(1);
        var listenerError = new AtomicReference<Throwable>();
        Method listen = BSLConnector.class.getDeclaredMethod("start");
        listen.setAccessible(true);
        Thread delayed = new Thread(() -> {
            try {
                if (!allowListener.await(10, TimeUnit.SECONDS)) {
                    throw new AssertionError("Test did not release the listening thread");
                }
                listen.invoke(connector);
            } catch (Throwable error) {
                listenerError.set(error);
            }
        });
        Field thread = BSLConnector.class.getDeclaredField("thread");
        thread.setAccessible(true);
        thread.set(connector, delayed);
        try {
            connector.startInThread();
            var response = connector.initialize();
            String request = output.toString(StandardCharsets.UTF_8);
            if (response == null || !request.contains("\"method\":\"initialize\"")) {
                throw new AssertionError("Initialization was not sent while the listening thread was delayed");
            }
            System.out.println("PASS: initialization does not depend on listener scheduling or a fixed sleep");
        } finally {
            allowListener.countDown();
            delayed.join(5000);
        }
        if (delayed.isAlive() || listenerError.get() != null) {
            throw new AssertionError("Listener did not finish normally", listenerError.get());
        }
    }
}
