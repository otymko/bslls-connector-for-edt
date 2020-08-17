package com.github.otymko.dt.bsl.lsconnector;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

import com.github.otymko.dt.bsl.lsconnector.lsp.BSLConnector;
import com.github.otymko.dt.bsl.lsconnector.lsp.BSLLanguageClient;

public class BSLPlugin extends Plugin {
	public static final String PLUGIN_ID = "com.github.otymko.dt.bsl.ls_connector";
	private static final URI FAKE_URI = Path.of("fake.bsl").toUri();
	private static BSLPlugin plugin;
	private static BundleContext context;
	private Process processLSP;
	private BSLConnector bslConnector;

	public static BSLPlugin getPlugin() {
		return plugin;
	}

	public void start(BundleContext bundleContext) throws Exception {
		plugin = this;
		super.start(bundleContext);
		BSLPlugin.context = bundleContext;

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

	private void runBSLLS() {
		processLSP = null;
		
		var pathToLSP = getPathToBSLLS();
		List<String> arguments = new ArrayList<>();
		arguments.add("java");
		arguments.add("-jar");
		arguments.add(pathToLSP.toString());
//		arguments.add("--configuration");
//		arguments.add("./.bsl-language-server.json");

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

	private void bindingLSP() {
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
		return Path.of(
				"D:\\DATA\\Develop\\Project\\bsl-language-server\\build\\libs\\bsl-language-server-feature-jsonrpc-diagnostics-146b9d4-DIRTY-exec.jar");
	}

	protected BundleContext getContext() {
		return context;
	}
}
