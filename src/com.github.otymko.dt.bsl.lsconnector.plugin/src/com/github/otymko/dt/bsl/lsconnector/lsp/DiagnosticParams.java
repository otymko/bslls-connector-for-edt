package com.github.otymko.dt.bsl.lsconnector.lsp;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiagnosticParams {
    private TextDocumentIdentifier textDocument;
    private Range range;
}
