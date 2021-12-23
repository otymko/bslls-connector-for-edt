package com.github.otymko.dt.bsl.lsconnector.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.github.otymko.dt.bsl.lsconnector.BSLActivator;

public class PluginPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    public static final String PLUGIN_ENABLE = "ENABLE";
    public static final String PATH_TO_BSLLS = "PATH_TO_LS";
    public static final String EXTERNAL_JAR = "EXTERNAL_JAR";
    public static final String PATH_TO_JAVA = "PATH_TO_JAVA";
    public static final String JAVA_OPTS = "JAVA_OPTS";
    
    public PluginPreferencePage() {
	setPreferenceStore(BSLActivator.getPlugin().getPreferenceStore());
    }
    
    @Override
    public void init(IWorkbench workbench) {
	// none
    }
    
    @Override
    protected void createFieldEditors() {
	var parent = getFieldEditorParent();
	
	var pathToJava = new StringFieldEditor(PATH_TO_JAVA, "Путь к JAVA", parent);
	addField(pathToJava);
	
	var pathToBSLLS = new FileFieldEditor(PATH_TO_BSLLS, "Путь к BSL LS", parent);
	addField(pathToBSLLS);
	
	var externalJar = new BooleanFieldEditor(EXTERNAL_JAR, "Использовать внешний JAR", parent);
	addField(externalJar);
	
	var javaOpts = new StringFieldEditor(JAVA_OPTS, "Java Opts", parent);
	addField(javaOpts);
	
	var enable = new BooleanFieldEditor(PLUGIN_ENABLE, "Включить плагин", parent);
	addField(enable);
    }

}
