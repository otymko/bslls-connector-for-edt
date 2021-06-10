package com.github.otymko.dt.bsl.lsconnector.util;

import java.nio.file.Path;

import com.github.otymko.dt.bsl.lsconnector.BSLActivator;
import com.github.otymko.dt.bsl.lsconnector.core.PluginSetting;
import com.github.otymko.dt.bsl.lsconnector.ui.PluginPreferencePage;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SettingHelper {
    
    public PluginSetting getSetting() {
	var preferenceStore = BSLActivator.getPlugin().getPreferenceStore();
	var setting = new PluginSetting();
	setting.setEnable(preferenceStore.getBoolean(PluginPreferencePage.PLUGIN_ENABLE));
	setting.setPathToJava(preferenceStore.getString(PluginPreferencePage.PATH_TO_JAVA));
	setting.setPathToLS(Path.of(preferenceStore.getString(PluginPreferencePage.PATH_TO_BSLLS)));
	setting.setJavaOpts(preferenceStore.getString(PluginPreferencePage.JAVA_OPTS));
	setting.setExternalJar(preferenceStore.getBoolean(PluginPreferencePage.EXTERNAL_JAR));
	return setting;
    }

}
