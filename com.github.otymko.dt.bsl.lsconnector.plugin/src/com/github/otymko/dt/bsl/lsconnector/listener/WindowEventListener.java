package com.github.otymko.dt.bsl.lsconnector.listener;

import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

public class WindowEventListener implements IWindowListener {
    private static final OpenEditorTrigger OPEN_EDITOR_TRIGGER = new OpenEditorTrigger();
    private static final IPageListener PAGE_EVENT_LISTENER = new PageEventListener();

    public static void addListenerToAllPages(IWorkbenchWindow window) {
	for (IWorkbenchPage page : window.getPages()) {
	    addListenersToPage(page);
	}
    }

    public static void addListenersToPage(IWorkbenchPage page) {
	page.addPartListener(OPEN_EDITOR_TRIGGER);
    }

    public static void removeListenerFromAllPages(IWorkbenchWindow window) {
	for (IWorkbenchPage page : window.getPages()) {
	    removeListenersFromPage(page);
	}
    }

    public static void removeListenersFromPage(IWorkbenchPage page) {
	page.removePartListener(OPEN_EDITOR_TRIGGER);
    }

    @Override
    public void windowClosed(IWorkbenchWindow window) {
	window.removePageListener(PAGE_EVENT_LISTENER);
	removeListenerFromAllPages(window);
    }

    @Override
    public void windowOpened(IWorkbenchWindow window) {
	window.addPageListener(PAGE_EVENT_LISTENER);
	addListenerToAllPages(window);
    }

    @Override
    public void windowActivated(IWorkbenchWindow window) {
	// none
    }

    @Override
    public void windowDeactivated(IWorkbenchWindow window) {
	// none
    }

}
