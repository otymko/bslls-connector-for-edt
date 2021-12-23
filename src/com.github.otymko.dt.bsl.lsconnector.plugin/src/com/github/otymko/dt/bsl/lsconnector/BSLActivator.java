package com.github.otymko.dt.bsl.lsconnector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.github.otymko.dt.bsl.lsconnector.core.BSLCore;
import com.github.otymko.dt.bsl.lsconnector.util.SettingHelper;

import lombok.Getter;

public class BSLActivator extends AbstractUIPlugin  {
    public static final String PLUGIN_ID = "com.github.otymko.dt.bsl.lsconnector";
    @Getter
    private static BSLActivator plugin;
    

    public static IStatus createErrorStatus(String message, Throwable throwable) {
	System.out.println("ERROR: "+ message);
	return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, throwable);
    }

    public static IStatus createWarningStatus(final String message, Exception throwable) {
	System.out.println("WARN: "+ message);
	return new Status(IStatus.WARNING, PLUGIN_ID, 0, message, throwable);
    }

    public static IStatus createWarningStatus(String message) {
	System.out.println("WARN: "+ message);
	return new Status(IStatus.WARNING, PLUGIN_ID, 0, message, null);
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
	plugin = this;
	super.start(bundleContext);
	
	var setting = SettingHelper.getSetting();
	if (!setting.isEnable()) {
	    return;
	}
	BSLCore.getInstance().checkExistLanguageServer();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
	plugin = null;
	super.stop(bundleContext);
    }
    
}
