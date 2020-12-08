package com.github.otymko.dt.bsl.lsconnector;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

import com.github.otymko.dt.bsl.lsconnector.listener.WindowEventListener;
import com.github.otymko.dt.bsl.lsconnector.service.LSService;
import com.github.otymko.dt.bsl.lsconnector.service.WindowsEventService;
import com.github.otymko.dt.bsl.lsconnector.ui.BSLPreferencePage;
import com.github.otymko.dt.bsl.lsconnector.util.BSLCommon;

import lombok.Getter;

public class BSLPlugin extends Plugin {
    public static final String PLUGIN_ID = "com.github.otymko.dt.bsl.ls_connector";
    private static Map<String, IWorkbenchPart> workbenchParts = new ConcurrentHashMap<>();
    private static BSLPlugin plugin;
    private static BundleContext context;
    private WindowsEventService windowsEventService;
    private LSService lsService;
    private Path appDir;
    private Path pathToImageApp;
    private Path pathToWorkspace;
    private Optional<Path> pathToConfiguration;
    @Getter
    private ScopedPreferenceStore preferenceStore;

    public static BSLPlugin getPlugin() {
	return plugin;
    }

    public static IStatus createErrorStatus(String message, Throwable throwable) {
	return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, throwable);
    }

    public static IStatus createWarningStatus(final String message, Exception throwable) {
	return new Status(IStatus.WARNING, PLUGIN_ID, 0, message, throwable);
    }

    public static IStatus createWarningStatus(String message) {
	return new Status(IStatus.WARNING, PLUGIN_ID, 0, message, null);
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
	plugin = this;
	super.start(bundleContext);
	BSLPlugin.context = bundleContext;

	initialize();
	startServices();
	startLS();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
	stopLS();
	plugin = null;

	if (PlatformUI.isWorkbenchRunning()) {
	    for (var window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
		WindowEventListener.removeListenerFromAllPages(window);
	    }
	}

	super.stop(bundleContext);
    }

    public void sleepCurrentThread(long value) {
	try {
	    Thread.sleep(value);
	} catch (Exception e) {
	    createWarningStatus(e.getMessage());
	}
    }

    private void startLS() {
	BSLCommon.downloadBSLLS(pathToImageApp);
	lsService.start();
    }

    private void stopLS() {
	lsService.stop();
    }

    public void restartLS() {
	lsService.restart();
    }

    public boolean isRunningLS() {
	return lsService.isLaunched();
    }

    private void initialize() {
	initAppDir();
	initPreferenceStore();
	prepareForStart();
    }

    private void startServices() {
	windowsEventService = new WindowsEventService();
	lsService = new LSService(this);
    }

    private void initPreferenceStore() {
	preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, PLUGIN_ID);
	preferenceStore.setDefault(BSLPreferencePage.PATH_TO_BSLLS, plugin.getPathToImageApp().toString());
	preferenceStore.setDefault(BSLPreferencePage.EXTERNAL_JAR, false);
	preferenceStore.setDefault(BSLPreferencePage.PATH_TO_JAVA, "java");
	preferenceStore.setDefault(BSLPreferencePage.JAVA_OPTS, "");
    }

    private void prepareForStart() {
	pathToWorkspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().toPath();

	try {
	    searchConfigurationFile();
	} catch (IOException e) {
	    createErrorStatus(e.getMessage(), e);
	}
    }

    private void searchConfigurationFile() throws IOException {
	pathToConfiguration = BSLCommon.getConfigurationFileFromWorkspace(pathToWorkspace);
    }

    private void initAppDir() {
	appDir = Path.of(System.getProperty("user.home"), ".bsl-connector-for-edt");
	if (!appDir.toFile().exists()) {
	    appDir.toFile().mkdir();
	}

	// проверим есть ли image app BSL LS
	var pathToApp = Path.of(appDir.toString(), "bsl-language-server").toFile();
	if (!pathToApp.exists()) {
	    pathToApp.mkdir();
	}

	// путь к image app
	pathToImageApp = Path.of(pathToApp.toString(), "bsl-language-server.exe");
    }

    public Path getAppDir() {
	return appDir;
    }

    public Path getPathToImageApp() {
	return pathToImageApp;
    }

    public Map<String, IWorkbenchPart> getWorkbenchParts() {
	return workbenchParts;
    }

    public Path getPathToWorkspace() {
	return pathToWorkspace;
    }

    public WindowsEventService getWindowsEventService() {
	return windowsEventService;
    }

    public Optional<Path> getPathToConfiguration() {
	return pathToConfiguration;
    }
    
    public LSService getLSService() {
	return lsService;
    }

    protected BundleContext getContext() {
	return context;
    }

}
