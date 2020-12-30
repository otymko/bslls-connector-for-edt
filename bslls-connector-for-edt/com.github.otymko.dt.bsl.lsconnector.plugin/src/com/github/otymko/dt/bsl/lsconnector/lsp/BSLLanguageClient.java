package com.github.otymko.dt.bsl.lsconnector.lsp;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.services.LanguageClient;

public class BSLLanguageClient implements LanguageClient {

    @Override
    public void telemetryEvent(Object object) {
	// none
    }

    @Override
    public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
	// none
    }

    @Override
    public void showMessage(MessageParams messageParams) {
	// none
    }

    @Override
    public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
	return null;
    }

    @Override
    public void logMessage(MessageParams message) {
	// none
    }

}
