import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.lsp4j.DocumentDiagnosticReport;
import org.eclipse.lsp4j.RelatedFullDocumentDiagnosticReport;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.github.otymko.dt.bsl.lsconnector.lsp.BSLConnector;
import com.github.otymko.dt.bsl.lsconnector.lsp.BSLLanguageClient;

/** A cold diagnostic response may take longer than ten seconds. */
public class ConnectorDiagnosticTimeoutTest {
    public static void main(String[] args) throws Exception {
        try {
            verifyColdResponse();
            System.exit(0);
        } catch (Throwable error) {
            error.printStackTrace();
            System.exit(1);
        }
    }

    private static void verifyColdResponse() throws Exception {
        var connector = new BSLConnector(new BSLLanguageClient(),
                new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream());
        var reply = new CompletableFuture<DocumentDiagnosticReport>();
        var document = (TextDocumentService) Proxy.newProxyInstance(
                TextDocumentService.class.getClassLoader(), new Class<?>[] {TextDocumentService.class},
                (proxy, method, params) -> {
                    if (method.getName().equals("diagnostic")) {
                        var report = new RelatedFullDocumentDiagnosticReport(List.of());
                        CompletableFuture.delayedExecutor(11, TimeUnit.SECONDS)
                                .execute(() -> reply.complete(new DocumentDiagnosticReport(report)));
                        return reply;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        var server = (LanguageServer) Proxy.newProxyInstance(
                LanguageServer.class.getClassLoader(), new Class<?>[] {LanguageServer.class},
                (proxy, method, params) -> document);
        Field field = BSLConnector.class.getDeclaredField("server");
        field.setAccessible(true);
        field.set(connector, server);
        var received = new AtomicReference<List<?>>();
        connector.runFutureTask(() -> received.set(connector.diagnostics("file:///cold-module.bsl")));
        if (received.get() == null || !reply.isDone()) {
            throw new AssertionError("Cold diagnostics were cancelled before the server response");
        }
        System.out.println("PASS cold diagnostics survive an eleven-second response through the outer task");
    }
}
