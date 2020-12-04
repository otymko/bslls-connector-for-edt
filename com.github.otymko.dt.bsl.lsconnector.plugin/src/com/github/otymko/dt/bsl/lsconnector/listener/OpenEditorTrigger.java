package com.github.otymko.dt.bsl.lsconnector.listener;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

import com._1c.g5.v8.dt.bsl.ui.editor.BslXtextEditor;
import com.github.otymko.dt.bsl.lsconnector.BSLPlugin;
import com.github.otymko.dt.bsl.lsconnector.util.BSLCommon;

public class OpenEditorTrigger implements IPartListener2 {

    @Override
    public void partActivated(IWorkbenchPartReference partRef) {
	var plugin = BSLPlugin.getPlugin();
	if (plugin.isRunningLS()) {
	    return;
	}

	var part = partRef.getPart(true);
	if (part instanceof BslXtextEditor) {
	    var editorPart = (BslXtextEditor) part;
	    var uri = BSLCommon.uri(editorPart.getResource().getLocationURI());

	    if (plugin.getWorkbenchParts().get(uri.toString()) == null) {
		partOpened(partRef);
	    }
	}

    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
	if (!BSLPlugin.getPlugin().isRunningLS()) {
	    return;
	}
	var part = partRef.getPart(true);
	if (part instanceof BslXtextEditor) {
	    var editorPart = (BslXtextEditor) part;
	    var uri = BSLCommon.uri(editorPart.getResource().getLocationURI());
	    var plugin = BSLPlugin.getPlugin();
	    plugin.getWorkbenchParts().remove(uri.toString());
	    plugin.getLSService().getConnector().textDocumentDidClose(uri);
	}
    }

    @Override
    public void partOpened(IWorkbenchPartReference partRef) {
	if (!BSLPlugin.getPlugin().isRunningLS()) {
	    return;
	}
	var part = partRef.getPart(true);
	if (part instanceof BslXtextEditor) {
	    var editorPart = (BslXtextEditor) part;
	    var uri = BSLCommon.uri(editorPart.getResource().getLocationURI());
	    var content = BSLCommon.getContentFromXtextEditor(editorPart);
	    var plugin = BSLPlugin.getPlugin();
	    plugin.getWorkbenchParts().put(uri.toString(), part);
	    plugin.getLSService().getConnector().textDocumentDidOpen(uri, content);
	}
    }

    @Override
    public void partHidden(IWorkbenchPartReference partRef) {
	// none
    }

    @Override
    public void partVisible(IWorkbenchPartReference partRef) {
	// none
    }

    @Override
    public void partInputChanged(IWorkbenchPartReference partRef) {
	// none
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partRef) {
	// none
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
	// none
    }

}
