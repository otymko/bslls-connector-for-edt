package com.github.otymko.dt.bsl.lsconnector;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

import com.github.otymko.dt.bsl.lsconnector.lsp.BSLConnector;
import com.github.otymko.dt.bsl.lsconnector.lsp.BSLLanguageClient;
import com.github.otymko.dt.bsl.lsconnector.ui.BSLPreferencePage;
import com.github.otymko.dt.bsl.lsconnector.util.BSLCommon;

public class BSLPlugin extends Plugin {
    public static final String PLUGIN_ID = "com.github.otymko.dt.bsl.ls_connector";
    private static BSLPlugin plugin;
    private static BundleContext context;
    private Process processLSP;
    private BSLConnector bslConnector;
    private Path appDir;
    private Path pathToImageApp;
    private Optional<Path> pathToConfiguration;
    private ScopedPreferenceStore preferenceStore;

    public static BSLPlugin getPlugin() {
	return plugin;
    }

    public void start(BundleContext bundleContext) throws Exception {
	plugin = this;
	super.start(bundleContext);
	BSLPlugin.context = bundleContext;
	atStart();
    }

    public void stop(BundleContext bundleContext) throws Exception {
	stopLS();
	plugin = null;
	super.stop(bundleContext);
    }
    
    protected BundleContext getContext() {
	return context;
    }

    public void sleepCurrentThread(long value) {
	try {
	    Thread.sleep(value);
	} catch (Exception e) {
	    BSLPlugin.createWarningStatus(e.getMessage());
	}
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

    public void restartLS() {
	stopLS();
	runBSLLS();
	bindingLSP();
    }

    public boolean isRunningLS() {
	return bslConnector != null;
    }
    
    public BSLConnector getBSLConnector() {
	return bslConnector;
    }
    
    public Path getAppDir() {
	return appDir;
    }
    
    public Path getPathToImageApp() {
	return pathToImageApp;
    }
    
    public ScopedPreferenceStore getPreferenceStore() {
	return preferenceStore;
    }
    
    private void atStart() {
	initAppDir();
	initPreferenceStore();	

	prepareForStart();
	
	runBSLLS();
	bindingLSP();
    }

    private void initPreferenceStore() {
	preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, PLUGIN_ID);
	preferenceStore.setDefault(BSLPreferencePage.PATH_TO_BSLLS, plugin.getPathToImageApp().toString());
	preferenceStore.setDefault(BSLPreferencePage.EXTERNAL_JAR, false);
	preferenceStore.setDefault(BSLPreferencePage.PATH_TO_JAVA, "java");
    }

    private void prepareForStart() {
	try {
	    searchConfigurationFile();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    private void runBSLLS() {
	processLSP = null;
	var isImageApp = false;

	var externalJar = preferenceStore.getBoolean(BSLPreferencePage.EXTERNAL_JAR);
	Path pathToLSP;

	if (externalJar) {
	    pathToLSP = getPathToBSLLS();
	} else {
	    pathToLSP = pathToImageApp;
	    isImageApp = true;
	}

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

	try {
	    processLSP = new ProcessBuilder().command(arguments).start();
	    sleepCurrentThread(500);
	    if (!processLSP.isAlive()) {
		BSLPlugin
			.createWarningStatus("Не удалалось запустить процесс с BSL LS. Процесс был аварийно завершен.");
	    }
	} catch (IOException e) {
	    BSLPlugin.createErrorStatus("Не удалось запустить процесс BSL LS", e);
	}
    }

    private void bindingLSP() {
	if (processLSP == null) {
	    return;
	}
	var bslClient = new BSLLanguageClient();
	bslConnector = new BSLConnector(bslClient, processLSP.getInputStream(), processLSP.getOutputStream());
	bslConnector.startInThread();
	sleepCurrentThread(2000); // FIXME: Сколько нужно ждать?
	bslConnector.initialize();
	bslConnector.textDocumentDidOpen(BSLCommon.FAKE_URI, "");
    }

    private void stopLS() {
	if (bslConnector != null) {
	    bslConnector.shutdown();
	    bslConnector.exit();
	}
    }

    private Path getPathToBSLLS() {
	return Path.of(Optional.of(preferenceStore.getString(BSLPreferencePage.PATH_TO_BSLLS)).orElse(""));
    }

    private void searchConfigurationFile() throws IOException {
	var pathToWorkspace = ResourcesPlugin.getWorkspace().getRoot().getLocation();
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
}
