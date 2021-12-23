package com.github.otymko.dt.bsl.lsconnector;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.Document;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.CheckType;

import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.validation.CustomValidationMessageAcceptor;
import com._1c.g5.v8.dt.bsl.validation.IExternalBslValidator;
import com.github.otymko.dt.bsl.lsconnector.core.BSLCore;
import com.github.otymko.dt.bsl.lsconnector.util.BSLCommon;
import com.github.otymko.dt.bsl.lsconnector.util.ValidatorHelper;

public class BSLValidator implements IExternalBslValidator {

    @Override
    public boolean needValidation(EObject object) {
	return object instanceof Module;
    }

    @Override
    @Check(CheckType.EXPENSIVE)
    public void validate(EObject object, CustomValidationMessageAcceptor messageAcceptor, 
	    CancelIndicator monitor) {
	
	if (monitor.isCanceled()) {
	    return;
	}
	
	var module = (Module) object;
	if (!ValidatorHelper.isDeepAnalysing(module)) {
	    return;
	}
	
	BSLCommon.runFutureTask(
		() -> validateFuture(module, messageAcceptor, monitor), BSLCommon.DEFAULT_TIMEOUT);
    }
    
    private void validateFuture(Module module, CustomValidationMessageAcceptor messageAcceptor, 
	    CancelIndicator monitor) {
	
	var connector = ValidatorHelper.getBSLConnector(module);
	if (connector == null) {
	    BSLActivator.createWarningStatus("Нет подключения к BSL LS");
	    return;
	}
	
	var content = ValidatorHelper.getContentFromModule(module);
	var document = new Document(content);
	var uri = ValidatorHelper.getUriFromModule(module);
	
	// или DidOpen или DidChange
	connector.textDocumentDidOpen(uri, content);
	// нужно ждать textDocumentDidOpen
	BSLCore.getInstance().sleepCurrentThread(1000);
	
	if (monitor.isCanceled()) {
	    return;
	}
	
	var diagnostics = connector.diagnostics(uri.toString());
	
	diagnostics.forEach(diagnostic -> {
	    if (monitor.isCanceled()) {
		return;
	    }
	    ValidatorHelper.publishIssue(module, messageAcceptor, diagnostic, document);
	});
    }

}
