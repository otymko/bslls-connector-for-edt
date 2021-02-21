package com.github.otymko.dt.bsl.lsconnector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

import lombok.Getter;

public class BSLPlugin extends Plugin {
    public static final String PLUGIN_ID = "com.github.otymko.dt.bsl.lsconnector";
    @Getter
    private static BSLPlugin plugin;
    

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
	super.start(bundleContext);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
	plugin = null;
	super.stop(bundleContext);
    }
    
}
