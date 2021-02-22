package com.github.otymko.dt.bsl.lsconnector.core;

import java.nio.file.Path;

import com.github.otymko.dt.bsl.lsconnector.service.LanguageServiceManager;

import lombok.Getter;

public class BSLCore {
    private static final BSLCore INSTANCE = new BSLCore();
    @Getter
    private final LanguageServiceManager serviceManager;
    @Getter
    private final Path appPath;
    @Getter
    private final Path pathToImageApp;

    private BSLCore() {
	this.serviceManager = new LanguageServiceManager();
	
	appPath = Path.of(System.getProperty("user.home"), ".bsl-connector-for-edt");
	if (!appPath.toFile().exists()) {
	    appPath.toFile().mkdir();
	}

	var pathToApp = Path.of(appPath.toString(), "bsl-language-server").toFile();
	if (!pathToApp.exists()) {
	    pathToApp.mkdir();
	}
	pathToImageApp = Path.of(pathToApp.toString(), "bsl-language-server.exe");
    }

    public static BSLCore getInstance() {
	return INSTANCE;
    }

    public PluginSetting getSetting() {
	var setting = new PluginSetting();
	setting.setEnable(true);
	setting.setPathToJava(Path.of(""));
	setting.setPathToLS(
		Path.of("C:\\Users\\otymko\\.bsl-connector-for-edt\\bsl-language-server\\bsl-language-server.exe"));
	setting.setJavaOpts("");
	return setting;
    }
    
    public void sleepCurrentThread(long value) {
	try {
	    Thread.sleep(value);
	} catch (Exception e) {
//	    createWarningStatus(e.getMessage());
	}
    }

}
