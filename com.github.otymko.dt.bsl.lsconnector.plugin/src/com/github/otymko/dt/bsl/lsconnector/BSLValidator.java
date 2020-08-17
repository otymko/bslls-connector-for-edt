package com.github.otymko.dt.bsl.lsconnector;

import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.CheckType;

import com._1c.g5.v8.dt.bsl.model.Module;
import com._1c.g5.v8.dt.bsl.validation.CustomValidationMessageAcceptor;
import com._1c.g5.v8.dt.bsl.validation.IExternalBslValidator;

public class BSLValidator implements IExternalBslValidator {

	@Override
	public boolean needValidation(EObject object) {
		return object instanceof Module;
	}

	@Override
	@Check(CheckType.EXPENSIVE)
	public void validate(EObject object, CustomValidationMessageAcceptor messageAcceptor, CancelIndicator monitor) {
		if (monitor.isCanceled()) {
			return;
		}

		var module = (Module) object;
		var node = NodeModelUtils.findActualNodeFor(module);
		var content = node.getText();
		var document = new Document(content);
		var moduleFile = ResourcesPlugin.getWorkspace().getRoot()
				.getFile(new Path(EcoreUtil.getURI(module).toPlatformString(true)));
		var uri = moduleFile.getLocationURI();

		// TODO: нужно ли открывать каждый раз ?
		BSLPlugin.getPlugin().getBSLConnector().textDocumentDidOpen(uri, content);
		BSLPlugin.getPlugin().getBSLConnector().textDocumentDidChange(uri, content);
		BSLPlugin.getPlugin().getBSLConnector().textDocumentDidSave(uri);

		List<Diagnostic> list = BSLPlugin.getPlugin().getBSLConnector().diagnostics(uri.toString());
		list.forEach(diagnostic -> acceptIssue(module, messageAcceptor, diagnostic, document));
	}

	private void acceptIssue(Module module, CustomValidationMessageAcceptor messageAcceptor, 
			Diagnostic diagnostic, Document document) {
		int[] offsetParams;
		try {
			offsetParams = getOffsetByRange(diagnostic.getRange(), document);
		} catch (BadLocationException e) {
			BSLPlugin.createErrorStatus(e.getMessage(), e);
			return;
		}
		messageAcceptor.acceptError(diagnostic.getMessage(), module, offsetParams[0], offsetParams[1],
				diagnostic.getCode());
	}

	private int[] getOffsetByRange(Range range, Document document) throws BadLocationException {
		int offset, lenght = 0;
		offset = document.getLineOffset(range.getStart().getLine()) + range.getStart().getCharacter();
		lenght = document.getLineOffset(range.getEnd().getLine()) + range.getEnd().getCharacter() - offset;
		return new int[] { offset, lenght };
	}

}
