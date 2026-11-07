package com.github.otymko.dt.bsl.lsconnector.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.github.otymko.dt.bsl.lsconnector.BSLPlugin;
import com.github.otymko.dt.bsl.lsconnector.lsp.BSLConnector;
import com.github.otymko.dt.bsl.lsconnector.lsp.BSLLanguageClient;
import com.github.otymko.dt.bsl.lsconnector.ui.BSLPreferencePage;

public class LSService {
    private final BSLPlugin plugin;
    private final WindowsEventService windowsEventService;
    private final ScopedPreferenceStore preferenceStore;
    private Process process;
    private BSLConnector connector;
    private Thread shutdownHook;
    
    public LSService(BSLPlugin plugin) {
	this.plugin = plugin;
	windowsEventService = plugin.getWindowsEventService();
	preferenceStore = plugin.getPreferenceStore();
    }
    
    public synchronized void start() {
        if (isLaunched()) {
            return;
        }
        stop();
        try {
            createProcess();
            if (!isLaunched()) {
                stop();
                return;
            }
            Process owned = process;
            shutdownHook = new Thread(() -> terminateProcess(owned), "BSLLanguageServerCleanup");
            Runtime.getRuntime().addShutdownHook(shutdownHook);
            connectToProcess();
            windowsEventService.start();
        } catch (RuntimeException | LinkageError error) {
            stop();
            throw error;
        }
    }
    
    public synchronized void stop() {
        boolean interrupted = Thread.interrupted();
        try {
            if (connector != null && isLaunched()) {
                try {
                    connector.shutdown().get(2, TimeUnit.SECONDS);
                } catch (InterruptedException error) {
                    interrupted = true;
                } catch (Exception | LinkageError error) {
                    BSLPlugin.createErrorStatus("BSL LS shutdown did not complete", error);
                }
                try {
                    connector.exit();
                } catch (RuntimeException | LinkageError error) {
                    BSLPlugin.createErrorStatus("BSL LS exit failed", error);
                }
            }
        } finally {
            try {
                windowsEventService.stop();
            } finally {
                terminateProcess(process);
                if (!isLaunched()) {
                    removeShutdownHook();
                    clear();
                }
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    public synchronized void restart() {
	stop();
	start();
    }
    
    public synchronized boolean isLaunched() {
	return process != null && process.isAlive();
    }

    public synchronized BSLConnector getConnector() {
        return connector;
    }

    private void removeShutdownHook() {
        if (shutdownHook != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            } catch (IllegalStateException ignored) {
                // JVM shutdown is already running the captured-process cleanup.
            }
            shutdownHook = null;
        }
    }

    private static void terminateProcess(Process owned) {
        if (owned == null) {
            return;
        }
        boolean interrupted = Thread.interrupted();
        try {
            try {
                if (owned.isAlive() && !owned.waitFor(200, TimeUnit.MILLISECONDS)) {
                    owned.destroy();
                    if (!owned.waitFor(1, TimeUnit.SECONDS)) {
                        owned.destroyForcibly();
                        owned.waitFor(2, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException error) {
                interrupted = true;
                owned.destroyForcibly();
                try {
                    owned.waitFor(2, TimeUnit.SECONDS);
                } catch (InterruptedException secondInterrupt) {
                    interrupted = true;
                }
            }
        } finally {
            try { owned.getOutputStream().close(); } catch (IOException ignored) { }
            try { owned.getInputStream().close(); } catch (IOException ignored) { }
            try { owned.getErrorStream().close(); } catch (IOException ignored) { }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void createProcess() {
	var pathToConfiguration = plugin.getPathToConfiguration();
	var pathToWorkspace = plugin.getPathToWorkspace();
	var externalJar = preferenceStore.getBoolean(BSLPreferencePage.EXTERNAL_JAR);
	var isImageApp = !externalJar;
	var pathToLSP = getPathToBSLLS();

	if (!pathToLSP.toFile().exists()) {
	    return;
	}

	List<String> arguments = new ArrayList<>();
	if (!isImageApp) {
	    arguments.add(preferenceStore.getString(BSLPreferencePage.PATH_TO_JAVA));
	    arguments.add("-jar");
	}
	arguments.add(pathToLSP.toString());

	if (pathToConfiguration.isPresent()) {
	    arguments.add("--configuration");
	    arguments.add(pathToConfiguration.get().toString());
	}

	BSLPlugin.createWarningStatus(arguments.toString());

	try {
	    process = new ProcessBuilder()
		    .command(arguments)
		    .directory(pathToWorkspace.toFile())
		    .start();
	    plugin.sleepCurrentThread(500);
	    if (!process.isAlive()) {
		BSLPlugin.createWarningStatus("Не удалалось запустить процесс с BSL LS. Процесс был аварийно завершен.");
	    }
	} catch (IOException e) {
	    BSLPlugin.createErrorStatus("Не удалось запустить процесс BSL LS", e);
	}
    }
    
    private void connectToProcess() {
	if (process == null) {
	    return;
	}
	var client = new BSLLanguageClient();
	connector = new BSLConnector(client, process.getInputStream(), process.getOutputStream());
	connector.startInThread();
	connector.initialize();
    }
    
    private void clear() {
	process = null; 
	connector = null;
    }
    
    private Path getPathToBSLLS() {
	return Path.of(Optional.of(preferenceStore.getString(BSLPreferencePage.PATH_TO_BSLLS)).orElse(""));
    }
}
