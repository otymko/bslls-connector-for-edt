package com.github.otymko.dt.bsl.lsconnector.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.github.otymko.dt.bsl.lsconnector.BSLPlugin;

public class BSLPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    public static final String PATH_TO_BSLLS = "PATH_TO_BSLLS";
    public static final String EXTERNAL_JAR = "EXTERNAL_JAR";
    public static final String PATH_TO_JAVA = "PATH_TO_JAVA";

    public BSLPreferencePage() {
	setPreferenceStore(BSLPlugin.getPlugin().getPreferenceStore());
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
    }

    @Override
    protected void performApply() {
	super.performApply();
	BSLPlugin.getPlugin().restartLS();
    }

}
