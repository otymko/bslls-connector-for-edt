package com.github.otymko.dt.bsl.lsconnector;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

import com.github.otymko.dt.bsl.lsconnector.lsp.BSLConnector;
import com.github.otymko.dt.bsl.lsconnector.lsp.BSLLanguageClient;
import com.github.otymko.dt.bsl.lsconnector.ui.BSLPreferencePage;

public class BSLPlugin extends Plugin {
    public static final String PLUGIN_ID = "com.github.otymko.dt.bsl.ls_connector";
    private static final URI FAKE_URI = Path.of("fake.bsl").toUri();
    private static BSLPlugin plugin;
    private static BundleContext context;
    
    private Optional<Path> pathToConfiguration;
    private Process processLSP;
    private BSLConnector bslConnector;

    private ScopedPreferenceStore preferenceStore;

    public static BSLPlugin getPlugin() {
	return plugin;
    }

    public void start(BundleContext bundleContext) throws Exception {
	plugin = this;
	super.start(bundleContext);
	BSLPlugin.context = bundleContext;
	
	preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, PLUGIN_ID);
	searchConfigurationFile();
	runBSLLS();
	bindingLSP();
    }

    public void stop(BundleContext bundleContext) throws Exception {
	plugin = null;
	super.stop(bundleContext);
	stopLS();
    }

    public BSLConnector getBSLConnector() {
	return bslConnector;
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

    public ScopedPreferenceStore getPreferenceStore() {
	return preferenceStore;
    }

    private void runBSLLS() {
	processLSP = null;

	var pathToLSP = getPathToBSLLS();
	if (!pathToLSP.toFile().exists()) {
	    showMessage("BSL LS не найден");
	}
	
	List<String> arguments = new ArrayList<>();
	arguments.add(Optional.of(preferenceStore.getString(BSLPreferencePage.PATH_TO_JAVA)).orElse("java"));
	arguments.add("-jar");
	arguments.add(pathToLSP.toString());
	if (pathToConfiguration.isPresent()) {
	    arguments.add("--configuration");
	    arguments.add(pathToConfiguration.get().toString());
	}

	try {
	    processLSP = new ProcessBuilder().command(arguments).start();
	    sleepCurrentThread(500);
	    if (!processLSP.isAlive()) {
		BSLPlugin.createWarningStatus("Не удалалось запустить процесс с BSL LS. Процесс был аварийно завершен.");
	    }
	} catch (IOException e) {
	    BSLPlugin.createErrorStatus("Не удалось запустить процесс BSL LS", e);
	}
    }
    
    private void showMessage(String message) {
        MessageDialog.openInformation(null, "BSL LS connector", message);
    }

    private void bindingLSP() {
	if (processLSP == null) {
	    return;
	}
	var bslClient = new BSLLanguageClient();
	bslConnector = new BSLConnector(bslClient, processLSP.getInputStream(), processLSP.getOutputStream());
	bslConnector.startInThread();
	sleepCurrentThread(2000); // FIXME: ? Сколько нужно ждать?
	bslConnector.initialize();
	bslConnector.textDocumentDidOpen(FAKE_URI, "");
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

    protected BundleContext getContext() {
	return context;
    }
}
