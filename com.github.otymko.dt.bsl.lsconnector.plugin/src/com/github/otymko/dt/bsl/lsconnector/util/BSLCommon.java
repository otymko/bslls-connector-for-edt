package com.github.otymko.dt.bsl.lsconnector.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.lsp4j.Range;

import com._1c.g5.v8.dt.bsl.ui.editor.BslXtextEditor;
import com.github.otymko.dt.bsl.lsconnector.BSLPlugin;

public class BSLCommon {
    public static final URI FAKE_URI = Path.of("fake.bsl").toUri();
    private static final int BUFFER_SIZE = 4096;
    private static final int DEFAULT_TIMEOUT = 300;

    public static void runDownloadImageApp() {
	try {
	    downloadImageApp();
	} catch (IOException e) {
	    BSLPlugin.createErrorStatus(e.getMessage(), e);
	}
    }

    public static void downloadLS(File file, String urlRelease) throws MalformedURLException, IOException {
	if (!file.exists()) {
	    FileUtils.copyURLToFile(new URL(urlRelease), file, DEFAULT_TIMEOUT, 0);
	}
    }

    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
	var destDir = new File(destDirectory);
	if (!destDir.exists()) {
	    destDir.mkdir();
	}
	var zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
	var entry = zipIn.getNextEntry();
	// iterates over entries in the zip file
	while (entry != null) {
	    var filePath = destDirectory + File.separator + entry.getName();
	    if (!entry.isDirectory()) {
		// if the entry is a file, extracts it
		extractFile(zipIn, filePath);
	    } else {
		// if the entry is a directory, make the directory
		var dir = new File(filePath);
		dir.mkdirs();
	    }
	    zipIn.closeEntry();
	    entry = zipIn.getNextEntry();
	}
	zipIn.close();
    }

    public static Optional<Path> getConfigurationFileFromWorkspace(IPath pathToWorkspace) throws IOException {
	var listFiles = Files.walk(Path.of(pathToWorkspace.toFile().toURI())).filter(Files::isRegularFile)
		.filter(path -> path.endsWith(".bsl-language-server.json")).collect(Collectors.toList());
	if (!listFiles.isEmpty()) {
	    return Optional.of(listFiles.get(0));
	}
	return Optional.empty();
    }

    public static int[] getOffsetByRange(Range range, Document document) throws BadLocationException {
	int offset, lenght = 0;
	offset = document.getLineOffset(range.getStart().getLine()) + range.getStart().getCharacter();
	lenght = document.getLineOffset(range.getEnd().getLine()) + range.getEnd().getCharacter() - offset;
	return new int[] { offset, lenght };
    }

    public static String getContentFromXtextEditor(BslXtextEditor editor) {
	var document = editor.getDocument();
	if (document == null) {
	    return "";
	}
	var content = document.get();
	if (content == null) {
	    content = "";
	}
	return content;
    }

    public static String getLatestReleaseURL() {
	// FIXME: переехать на получение последнего с GitHub
	// нужно учесть, что мининимальная допустимая версия - 0.17.0
	return "https://github.com/1c-syntax/bsl-language-server/releases/download/v0.17.0-RC1/bsl-language-server_win.zip";
    }

    private static void downloadImageApp() throws MalformedURLException, IOException {
	var appDir = BSLPlugin.getPlugin().getAppDir();
	var file = Path.of(appDir.toString(), "bsl-language-server.zip").toFile();
	if (!file.exists()) {
	    downloadLS(file, getLatestReleaseURL());
	}
	// распакуем
	unzip(file.toString(), appDir.toString());
    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
	var bos = new BufferedOutputStream(new FileOutputStream(filePath));
	var bytesIn = new byte[BUFFER_SIZE];
	var read = 0;
	while ((read = zipIn.read(bytesIn)) != -1) {
	    bos.write(bytesIn, 0, read);
	}
	bos.close();
    }

}
