package com.github.otymko.dt.bsl.lsconnector.util;

import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ProjectHelper {
    
    public Optional<IProject> getProjectByPath(Path pathToFile) {
	IProject project = null;
	for (IProject currentProject : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
	    var pathProject = getProjectPath(currentProject);
	    if (pathToFile.startsWith(pathProject)) {
		project = currentProject;
		break;
	    }
	}
	return Optional.ofNullable(project);
    }

    public Path getProjectPath(IProject project) {
	return project.getLocation().toFile().toPath();
    }
    
}
