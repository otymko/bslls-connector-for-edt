package com.github.otymko.dt.bsl.lsconnector.util;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.lsp4j.Range;

import com.github._1c_syntax.utils.Absolute;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BSLCommon {
    public final int DEFAULT_TIMEOUT = 30;
    
    public void runFutureTask(Runnable runnable, int timeoutInSeconds) {
	var threadpool = Executors.newCachedThreadPool();
	var futureTask = threadpool.submit(runnable);
	try {
	    futureTask.get(timeoutInSeconds, TimeUnit.SECONDS);
	    } catch (Exception e){
	        e.printStackTrace();
	        futureTask.cancel(true);
	    }
	threadpool.shutdown();
    }
    
    public URI uri(URI uri) {
	return Absolute.uri(uri);
    }
    
    public int[] getOffsetByRange(Range range, Document document) throws BadLocationException {
	int offset, lenght = 0;
	offset = document.getLineOffset(range.getStart().getLine()) + range.getStart().getCharacter();
	lenght = document.getLineOffset(range.getEnd().getLine()) + range.getEnd().getCharacter() - offset;
	return new int[] { offset, lenght };
    }

}
