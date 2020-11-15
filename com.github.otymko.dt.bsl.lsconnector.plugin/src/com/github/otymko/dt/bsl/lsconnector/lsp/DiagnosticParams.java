package com.github.otymko.dt.bsl.lsconnector.lsp;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;

public class DiagnosticParams {
    private TextDocumentIdentifier textDocument;
    private Range range;

    public DiagnosticParams(TextDocumentIdentifier textDocument) {
	this.textDocument = textDocument;
    }

    public TextDocumentIdentifier getTextDocument() {
	return textDocument;
    }

    public void setTextDocument(TextDocumentIdentifier textDocument) {
	this.textDocument = textDocument;
    }
    
    public Range getRange() {
	return range;
    }
    
    public void setRange(Range range) {
	this.range = range;
    }

}
