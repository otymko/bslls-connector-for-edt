package com.github.otymko.dt.bsl.lsconnector.lsp;

import java.io.*;
import java.util.concurrent.*;

public class BSLConnector {
  public static String mode = "normal";
  public static boolean shutdownDone, exitTooEarly;

  public BSLConnector(BSLLanguageClient client, InputStream in, OutputStream out) {}

  public void startInThread() {}

  public CompletableFuture<Object> initialize() {
    if (mode.equals("startup-error"))
      throw new NoClassDefFoundError("simulated optional LSP dependency");
    return CompletableFuture.completedFuture(null);
  }

  public CompletableFuture<Object> shutdown() {
    shutdownDone = false;
    if (mode.equals("stop-error")) throw new IllegalStateException("broken RPC");
    if (mode.equals("timeout")) return new CompletableFuture<>();
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            Thread.sleep(150);
          } catch (InterruptedException e) {
          }
          shutdownDone = true;
          return null;
        });
  }

  public void exit() {
    if (!shutdownDone) exitTooEarly = true;
  }
}
