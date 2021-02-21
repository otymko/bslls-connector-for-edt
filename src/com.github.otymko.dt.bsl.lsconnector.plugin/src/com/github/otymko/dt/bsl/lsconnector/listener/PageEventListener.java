package com.github.otymko.dt.bsl.lsconnector.listener;

import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWorkbenchPage;

public class PageEventListener implements IPageListener {
    @Override
    public void pageClosed(IWorkbenchPage page) {
	WindowEventListener.removeListenersFromPage(page);
    }

    @Override
    public void pageOpened(IWorkbenchPage page) {
	WindowEventListener.addListenersToPage(page);
    }

    @Override
    public void pageActivated(IWorkbenchPage page) {
	// none
    }
}
