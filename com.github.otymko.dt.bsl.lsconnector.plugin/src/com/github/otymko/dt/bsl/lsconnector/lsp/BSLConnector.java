package com.github.otymko.dt.bsl.lsconnector.lsp;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeActionCapabilities;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionKindCapabilities;
import org.eclipse.lsp4j.CodeActionLiteralSupportCapabilities;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageServer;

import com.github.otymko.dt.bsl.lsconnector.BSLPlugin;
import com.github.otymko.dt.bsl.lsconnector.util.BSLCommon;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class BSLConnector {
    private static final String LAUNCHER_NAME = "BSLLanguageLauncher";
    private static final Gson GSON = new Gson();
    @SuppressWarnings("serial")
    private static final Type LIST_DIAGNOSTIC_TYPE = new TypeToken<List<Diagnostic>>() {
    }.getType();

    private BSLLanguageClient client;
    private LanguageServer server;
    private InputStream in;
    private OutputStream out;
    private Launcher<LanguageServer> launcher;
    private Thread thread = new Thread(this::start); // TODO: через eclipse

    public BSLConnector(BSLLanguageClient client, InputStream in, OutputStream out) {
	this.client = client;
	this.in = in;
	this.out = out;
    }

    public void startInThread() {
	BSLPlugin.createWarningStatus("Запуск LSP");
	thread.setDaemon(true);
	thread.setName(LAUNCHER_NAME);
	thread.start();
    }

    public CompletableFuture<InitializeResult> initialize() {
	var rootUri = BSLCommon.uri(BSLPlugin.getPlugin().getPathToWorkspace().toUri());
	
	var params = new InitializeParams();
	params.setProcessId((int) ProcessHandle.current().pid());
	params.setTrace("verbose"); // TODO:вынести в константы
	params.setRootUri(rootUri.toString());

	ClientCapabilities serverCapabilities = new ClientCapabilities();
	TextDocumentClientCapabilities textDocument = new TextDocumentClientCapabilities();
	CodeActionCapabilities codeActionCapabilities = new CodeActionCapabilities(false);
	textDocument.setCodeAction(codeActionCapabilities);
	textDocument.setCodeAction(new CodeActionCapabilities(new CodeActionLiteralSupportCapabilities(
		new CodeActionKindCapabilities(Arrays.asList("", CodeActionKind.QuickFix))), false));
	serverCapabilities.setTextDocument(textDocument);

	params.setCapabilities(serverCapabilities);

	return server.initialize(params);
    }

    public CompletableFuture<Object> shutdown() {
	return server.shutdown();
    }

    public void exit() {
	server.exit();
    }

    public void textDocumentDidOpen(URI uri, String text) {
	DidOpenTextDocumentParams params = new DidOpenTextDocumentParams();
	TextDocumentItem item = new TextDocumentItem();
	item.setLanguageId("bsl"); // TODO: вынести в константы
	item.setUri(uri.toString());
	item.setText(text);
	params.setTextDocument(item);
	server.getTextDocumentService().didOpen(params);
    }

    public void textDocumentDidSave(URI uri) {
	var paramsSave = new DidSaveTextDocumentParams();
	TextDocumentIdentifier textDocumentIdentifier = new TextDocumentIdentifier();
	textDocumentIdentifier.setUri(uri.toString());
	paramsSave.setTextDocument(textDocumentIdentifier);
	server.getTextDocumentService().didSave(paramsSave);
    }

    public void textDocumentDidChange(URI uri, String text) {
	var params = new DidChangeTextDocumentParams();
	VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = new VersionedTextDocumentIdentifier();
	versionedTextDocumentIdentifier.setUri(uri.toString());
	versionedTextDocumentIdentifier.setVersion(0);
	params.setTextDocument(versionedTextDocumentIdentifier);
	var textDocument = new TextDocumentContentChangeEvent();
	textDocument.setText(text);
	List<TextDocumentContentChangeEvent> list = new ArrayList<>();
	list.add(textDocument);
	params.setContentChanges(list);
	server.getTextDocumentService().didChange(params);
    }
    
    public void textDocumentDidClose(URI uri) {
	var params = new DidCloseTextDocumentParams();
	var textDocument = new TextDocumentIdentifier(uri.toString());
	params.setTextDocument(textDocument);
	server.getTextDocumentService().didClose(params);
    }

    public List<Diagnostic> diagnostics(String uri) {
	TextDocumentIdentifier textDocument = new TextDocumentIdentifier(uri);
	DiagnosticParams params = new DiagnosticParams(textDocument);
	CompletableFuture<Object> result = launcher.getRemoteEndpoint().request("textDocument/x-diagnostics", params);
	return getDiagnosticFromFuture(result);
    }

    private void start() {
	launcher = LSPLauncher.createClientLauncher(client, in, out);
	Future<?> future = launcher.startListening();
	server = launcher.getRemoteProxy();
	while (true) {
	    try {
		future.get();
		return;
	    } catch (InterruptedException e) {
		BSLPlugin.createErrorStatus(e.getMessage(), e);
	    } catch (ExecutionException e) {
		BSLPlugin.createErrorStatus(e.getMessage(), e);
	    }
	}
    }

    // TODO: перевести на lsp4j
    private List<Diagnostic> getDiagnosticFromFuture(CompletableFuture<Object> future) {	
	JsonObject response;
	JsonArray array = null;
	
	try {
	    response = (JsonObject) future.get();
	    array = (JsonArray) response.get("diagnostics");
	} catch (InterruptedException | ExecutionException e) {
	    BSLPlugin.createErrorStatus(e.getMessage(), e);
	}

	if (array != null) {
	    return GSON.fromJson(array, LIST_DIAGNOSTIC_TYPE);
	}
	return Collections.emptyList();
    }
}
