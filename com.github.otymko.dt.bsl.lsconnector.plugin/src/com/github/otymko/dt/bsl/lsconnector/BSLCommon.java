package com.github.otymko.dt.bsl.lsconnector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IPath;

public final class BSLCommon {

    public static Optional<Path> getConfigurationFileFromWorkspace(IPath pathToWorkspace) throws IOException {
	var listFiles = Files.walk(Path.of(pathToWorkspace.toFile().toURI())).filter(Files::isRegularFile)
		.filter(path -> path.endsWith(".bsl-language-server.json")).collect(Collectors.toList());
	if (!listFiles.isEmpty()) {
	    return Optional.of(listFiles.get(0));
	}

	return Optional.empty();
    }

}
