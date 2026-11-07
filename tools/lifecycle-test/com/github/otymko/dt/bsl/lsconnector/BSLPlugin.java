package com.github.otymko.dt.bsl.lsconnector;

import com.github.otymko.dt.bsl.lsconnector.service.WindowsEventService;
import java.nio.file.Path;
import java.util.Optional;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class BSLPlugin {
  public WindowsEventService getWindowsEventService() {
    return new WindowsEventService();
  }

  public ScopedPreferenceStore getPreferenceStore() {
    return new ScopedPreferenceStore();
  }

  public Path getPathToWorkspace() {
    return Path.of(System.getProperty("java.io.tmpdir"));
  }

  public Optional<Path> getPathToConfiguration() {
    return Optional.empty();
  }

  public void sleepCurrentThread(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public static void createWarningStatus(String text) {}

  public static void createErrorStatus(String text, Throwable error) {}
}
