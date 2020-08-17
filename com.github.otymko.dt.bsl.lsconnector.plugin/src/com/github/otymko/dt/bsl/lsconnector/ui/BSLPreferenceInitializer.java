package com.github.otymko.dt.bsl.lsconnector.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import com.github.otymko.dt.bsl.lsconnector.BSLPlugin;

public class BSLPreferenceInitializer extends AbstractPreferenceInitializer {

	public BSLPreferenceInitializer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initializeDefaultPreferences() {
		var scopedPreferenceStore = BSLPlugin.getPlugin().getPreferenceStore();
		scopedPreferenceStore.setDefault(BSLPreferencePage.PATH_TO_JAVA, "java");
	}

}
