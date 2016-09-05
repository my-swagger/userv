package com.userv;

import java.util.LinkedList;

/**
 * This class is made to tell a FileRequest to service as well as intercept any calls.
 * 
 * @author Bot
 * 
 */
public abstract class FileRequestWorker {
	/**
	 * <p>
	 * Services a number of requests(Usually the amount of clients connected multiplied by 5)
	 * in a single 400ms cycle.
	 * </p>
	 * 
	 * @param requests
	 *            The requests that will be serviced.
	 */
	public abstract void service(LinkedList<FileRequest> requests);

}