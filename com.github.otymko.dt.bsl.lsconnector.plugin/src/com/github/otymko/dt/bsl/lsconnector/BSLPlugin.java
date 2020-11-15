package com.github.otymko.dt.bsl.lsconnector;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

import com.github.otymko.dt.bsl.lsconnector.listener.WindowEventListener;
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
    private static final WindowEventListener WINDOWS_EVENT_LISTENER = new WindowEventListener();

    private static Map<String, IWorkbenchPart> workbenchParts = new ConcurrentHashMap<>();

    public static BSLPlugin getPlugin() {
	return plugin;
    }

    public void start(BundleContext bundleContext) throws Exception {
	plugin = this;
	super.start(bundleContext);
	BSLPlugin.context = bundleContext;

	atStart();

	checkBSlLS();
	runBSLLS();
	bindingLSP();

	startWindowsListener();
    }

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

    protected BundleContext getContext() {
	return context;
    }

    public void sleepCurrentThread(long value) {
	try {
	    Thread.sleep(value);
	} catch (Exception e) {
	    createWarningStatus(e.getMessage());
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
	var result = true;
	if (bslConnector == null) {
	    result = false;
	} else {
	    result = processLSP != null && processLSP.isAlive();
	}
	return result;
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

    public Map<String, IWorkbenchPart> getWorkbenchParts() {
	return workbenchParts;
    }

    private void startWindowsListener() {
	sleepCurrentThread(1000); // в плагине sonarlint так
	if (PlatformUI.isWorkbenchRunning()) {
	    PlatformUI.getWorkbench().addWindowListener(WINDOWS_EVENT_LISTENER);
	    for (var window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
		WindowEventListener.addListenerToAllPages(window);
	    }
	}
    }

    private void atStart() {
	initAppDir();
	initPreferenceStore();
	prepareForStart();
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

    private void checkBSlLS() {
	if (pathToImageApp.toFile().exists()) {
	    return;
	}

	// проверяем на повторный запуск
	var jobName = "Загрузка BSL LS с GitHub";
	var jobs = Job.getJobManager().find(jobName);
	if (jobs.length > 0) {
	    return;
	}

	var job = new Job(jobName) {
	    @Override
	    protected IStatus run(IProgressMonitor monitor) {
		BSLCommon.runDownloadImageApp();
		if (pathToImageApp.toFile().exists()) {
		    return Status.OK_STATUS;
		}
		return Status.CANCEL_STATUS;
	    }
	};
	job.addJobChangeListener(new JobChangeAdapter() {
	    @Override
	    public void done(IJobChangeEvent event) {
		if (event.getResult().isOK()) {
		    restartLS();
		}
	    }
	});
	job.schedule();
    }

    private void runBSLLS() {
	processLSP = null;

	var externalJar = preferenceStore.getBoolean(BSLPreferencePage.EXTERNAL_JAR);
	var isImageApp = !externalJar;
	Path pathToLSP = getPathToBSLLS();

	if (!pathToLSP.toFile().exists()) {
	    return;
	}

	List<String> arguments = new ArrayList<>();
	if (!isImageApp) {
	    arguments.add(preferenceStore.getString(BSLPreferencePage.PATH_TO_JAVA));
	    arguments.add("-jar");
	}
	arguments.add("\"" + pathToLSP.toString() + "\"");

	if (pathToConfiguration.isPresent()) {
	    arguments.add("--configuration");
	    arguments.add(pathToConfiguration.get().toString());
	}

	createWarningStatus(arguments.toString());

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
