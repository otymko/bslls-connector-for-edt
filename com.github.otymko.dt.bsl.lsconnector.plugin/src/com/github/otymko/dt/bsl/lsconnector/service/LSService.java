package com.github.otymko.dt.bsl.lsconnector.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.github.otymko.dt.bsl.lsconnector.BSLPlugin;
import com.github.otymko.dt.bsl.lsconnector.lsp.BSLConnector;
import com.github.otymko.dt.bsl.lsconnector.lsp.BSLLanguageClient;
import com.github.otymko.dt.bsl.lsconnector.ui.BSLPreferencePage;

import lombok.Getter;

public class LSService {
    private final BSLPlugin plugin;
    private final WindowsEventService windowsEventService;
    private final ScopedPreferenceStore preferenceStore;
    private Process process;
    @Getter
    private BSLConnector connector;
    
    public LSService(BSLPlugin plugin) {
	this.plugin = plugin;
	windowsEventService = plugin.getWindowsEventService();
	preferenceStore = plugin.getPreferenceStore();
    }
    
    public void start() {
	createProcess();
	connectToProcess();
	if (isLaunched()) {
	    windowsEventService.start();
	}
    }
    
    public void stop() {
	if (connector != null) {
	    connector.shutdown();
	    connector.exit();
	}
	windowsEventService.stop();
	clear();
    }
    
    public void restart() {
	stop();
	start();
    }
    
    public boolean isLaunched() {
	return process != null && process.isAlive();
    }

    private void createProcess() {
	var pathToConfiguration = plugin.getPathToConfiguration();
	var pathToWorkspace = plugin.getPathToWorkspace();
	var externalJar = preferenceStore.getBoolean(BSLPreferencePage.EXTERNAL_JAR);
	var isImageApp = !externalJar;
	var pathToLSP = getPathToBSLLS();
	var fileLSP = pathToLSP.toFile();

	if (!fileLSP.exists()) {
	    return;
	}
	
	if (isImageApp && !fileLSP.canExecute()) {
	    try {
		fileLSP.setExecutable(true);
	    } catch (SecurityException e) {
		BSLPlugin.createErrorStatus("Не удалось установить право исполнения для файла процесса BSL LS", e);
	    }
	}

	List<String> arguments = new ArrayList<>();
	if (!isImageApp) {
	    arguments.add(preferenceStore.getString(BSLPreferencePage.PATH_TO_JAVA));
	    arguments.add("-jar");
	}
	
	if (SystemUtils.IS_OS_UNIX) {
	    arguments.add(pathToLSP.toString());
	} else {
	    arguments.add("\"" + pathToLSP.toString() + "\"");
	}

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
	plugin.sleepCurrentThread(2000); // FIXME: Сколько нужно ждать?
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
