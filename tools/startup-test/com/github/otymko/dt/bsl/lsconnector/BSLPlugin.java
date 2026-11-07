package com.github.otymko.dt.bsl.lsconnector;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/** Standalone fixture for the workspace and logging services used by the connector. */
public class BSLPlugin {
    private static final BSLPlugin INSTANCE = new BSLPlugin();
    public static final AtomicInteger ERRORS = new AtomicInteger();

    public static BSLPlugin getPlugin() { return INSTANCE; }
    public Path getPathToWorkspace() { return Path.of(".").toAbsolutePath(); }
    public static IStatus createWarningStatus(String message) { return Status.OK_STATUS; }
    public static IStatus createErrorStatus(String message, Throwable cause) {
        ERRORS.incrementAndGet();
        return Status.OK_STATUS;
    }
}
