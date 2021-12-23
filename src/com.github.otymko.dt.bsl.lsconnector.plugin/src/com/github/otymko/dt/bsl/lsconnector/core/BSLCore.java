package com.github.otymko.dt.bsl.lsconnector.core;

import java.nio.file.Path;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import com.github.otymko.dt.bsl.lsconnector.BSLActivator;
import com.github.otymko.dt.bsl.lsconnector.service.LanguageServiceManager;
import com.github.otymko.dt.bsl.lsconnector.util.DownloadingLanguageServer;
import com.github.otymko.dt.bsl.lsconnector.util.SettingHelper;

import lombok.Getter;

public class BSLCore {
    private static final BSLCore INSTANCE = new BSLCore();
    @Getter
    private final LanguageServiceManager serviceManager;
    @Getter
    private final Path appPath;
    @Getter
    private final Path pathToImageApp;

    private BSLCore() {
	this.serviceManager = new LanguageServiceManager();
	
	appPath = Path.of(System.getProperty("user.home"), ".bsl-connector-for-edt");
	if (!appPath.toFile().exists()) {
	    appPath.toFile().mkdir();
	}

	var pathToApp = Path.of(appPath.toString(), "bsl-language-server").toFile();
	if (!pathToApp.exists()) {
	    pathToApp.mkdir();
	}
	pathToImageApp = Path.of(pathToApp.toString(), "bsl-language-server.exe");
    }

    public static BSLCore getInstance() {
	return INSTANCE;
    }

    @Deprecated
    public PluginSetting getSetting() {
	return SettingHelper.getSetting();
    }
    
    public void sleepCurrentThread(long value) {
	try {
	    Thread.sleep(value);
	} catch (Exception e) {
	    BSLActivator.createWarningStatus(e.getMessage());
	}
    }
    
    public void checkExistLanguageServer() {
	if (!DownloadingLanguageServer.needDownloadLanguageServer()) {
	   return;
	}
	
	var jobName = "Загрузка BSL LS с GitHub";
	var jobs = Job.getJobManager().find(jobName);
	if (isJobExist(jobs)) {
	    return;
	}
	
	var job = new Job(jobName) {
	    @Override
	    protected IStatus run(IProgressMonitor monitor) {
		DownloadingLanguageServer.runDownloadImageApp();
		if (pathToImageApp.toFile().exists()) {
		    return Status.OK_STATUS;
		}
		return Status.CANCEL_STATUS;
	    }
	};
	job.addJobChangeListener(new JobChangeAdapter() {
	    @Override
	    public void done(IJobChangeEvent event) {
		if (event.getResult().isOK()) {
		    BSLCore.getInstance().getServiceManager().stopServiceAll();
		}
	    }
	});
	job.schedule(); // 1 сек
	
    }
     
    private boolean isJobExist(Job[] jobs) {
	return jobs.length > 0;
    }

}
