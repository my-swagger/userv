package com.userv;

import java.util.concurrent.LinkedBlockingDeque;

import org.jboss.netty.channel.Channel;

/**
 * 
 * @author Bot
 * 
 */
public class Client {

	/**
	 * Our current connection stage. Default is Handshake.
	 */
	private Stage stage;
	/**
	 * Our current connection channel.
	 */
	private Channel channel;
	/**
	 * All our requests.
	 */
	private LinkedBlockingDeque<FileRequest> requests;

	/**
	 * Creates a new Client with the specified Channel
	 * 
	 * @param channel
	 *            The channel specified
	 */
	public Client(Channel channel) {
		this.channel = channel;
		this.requests = new LinkedBlockingDeque<FileRequest>(21);
		this.stage = Stage.HANDSHAKE;
	}

	/**
	 * Submits a file request to be served.
	 * 
	 * @param request
	 *            The request to served
	 * @return Whether or not the request was submitted
	 */
	public boolean submitRequest(FileRequest request) {
		if (requests.size() == 20) {
			return false;
		}
		return requests.add(request);
	}

	/**
	 * Sets the client's current stage to the one specified
	 * 
	 * @param stage
	 *            The new stage.
	 */
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	/**
	 * 
	 * @return The next FileRequest in the queue.
	 */
	public FileRequest nextRequest() {
		return requests.poll();
	}

	/**
	 * 
	 * @return The client's connection stage.
	 */
	public Stage getStage() {
		return stage;
	}

	/**
	 * 
	 * @return The client's channel.
	 */
	public Channel getChannel() {
		return channel;
	}
}