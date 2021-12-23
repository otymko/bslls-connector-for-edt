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
	// TODO Auto-generated method stub	
    }

    @Override
    public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
	// TODO Auto-generated method stub
    }

    @Override
    public void showMessage(MessageParams messageParams) {
	// TODO Auto-generated method stub
    }

    @Override
    public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void logMessage(MessageParams message) {
	// TODO Auto-generated method stub
    }

}
