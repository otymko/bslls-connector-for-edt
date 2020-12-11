package com.github.otymko.dt.bsl.lsconnector.service;

import org.eclipse.ui.PlatformUI;

import com.github.otymko.dt.bsl.lsconnector.listener.WindowEventListener;

public class WindowsEventService {
    private static final WindowEventListener WINDOWS_EVENT_LISTENER = new WindowEventListener();

    public void start() {
	if (PlatformUI.isWorkbenchRunning()) {
	    PlatformUI.getWorkbench().addWindowListener(WINDOWS_EVENT_LISTENER);
	    for (var window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
		WindowEventListener.addListenerToAllPages(window);
	    }
	}
    }

    public void stop() {
	if (PlatformUI.isWorkbenchRunning()) {
	    for (var window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
		WindowEventListener.removeListenerFromAllPages(window);
	    }
	}
    }

}
