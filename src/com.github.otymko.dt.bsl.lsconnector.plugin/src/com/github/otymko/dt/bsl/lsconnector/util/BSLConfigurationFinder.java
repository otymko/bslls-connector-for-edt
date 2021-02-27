package com.github.otymko.dt.bsl.lsconnector.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import lombok.experimental.UtilityClass;


@UtilityClass
public class BSLConfigurationFinder {
    private final String BSL_CONFIGURATION_FILE_NAME = ".bsl-language-server.json";
   
    public Optional<Path> getPathToBSLConfiguration(Path workspacePath, Path projectPath) {
	var pathInProject = getPathToBSLConfigurationInPath(projectPath);
	if (pathInProject.isEmpty()) {
	    pathInProject = getPathToBSLConfigurationInPath(workspacePath);
	}
	return pathInProject;
    }

    private Optional<Path> getPathToBSLConfigurationInPath(Path basePath) {
	try {
	    return Files.walk(basePath)
		    .filter(path -> path.getFileName().toString().equals(BSL_CONFIGURATION_FILE_NAME))
		    .findAny();
	} catch (IOException e) {
	    return Optional.empty();
	}
    }
    
}
