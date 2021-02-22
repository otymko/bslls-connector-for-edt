package com.github.otymko.dt.bsl.lsconnector.util;

import java.net.URI;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.resource.BslResource;
import com._1c.g5.v8.dt.bsl.validation.CustomValidationMessageAcceptor;
import com.github.otymko.dt.bsl.lsconnector.BSLActivator;
import com.github.otymko.dt.bsl.lsconnector.core.BSLCore;
import com.github.otymko.dt.bsl.lsconnector.lsp.BSLConnector;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidatorHelper {
    
    public URI getUriFromModule(Module module) {
	var path = new Path(EcoreUtil.getURI(module).toPlatformString(true));
	var moduleFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
	return BSLCommon.uri(moduleFile.getLocationURI());
    }
    
    public boolean isDeepAnalysing(Module object) {
	var resource = (BslResource) object.eResource();
	return resource.isDeepAnalysing();
    }
    
    public String getContentFromModule(Module module) {
	var node = NodeModelUtils.findActualNodeFor(module);
	var content = node.getText();
	if (content == null) {
	    content = "";
	}
	return content;
    }
    
    public void publishIssue(Module module, CustomValidationMessageAcceptor messageAcceptor, 
	    Diagnostic diagnostic, Document document) {
	int[] offsetParams;
	try {
	    offsetParams = BSLCommon.getOffsetByRange(diagnostic.getRange(), document);
	} catch (BadLocationException e) {
	    BSLActivator.createErrorStatus(e.getMessage(), e);
	    return;
	}
	var severity = diagnostic.getSeverity();
	var message = "[BSL LS] " + diagnostic.getMessage();
	if (severity == DiagnosticSeverity.Error) {
	    messageAcceptor.acceptError(message, module, offsetParams[0], offsetParams[1], diagnostic.getCode());
	} else if (severity == DiagnosticSeverity.Warning) {
	    messageAcceptor.acceptWarning(message, module, offsetParams[0], offsetParams[1], diagnostic.getCode());
	} else {
	    messageAcceptor.acceptInfo(message, module, offsetParams[0], offsetParams[1], diagnostic.getCode());
	}

    }
    
    public BSLConnector getBSLConnector(Module module) {
	var uri = ValidatorHelper.getUriFromModule(module);
	var manager = BSLCore.getInstance().getServiceManager();
	var service = manager.getOrCreateService(uri);
	if (service == null) {
	    return null;
	}
	return service.getConnector();
    }

}
