package com.github.otymko.dt.bsl.lsconnector.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.github.otymko.dt.bsl.lsconnector.BSLPlugin;

public class BSLPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public static final String PATH_TO_JAVA = "PATH_TO_JAVA";
	public static final String JAVA_OPTS = "JAVA_OPTS";
	public static final String USE_JAR = "USE_JAR";
	public static final String PATH_TO_BSLLS = "PATH_TO_BSLLS";

	public BSLPreferencePage() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(BSLPlugin.getPlugin().getPreferenceStore());
        setDescription("Настройки плагина BSLLS для EDT");	
	}

	@Override
	protected void createFieldEditors() {
		addField(new StringFieldEditor(PATH_TO_JAVA, "Путь к Java:", getFieldEditorParent()));
		addField(new StringFieldEditor(JAVA_OPTS, "Java optional:", getFieldEditorParent()));
		addField(new BooleanFieldEditor(USE_JAR, "Внешний Jar", getFieldEditorParent()));
		addField(new StringFieldEditor(PATH_TO_BSLLS, "Внешний путь к Jar:", getFieldEditorParent()));
	}

}
