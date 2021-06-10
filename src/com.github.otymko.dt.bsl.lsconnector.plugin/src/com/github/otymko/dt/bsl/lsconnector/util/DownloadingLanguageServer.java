package com.github.otymko.dt.bsl.lsconnector.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;

import com.github.otymko.dt.bsl.lsconnector.BSLActivator;
import com.github.otymko.dt.bsl.lsconnector.core.BSLCore;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DownloadingLanguageServer {
    private final int BUFFER_SIZE = 4096;
    private final int DEFAULT_TIMEOUT = 300;
    private static final String ARCHIVE_FILE_NAME = "bsl-language-server.zip";

    public void runDownloadImageApp() {
	try {
	    downloadImageApp();
	} catch (IOException e) {
	    BSLActivator.createErrorStatus(e.getMessage(), e);
	}
    }
    
    public boolean needDownloadLanguageServer() {
	return !BSLCore.getInstance().getPathToImageApp().toFile().exists();
    }
    
    private void downloadImageApp() throws MalformedURLException, IOException {
	var appDir = BSLCore.getInstance().getAppPath();
	var file = Path.of(appDir.toString(), ARCHIVE_FILE_NAME).toFile();
	if (!file.exists()) {
	    downloadLS(file, getLatestReleaseURL());
	}
	// распакуем
	unzip(file.toString(), appDir.toString());
    }

    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
	var bos = new BufferedOutputStream(new FileOutputStream(filePath));
	var bytesIn = new byte[BUFFER_SIZE];
	var read = 0;
	while ((read = zipIn.read(bytesIn)) != -1) {
	    bos.write(bytesIn, 0, read);
	}
	bos.close();
    }
    
    private void downloadLS(File file, String urlRelease) throws MalformedURLException, IOException {
	if (!file.exists()) {
	    FileUtils.copyURLToFile(new URL(urlRelease), file, DEFAULT_TIMEOUT, 0);
	}
    }
    
    private String getLatestReleaseURL() {
	// FIXME: переехать на получение последнего с GitHub
	// нужно учесть, что мининимальная допустимая версия - 0.17.0
	return "https://github.com/1c-syntax/bsl-language-server/releases/download/v0.17.0-RC4/bsl-language-server_win.zip";
    }
    
    private void unzip(String zipFilePath, String destDirectory) throws IOException {
	var destDir = new File(destDirectory);
	if (!destDir.exists()) {
	    destDir.mkdir();
	}
	var zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
	var entry = zipIn.getNextEntry();
	while (entry != null) {
	    var filePath = destDirectory + File.separator + entry.getName();
	    if (!entry.isDirectory()) {
		extractFile(zipIn, filePath);
	    } else {
		var dir = new File(filePath);
		dir.mkdirs();
	    }
	    zipIn.closeEntry();
	    entry = zipIn.getNextEntry();
	}
	zipIn.close();
    }
    
}
