package com.github.otymko.dt.bsl.lsconnector.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;

import com.github.otymko.dt.bsl.lsconnector.BSLActivator;
import com.github.otymko.dt.bsl.lsconnector.core.BSLCore;
import com.github.otymko.dt.bsl.lsconnector.core.PluginSetting;
import com.github.otymko.dt.bsl.lsconnector.lsp.BSLConnector;
import com.github.otymko.dt.bsl.lsconnector.lsp.BSLLanguageClient;
import com.github.otymko.dt.bsl.lsconnector.util.BSLConfigurationFinder;

import lombok.Getter;

public class LanguageService {
    /**
     * Глобальные настройки плагина
     */
    private final PluginSetting setting;

    /**
     * Путь к базовому каталогу проекта
     */
    private final Path rootPath;

    private Process process;
    @Getter
    private BSLConnector connector;

    public LanguageService(PluginSetting setting, Path rootPath) {
	this.setting = setting;
	this.rootPath = rootPath;
    }

    public void start() {
	createProcess();
	connectToProcess();
    }

    public void stop() {
	if (connector != null) {
	    connector.shutdown();
	    connector.exit();
	}
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
	var pathToWorkspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().toPath();
	var externalJar = setting.isExternalJar();
	var isImageApp = !externalJar;
	var pathToLSP = getPathToBSLLS();

	if (!pathToLSP.toFile().exists()) {
	    return;
	}

	List<String> arguments = new ArrayList<>();
	if (!isImageApp) {
	    arguments.add(setting.getPathToJava());
	    arguments.add("-jar");
	}
	arguments.add("\"" + pathToLSP.toString() + "\"");

	if (isImageApp) {
	    arguments.add("lsp");
	}

	var optionalConfiguration = BSLConfigurationFinder.getPathToBSLConfiguration(pathToWorkspace, rootPath);
	if (optionalConfiguration.isPresent()) {
	    arguments.add("--configuration");
	    arguments.add(optionalConfiguration.get().toString());
	}

	BSLActivator.createWarningStatus(arguments.toString());

	try {
	    process = new ProcessBuilder()
		    .command(arguments)
		    .directory(pathToWorkspace.toFile())
		    .start();
	    BSLCore.getInstance().sleepCurrentThread(500);
	    if (!process.isAlive()) {
		BSLActivator.createWarningStatus("Не удалалось запустить процесс с BSL LS. Процесс был аварийно завершен.");
	    }
	} catch (IOException e) {
	    BSLActivator.createWarningStatus("Не удалось запустить процесс BSL LS", e);
	}
    }

    private void connectToProcess() {
	if (process == null) {
	    return;
	}
	var client = new BSLLanguageClient();
	connector = new BSLConnector(client, process.getInputStream(), process.getOutputStream(), rootPath);
	connector.startInThread();
	BSLCore.getInstance().sleepCurrentThread(2000); // FIXME: Сколько нужно ждать?
	connector.initialize();
    }

    private void clear() {
	process = null;
	connector = null;
    }

    private Path getPathToBSLLS() {
	return setting.getPathToLS();
    }
}
