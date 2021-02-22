package com.github.otymko.dt.bsl.lsconnector.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import com.github.otymko.dt.bsl.lsconnector.BSLActivator;
import com.github.otymko.dt.bsl.lsconnector.core.BSLCore;

public class PluginPreferenceInitializer extends AbstractPreferenceInitializer{
    
    public PluginPreferenceInitializer() {
	// noop
    }
    
    @Override
    public void initializeDefaultPreferences() {
	var preferenceStore = BSLActivator.getPlugin().getPreferenceStore();
	preferenceStore.setDefault(PluginPreferencePage.PATH_TO_BSLLS, BSLCore.getInstance().getPathToImageApp().toString());
	preferenceStore.setDefault(PluginPreferencePage.EXTERNAL_JAR, false);
	preferenceStore.setDefault(PluginPreferencePage.PATH_TO_JAVA, "java");
	preferenceStore.setDefault(PluginPreferencePage.JAVA_OPTS, "");
    }

}
