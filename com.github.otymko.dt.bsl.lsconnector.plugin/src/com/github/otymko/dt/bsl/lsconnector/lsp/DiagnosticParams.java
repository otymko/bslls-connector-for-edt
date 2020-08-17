package com.github.otymko.dt.bsl.lsconnector.lsp;

import org.eclipse.lsp4j.TextDocumentIdentifier;

public class DiagnosticParams {
    private TextDocumentIdentifier textDocument;

    public DiagnosticParams(TextDocumentIdentifier textDocument) {
	this.textDocument = textDocument;
    }

    public TextDocumentIdentifier getTextDocument() {
	return textDocument;
    }

    public void setTextDocument(TextDocumentIdentifier textDocument) {
	this.textDocument = textDocument;
    }

}
