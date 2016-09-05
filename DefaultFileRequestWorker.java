package com.userv;

import java.util.LinkedList;

/**
 * 
 * @author Bot
 * 
 */
public class DefaultFileRequestWorker extends FileRequestWorker {

	@Override
	public void service(LinkedList<FileRequest> requests) {
		for (FileRequest request : requests) {
			request.service();
		}
	}
}