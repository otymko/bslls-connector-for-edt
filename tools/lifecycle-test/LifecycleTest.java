import com.github.otymko.dt.bsl.lsconnector.BSLPlugin;
import com.github.otymko.dt.bsl.lsconnector.lsp.BSLConnector;
import com.github.otymko.dt.bsl.lsconnector.lsp.BSLLanguageClient;
import com.github.otymko.dt.bsl.lsconnector.service.LSService;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LifecycleTest {
  static Field process;
  static List<Process> children = new ArrayList<>();
  static int failed;

  interface Check {
    void run() throws Exception;
  }

  static void check(boolean ok, String message) {
    if (!ok) throw new AssertionError(message);
  }

  static Process child(LSService service) throws Exception {
    return (Process) process.get(service);
  }

  static LSService prepared(String mode) throws Exception {
    BSLConnector.mode = mode;
    BSLConnector.shutdownDone = false;
    BSLConnector.exitTooEarly = false;
    LSService service = new LSService(new BSLPlugin());
    Process child =
        new ProcessBuilder(System.getProperty("java"), "-Xmx32m", "-jar", System.getProperty("jar"))
            .start();
    children.add(child);
    process.set(service, child);
    Field connector = LSService.class.getDeclaredField("connector");
    connector.setAccessible(true);
    connector.set(
        service,
        new BSLConnector(new BSLLanguageClient(), child.getInputStream(), child.getOutputStream()));
    return service;
  }

  static void run(String name, Check test) {
    try {
      test.run();
      System.out.println("PASS " + name);
    } catch (Throwable e) {
      failed++;
      System.out.println("FAIL " + name + ": " + e);
    } finally {
      Thread.interrupted();
      for (Process childProcess : children) {
        childProcess.destroyForcibly();
        try {
          childProcess.waitFor(5, TimeUnit.SECONDS);
        } catch (Exception e) {
        }
      }
      children.clear();
    }
  }

  public static void main(String[] args) throws Exception {
    process = LSService.class.getDeclaredField("process");
    process.setAccessible(true);
    run(
        "stop-reaps-real-child",
        () -> {
          LSService service = prepared("normal");
          Process childProcess = child(service);
          service.stop();
          check(!childProcess.isAlive(), "orphan after stop");
          check(!service.isLaunched(), "still launched");
        });
    run(
        "shutdown-response-before-exit",
        () -> {
          LSService service = prepared("normal");
          service.stop();
          check(!BSLConnector.exitTooEarly, "exit before shutdown response");
        });
    run(
        "rpc-exception-still-reaps",
        () -> {
          LSService service = prepared("stop-error");
          Process childProcess = child(service);
          try {
            service.stop();
          } catch (RuntimeException e) {
          }
          check(!childProcess.isAlive(), "orphan after RPC error");
        });
    run(
        "hung-shutdown-is-bounded",
        () -> {
          LSService service = prepared("timeout");
          Process childProcess = child(service);
          long started = System.nanoTime();
          service.stop();
          check(!childProcess.isAlive(), "orphan after timeout");
          check(System.nanoTime() - started < TimeUnit.SECONDS.toNanos(10), "unbounded stop");
        });
    run(
        "repeat-start-does-not-replace-child",
        () -> {
          LSService service = prepared("normal");
          Process childProcess = child(service);
          service.start();
          if (child(service) != null) children.add(child(service));
          check(child(service) == childProcess, "start spawned a duplicate");
          service.stop();
        });
    run(
        "startup-linkage-error-reaps",
        () -> {
          BSLConnector.mode = "startup-error";
          LSService service = new LSService(new BSLPlugin());
          try {
            service.start();
          } catch (LinkageError e) {
          }
          Process childProcess = child(service);
          if (childProcess != null) children.add(childProcess);
          check(childProcess == null || !childProcess.isAlive(), "orphan after activation error");
        });
    run(
        "interrupted-stop-reaps-and-restores-flag",
        () -> {
          LSService service = prepared("normal");
          Process childProcess = child(service);
          Thread.currentThread().interrupt();
          service.stop();
          check(!childProcess.isAlive(), "orphan after interrupt");
          check(Thread.currentThread().isInterrupted(), "interrupt flag lost");
        });
    run(
        "double-stop-is-safe",
        () -> {
          LSService service = prepared("normal");
          service.stop();
          service.stop();
        });
    if (failed > 0) throw new AssertionError(failed + " lifecycle checks failed");
  }
}
