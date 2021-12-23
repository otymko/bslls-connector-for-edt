package com.github.otymko.dt.bsl.lsconnector.service;

import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;

import com.github.otymko.dt.bsl.lsconnector.BSLActivator;
import com.github.otymko.dt.bsl.lsconnector.core.BSLCore;
import com.github.otymko.dt.bsl.lsconnector.util.ProjectHelper;

public class LanguageServiceManager {
    private Map<IProject, LanguageService> pool = new ConcurrentHashMap<>();
    
    public LanguageServiceManager() {
	// none
    }
    
    public LanguageService getOrCreateService(URI uri) {
	var pluginSetting = BSLCore.getInstance().getSetting();
	if (!pluginSetting.isEnable()) {
	    return null;
	}
	if (!pluginSetting.getPathToLS().toFile().exists()) {
	    BSLActivator.createWarningStatus("Не удалось найти исполняемый файл BSL LS");
	    return null;
	}
	
	var projectOptional = ProjectHelper.getProjectByPath(Path.of(uri));
	if (projectOptional.isEmpty()) {
	    return null;
	}	
	var project = projectOptional.get();
	var manager = BSLCore.getInstance().getServiceManager();
	
	LanguageService service;
	var optionalService = manager.getService(project);
	if (optionalService.isPresent()) {
	    service = optionalService.get();
	} else {
	    service = manager.createService(project);
    	}
	return service;
    }
    
    public Optional<LanguageService> getService(IProject project) {
	return Optional.ofNullable(pool.get(project));
    }

    public LanguageService createService(IProject project) {
	var rootPath = ProjectHelper.getProjectPath(project);
	var service = new LanguageService(BSLCore.getInstance().getSetting(), rootPath);
	pool.put(project, service);
	
	service.start();
	
	return service;
    }

    public void stopServer(IProject project) {
	getService(project).ifPresent(this::stopService);
    }

    public void stopService(LanguageService service) {
	service.stop();
    }

    public void stopServiceAll() {
	pool.values().forEach(this::stopService);
    }
    
}
